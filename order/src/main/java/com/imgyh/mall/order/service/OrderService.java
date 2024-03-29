package com.imgyh.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imgyh.mall.common.to.mq.SeckillOrderTo;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.order.entity.OrderEntity;
import com.imgyh.mall.order.vo.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 14:52:18
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    void closeOrder(OrderEntity entity);

    PayVo getOrderPay(String orderSn);

    List<OrderwithItemVo> listOrderItem();

    String handlePayResult(PayAsyncVo vo);

    void createSeckillOrder(SeckillOrderTo seckillOrder);
}

