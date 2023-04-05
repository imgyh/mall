package com.imgyh.mall.cart.service;

import com.imgyh.mall.cart.vo.Cart;
import com.imgyh.mall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @ClassName : CartService
 * @Package : com.imgyh.mall.cart.service
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
public interface CartService {
    Cart getCart() throws ExecutionException, InterruptedException;

    void addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);

    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);

    void countItem(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItem> getUserCartItems();
}
