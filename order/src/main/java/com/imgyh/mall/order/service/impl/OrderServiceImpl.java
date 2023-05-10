package com.imgyh.mall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imgyh.mall.common.exception.NoStockException;
import com.imgyh.mall.common.to.mq.OrderTo;
import com.imgyh.mall.common.to.mq.SeckillOrderTo;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.common.utils.Query;
import com.imgyh.mall.common.utils.R;
import com.imgyh.mall.common.vo.MemberRespVo;
import com.imgyh.mall.order.constant.OrderConstant;
import com.imgyh.mall.order.dao.OrderDao;
import com.imgyh.mall.order.entity.OrderEntity;
import com.imgyh.mall.order.entity.OrderItemEntity;
import com.imgyh.mall.order.entity.PaymentInfoEntity;
import com.imgyh.mall.order.enume.OrderStatusEnum;
import com.imgyh.mall.order.feign.CartFeignService;
import com.imgyh.mall.order.feign.MemberFeignService;
import com.imgyh.mall.order.feign.ProductFeignService;
import com.imgyh.mall.order.feign.WareFeignService;
import com.imgyh.mall.order.interceptor.OrderInterceptor;
import com.imgyh.mall.order.service.OrderItemService;
import com.imgyh.mall.order.service.OrderService;
import com.imgyh.mall.order.service.PaymentInfoService;
import com.imgyh.mall.order.to.OrderCreateTo;
import com.imgyh.mall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();
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

    @Autowired
    OrderItemService orderItemService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    PaymentInfoService paymentInfoService;


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

    // 本地事务，在分布式系统，只能控制住自己的回滚，控制不了其他服务的回滚
    // 分布式事务： 最大原因。网络问题+分布式机器。
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        // 将 OrderSubmitVo 放入threadlocal
        confirmVoThreadLocal.set(vo);

        SubmitOrderResponseVo response = new SubmitOrderResponseVo();
        response.setCode(0);
        MemberRespVo memberRespVo = OrderInterceptor.loginUser.get();

        // 0. 验证令牌。从redis中读令牌，比较令牌，删除令牌这三个操作要保证原子性
        // 0令牌失败 - 1删除成功
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()),
                orderToken);

        if (result == 0L){
            // 验证令牌失败
            response.setCode(1);
            return response;
        }
        // 验证令牌成功
        // 下单：去创建订单，验价格，锁库存...
        // 1. 创建订单、订单项等信息
        OrderCreateTo order = createOrder();

        // 2. 验价格
        BigDecimal payAmount = order.getOrder().getPayAmount();
        BigDecimal payPrice = vo.getPayPrice();

        if (Math.abs(payAmount.subtract(payPrice).doubleValue()) >= 0.01) {
            // 验价格失败，价格不对应
            response.setCode(2);
            return response;
        }
        // 验价格成功
        // 保存订单到数据库
        saveOrder(order);

        // 3. 锁库存 只要有异常回滚订单数据。

        // 3.1 创建锁库存数据。订单号，所有订单项（skuId，skuName，num）
        WareSkuLockVo lockVo = new WareSkuLockVo();
        lockVo.setOrderSn(order.getOrder().getOrderSn());
        List<OrderItemVo> locks = order.getOrderItems().stream().map(item -> {
            OrderItemVo itemVo = new OrderItemVo();
            itemVo.setSkuId(item.getSkuId());
            itemVo.setCount(item.getSkuQuantity());
            itemVo.setTitle(item.getSkuName());
            return itemVo;
        }).collect(Collectors.toList());
        lockVo.setLocks(locks);

        // 3.2 远程锁库存
        //库存成功了，但是网络原因超时了，订单回滚，库存不滚。
        //为了保证高并发。库存服务自己回滚。可以发消息给库存服务；
        //库存服务本身也可以使用自动解锁模式  消息
        R r = wareFeignService.orderLockStock(lockVo);
        if (r.getCode() == 0) {
            //锁成功了
            response.setOrder(order.getOrder());

            // 模拟下单异常，库存正常扣减
            // int i = 10/0; //订单回滚，库存不滚

            //订单创建成功发送消息给MQ
            rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());

            return response;
        } else {
            //锁定失败
            String msg = (String) r.get("msg");
            throw new NoStockException(msg);
        }

    }

    /**
     * 保存订单和所有订单项
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 创建订单
     * @return
     */
    private OrderCreateTo createOrder() {
        OrderCreateTo createTo = new OrderCreateTo();
        // 1. 生成订单号
        String orderSn = IdWorker.getTimeId();
        // 2. 创建订单
        OrderEntity orderEntity = buildOrder(orderSn);
        // 3. 获取所有订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);

        // 4、计算价格、积分等相关
        computePrice(orderEntity, itemEntities);

        createTo.setOrder(orderEntity);
        createTo.setOrderItems(itemEntities);

        return createTo;
    }

    /**
     * 构建订单实体
     * @param orderSn
     * @return
     */
    private OrderEntity buildOrder(String orderSn) {
        MemberRespVo respVo = OrderInterceptor.loginUser.get();
        OrderEntity entity = new OrderEntity();

        entity.setOrderSn(orderSn);
        entity.setMemberId(respVo.getId());

        OrderSubmitVo submitVo = confirmVoThreadLocal.get();
        //获取收货地址信息
        R fare = wareFeignService.getFare(submitVo.getAddrId());
        FareVo fareResp = fare.getData(new TypeReference<FareVo>() {
        });

        //设置运费信息
        entity.setFreightAmount(fareResp.getFare());
        //设置收货人信息
        entity.setReceiverCity(fareResp.getAddress().getCity());
        entity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        entity.setReceiverName(fareResp.getAddress().getName());
        entity.setReceiverPhone(fareResp.getAddress().getPhone());
        entity.setReceiverPostCode(fareResp.getAddress().getPostCode());
        entity.setReceiverProvince(fareResp.getAddress().getProvince());
        entity.setReceiverRegion(fareResp.getAddress().getRegion());

        //设置订单的相关状态信息
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setAutoConfirmDay(7);


        return entity;
    }

    /**
     * 所有订单项数据
     * @param orderSn
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //最后确定每个购物项的价格
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> itemEntities = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity itemEntity = buildOrderItem(cartItem);
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
            return itemEntities;
        }

        return null;
    }

    /**
     * 构建每一项订单项
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity();
        //1、订单信息：订单号 v
        //2、商品的SPU信息  V
        Long skuId = cartItem.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = r.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(data.getId());
        itemEntity.setSpuBrand(data.getBrandId().toString());
        itemEntity.setSpuName(data.getSpuName());
        itemEntity.setCategoryId(data.getCatalogId());
        //3、商品的sku信息  v
        itemEntity.setSkuId(cartItem.getSkuId());
        itemEntity.setSkuName(cartItem.getTitle());
        itemEntity.setSkuPic(cartItem.getImage());
        itemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        itemEntity.setSkuAttrsVals(skuAttr);
        itemEntity.setSkuQuantity(cartItem.getCount());
        //4、优惠信息[不做]
        //5、积分信息
        itemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        itemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        //6、订单项的价格信息
        itemEntity.setPromotionAmount(new BigDecimal("0"));
        itemEntity.setCouponAmount(new BigDecimal("0"));
        itemEntity.setIntegrationAmount(new BigDecimal("0"));
        //当前订单项的实际金额。 总额-各种优惠
        BigDecimal orign = itemEntity.getSkuPrice().multiply(new BigDecimal(itemEntity.getSkuQuantity().toString()));
        BigDecimal subtract = orign.subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(subtract);

        return itemEntity;
    }

    /**
     * 计算价格
     * @param orderEntity
     * @param itemEntities
     */
    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal("0.0");

        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");

        BigDecimal gift = new BigDecimal("0.0");
        BigDecimal growth = new BigDecimal("0.0");
        //订单的总额，叠加每一个订单项的总额信息
        for (OrderItemEntity entity : itemEntities) {
            coupon = coupon.add(entity.getCouponAmount());
            integration = integration.add(entity.getIntegrationAmount());
            promotion = promotion.add(entity.getPromotionAmount());
            total = total.add(entity.getRealAmount());
            gift = gift.add(new BigDecimal(entity.getGiftIntegration().toString()));
            growth = growth.add(new BigDecimal(entity.getGiftGrowth().toString()));

        }
        //1、订单价格相关
        orderEntity.setTotalAmount(total);
        //应付总额
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);

        //设置积分等信息
        orderEntity.setIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());
        orderEntity.setDeleteStatus(0);//未删除
    }


    /**
     * 关闭订单
     * @param entity
     */
    @Override
    public void closeOrder(OrderEntity entity) {
        //查询当前这个订单的最新状态
        OrderEntity orderEntity = this.getById(entity.getId());
        if (orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            //关单
            OrderEntity update = new OrderEntity();
            update.setId(entity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(update);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);
            // 关闭订单后，发送消息 解锁库存
            try {
                //TODO 保证消息一定会发送出去，每一个消息都可以做好日志记录（给数据库保存每一个消息的详细信息）。
                //TODO 定期扫描数据库将失败的消息再发送一遍；
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (Exception e) {
                //TODO 将没法送成功的消息进行重试发送。
            }
        }
    }

    // 订单支付页面
    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity order = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));


        BigDecimal bigDecimal = order.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        payVo.setTotal_amount(bigDecimal.toString());
        payVo.setOut_trade_no(order.getOrderSn());

        List<OrderItemEntity> order_sn = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity entity = order_sn.get(0);


        payVo.setSubject(entity.getSkuName());
        payVo.setBody(entity.getSkuAttrsVals());
        return payVo;
    }

    @Override
    public List<OrderwithItemVo> listOrderItem() {
        List<OrderEntity> orders = this.list(new QueryWrapper<OrderEntity>().eq("member_id", OrderInterceptor.loginUser.get().getId()).orderByDesc("id"));
        List<OrderwithItemVo> list = orders.stream().map((order) -> {
            OrderwithItemVo vo = new OrderwithItemVo();
            BeanUtils.copyProperties(order, vo);
            List<OrderItemEntity> items = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", vo.getOrderSn()));
            vo.setItems(items);
            return vo;
        }).collect(Collectors.toList());

        return list;
    }

    // 处理支付宝的支付结果
    @Override
    public String handlePayResult(PayAsyncVo vo) {
        //1、保存交易流水
        PaymentInfoEntity infoEntity = new PaymentInfoEntity();
        infoEntity.setAlipayTradeNo(vo.getTrade_no());
        infoEntity.setOrderSn(vo.getOut_trade_no());
        infoEntity.setPaymentStatus(vo.getTrade_status());
        infoEntity.setCallbackTime(vo.getNotify_time());

        paymentInfoService.save(infoEntity);

        //2、修改订单的状态信息
        if (vo.getTrade_status().equals("TRADE_SUCCESS") || vo.getTrade_status().equals("TRADE_FINISHED")) {
            //支付成功状态
            String outTradeNo = vo.getOut_trade_no();
            // this.baseMapper.updateOrderStatus(outTradeNo,OrderStatusEnum.PAYED.getCode());
            OrderEntity entity = new OrderEntity();
            entity.setStatus(OrderStatusEnum.PAYED.getCode());
            this.update(entity, new UpdateWrapper<OrderEntity>().eq("order_sn", outTradeNo));
        }
        return "success";

    }

    @Override
    public void createSeckillOrder(SeckillOrderTo seckillOrder) {
        //TODO 保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrder.getOrderSn());
        orderEntity.setMemberId(seckillOrder.getMemberId());

        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());

        BigDecimal multiply = seckillOrder.getSeckillPrice().multiply(new BigDecimal("" + seckillOrder.getNum()));
        orderEntity.setPayAmount(multiply);
        this.save(orderEntity);

        //TODO 保存订单项信息
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(seckillOrder.getOrderSn());
        orderItemEntity.setRealAmount(multiply);
        //TODO 获取当前SKU的详细信息进行设置  productFeignService.getSpuInfoBySkuId()
        orderItemEntity.setSkuQuantity(seckillOrder.getNum());

        orderItemService.save(orderItemEntity);

    }

}