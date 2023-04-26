package com.imgyh.mall.order.web;

import com.imgyh.mall.common.exception.NoStockException;
import com.imgyh.mall.order.service.OrderService;
import com.imgyh.mall.order.vo.OrderConfirmVo;
import com.imgyh.mall.order.vo.OrderSubmitVo;
import com.imgyh.mall.order.vo.OrderwithItemVo;
import com.imgyh.mall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
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

    /**
     * 下单
     * @param vo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes){
        System.out.println("订单数据: "+vo);

        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
            if (responseVo.getCode()==0){
                // 下单成功来到支付选择页
                model.addAttribute("submitOrderResp",responseVo);
                return "pay";
            }else {
                // 下单失败
                String msg = "下单失败；";
                switch (responseVo.getCode()){
                    case 1:  msg += "订单信息过期，请刷新再次提交"; break;
                    case 2: msg+= "订单商品价格发生变化，请确认后再次提交"; break;
                    case 3: msg+="库存锁定失败，商品库存不足"; break;
                }
                redirectAttributes.addFlashAttribute("msg",msg);
                return "redirect:http://order.mall.gyh.im/toTrade";
            }
        }catch (Exception e){
            if (e instanceof NoStockException){
                String message = ((NoStockException) e).getMessage();
                redirectAttributes.addFlashAttribute("msg",message);
            }
            return "redirect:http://order.mall.gyh.im/toTrade";
        }
    }

    @GetMapping("list.html")
    public String listOrder(Model model){
        List<OrderwithItemVo> res = orderService.listOrderItem();
        model.addAttribute("orders", res);

        return "list";
    }
}
