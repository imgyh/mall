package com.imgyh.mall.order.web;

import com.imgyh.mall.order.service.OrderService;
import com.imgyh.mall.order.vo.OrderConfirmVo;
import com.imgyh.mall.order.vo.OrderSubmitVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;

/**
 * @ClassName : OrderWebController
 * @Package : com.imgyh.mall.order.web
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/4/4 21:30
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;



    /**
     * 去结算确认页
     * @param model
     * @param request
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model, HttpServletRequest request) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo =  orderService.confirmOrder();

        model.addAttribute("orderConfirmData", confirmVo);
        //展示订单确认的数据
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo){
        System.out.println("订单数据: "+vo);
        return null;
    }
}
