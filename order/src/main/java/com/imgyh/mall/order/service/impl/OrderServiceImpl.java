package com.imgyh.mall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.common.utils.Query;
import com.imgyh.mall.common.utils.R;
import com.imgyh.mall.common.vo.MemberRespVo;
import com.imgyh.mall.order.constant.OrderConstant;
import com.imgyh.mall.order.dao.OrderDao;
import com.imgyh.mall.order.entity.OrderEntity;
import com.imgyh.mall.order.feign.CartFeignService;
import com.imgyh.mall.order.feign.MemberFeignService;
import com.imgyh.mall.order.feign.ProductFeignService;
import com.imgyh.mall.order.feign.WareFeignService;
import com.imgyh.mall.order.interceptor.OrderInterceptor;
import com.imgyh.mall.order.service.OrderService;
import com.imgyh.mall.order.vo.MemberAddressVo;
import com.imgyh.mall.order.vo.OrderConfirmVo;
import com.imgyh.mall.order.vo.OrderItemVo;
import com.imgyh.mall.order.vo.SkuStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        // 获取当前用户的信息
        MemberRespVo memberRespVo = OrderInterceptor.loginUser.get();

        //获取之前的请求，用于共享给异步线程，使得feign能够获取到请求头
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // 1. 查询收货地址
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            System.out.println("member线程...." + Thread.currentThread().getId());
            //每一个线程都来共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setAddress(address);
        }, executor);

        // 2. 所有选中的购物项
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            System.out.println("cart线程...." + Thread.currentThread().getId());
            //每一个线程都来共享之前的请求数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(items);
        }, executor).thenRunAsync(()->{
            // 4. 每件商品的库存信息
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());

            R hasStock = wareFeignService.getSkuHasStock(collect);
            List<SkuStockVo> data = hasStock.getData(new TypeReference<List<SkuStockVo>>() {
            });
            if (data != null) {
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(map);
            }

        },executor);

        // 3. 积分信息
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);

        // 其他数据自动计算, 价格总价等

        // 5. 防重复提交令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);

        CompletableFuture.allOf(future1, future2).get();

        return confirmVo;
    }

}