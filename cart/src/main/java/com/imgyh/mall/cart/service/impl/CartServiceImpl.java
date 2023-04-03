package com.imgyh.mall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.imgyh.mall.cart.feign.ProductFeignService;
import com.imgyh.mall.cart.interceptor.CartInterceptor;
import com.imgyh.mall.cart.service.CartService;
import com.imgyh.mall.cart.vo.Cart;
import com.imgyh.mall.cart.vo.CartItem;
import com.imgyh.mall.cart.vo.SkuInfoVo;
import com.imgyh.mall.cart.vo.UserInfoTo;
import com.imgyh.mall.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @ClassName : CartServiceImpl
 * @Package : com.imgyh.mall.cart.service.impl
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/4/3 11:02
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/
@Service
public class CartServiceImpl implements CartService {
    private final String CART_PREFIX = "mall:cart:";

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    ThreadPoolExecutor executor;

    /**
     * 删除商品
     * @param skuId
     */
    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    /**
     * 改变商品数量
     * @param skuId
     * @param num
     */
    @Override
    public void countItem(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        if (cartItem != null) {
            cartItem.setCount(num);
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(),s);
        }
    }


    /**
     * 改变商品选中状态
     * @param skuId
     * @param check
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        if (cartItem != null) {
            cartItem.setCheck(check == 1);
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(),s);
        }
    }


    /**
     * 查询购物车所有商品
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        // 1. 得到用户是否登录信息
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String userKey = userInfoTo.getUserKey();
        Long userId = userInfoTo.getUserId();
        String tempCartKey = CART_PREFIX + userKey;


        Cart cart = new Cart();

        if (userInfoTo.getUserId() != null){
            // 登录了
            // 查询userkey临时购物车下有数据没有，有的话要合并到用户购物车[合并购物车]
            // 1. 查询临时购物车中所有商品
            List<CartItem> cartItems = getCartItems(tempCartKey);
            if (cartItems != null){
                for (CartItem cartItem : cartItems) {
                    // 2. 把每个临时购物车的商品加入用户购物车
                    addToCart(cartItem.getSkuId(), cartItem.getCount());
                }
                // 3. 清除临时购物车
                clearCart(tempCartKey);
            }

            // 4. 查询用户购物车中所有商品[包含了用户以前的所有商品和合并了临时购物车中的所有商品]
            String userCartKey = CART_PREFIX + userId.toString();
            List<CartItem> userCartItems = getCartItems(userCartKey);
            cart.setItems(userCartItems);
        }else {
            // 没登录
            // 查询临时购物车中所有商品
            List<CartItem> cartItems = getCartItems(tempCartKey);
            cart.setItems(cartItems);
        }

        return cart;
    }

    /**
     * 清空购物车
     * @param cartKey
     */
    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }


    /**
     * 获取购物车中的所有商品
     * @param cartKey
     * @return
     */
    private List<CartItem> getCartItems(String cartKey){
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(cartKey);

        List<Object> values = ops.values();
        if (values != null && values.size()>0) {
            List<CartItem> collect = values.stream().map((val) -> {
                CartItem item = JSON.parseObject((String) val, CartItem.class);
                return item;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }


    /**
     * 添加商品到购物车
     *
     * @param skuId
     * @param num
     */
    @Override
    public void addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        // 查询redis中是否有这个商品
        String res = (String) cartOps.get(skuId.toString());

        if (StringUtils.isEmpty(res)) {
            // redis中没有这个商品，执行添加操作

            CartItem cartItem = new CartItem();
            // 1. 查询sku信息
            CompletableFuture<Void> task1 = CompletableFuture.runAsync(() -> {
                R skuinfo = productFeignService.getSkuinfo(skuId);
                SkuInfoVo data = skuinfo.getData("skuInfo",new TypeReference<SkuInfoVo>() {
                });
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(data.getSkuDefaultImg());
                cartItem.setSkuId(skuId);
                cartItem.setTitle(data.getSkuTitle());
                cartItem.setPrice(data.getPrice());
            },executor);
            // 2. 查询sku销售属性信息
            CompletableFuture<Void> task2 = CompletableFuture.runAsync(() -> {
                List<String> attrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(attrValues);
            },executor);

            // 3. 等待异步执行完成，保存数据到redis
            CompletableFuture.allOf(task1,task2).get();
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(),s);
        } else {
            // redis中有这个商品，执行更新商品数量操作
            CartItem cartItem = JSON.parseObject(res, new TypeReference<CartItem>() {
            });
            cartItem.setCount(cartItem.getCount()+num);
            String s = JSON.toJSONString(cartItem);
            cartOps.put(skuId.toString(),s);
        }
    }

    /**
     * 查询购物车商品
     * @param skuId
     * @return
     */
    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String res = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(res, new TypeReference<CartItem>() {
        });
        return cartItem;
    }

    /**
     * 获取操作hash的操作器
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            // 把user 的唯一标识符id作为key
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            // 使用随机生成的user-key 作为key
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(cartKey);

        return ops;
    }
}
