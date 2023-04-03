package com.imgyh.mall.cart.controller;

import com.imgyh.mall.cart.interceptor.CartInterceptor;
import com.imgyh.mall.cart.service.CartService;
import com.imgyh.mall.cart.vo.Cart;
import com.imgyh.mall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * @ClassName : CartController
 * @Package : com.imgyh.mall.cart.controller
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/4/3 10:51
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/
@Controller
public class CartController {
    @Autowired
    CartService cartService;

    /**
     * 商品删除
     * @param skuId
     * @return
     */
    @GetMapping("deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){

        cartService.deleteItem(skuId);
        return "redirect:http://cart.mall.gyh.im/cart.html";
    }

    /**
     * 商品数量修改
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("countItem")
    public String countItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num){

        cartService.countItem(skuId, num);
        return "redirect:http://cart.mall.gyh.im/cart.html";
    }

    /**
     * 选中与不选中商品
     * @param skuId
     * @param check
     * @return
     */
    @GetMapping("checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("check") Integer check){

        cartService.checkItem(skuId, check);
        return "redirect:http://cart.mall.gyh.im/cart.html";
    }

    /**
     *
     * 浏览器有一个cookie；user-key；标识用户身份，一个月后过期；
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份；
     * 浏览器以后保存，每次访问都会带上这个cookie；
     *
     * 登录：session有
     * 没登录：按照cookie里面带来user-key来做。
     * 第一次：如果没有临时用户，帮忙创建一个临时用户。
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        //1、快速得到用户信息，id，user-key
       System.out.println(CartInterceptor.threadLocal.get());

        Cart cart = cartService.getCart();

        model.addAttribute("cart",cart);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     * RedirectAttributes ra
     *      ra.addFlashAttribute();将数据放在session里面可以在页面取出，但是只能取一次
     *      ra.addAttribute("skuId",skuId);将数据放在url后面
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes ra) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId, num);
        ra.addAttribute("skuId", skuId);
        // 添加成功后重定向到成功页面，防止刷新这个请求造成重复添加
        return "redirect:http://cart.mall.gyh.im/addToCartSuccess.html";
    }

    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccess(@RequestParam("skuId") Long skuId, Model model) {
        // 查询购物车，展示添加成功页面
        CartItem item = cartService.getCartItem(skuId);
        model.addAttribute("item", item);
        return "success";
    }
}
