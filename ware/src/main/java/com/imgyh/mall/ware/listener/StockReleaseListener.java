package com.imgyh.mall.ware.listener;

import com.imgyh.mall.common.to.mq.OrderTo;
import com.imgyh.mall.common.to.mq.StockLockedTo;
import com.imgyh.mall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @ClassName : StockReleaseListener
 * @Package : com.imgyh.mall.order.listener
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/4/11 19:11
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/
@Component
@RabbitListener(queues = {"stock.release.stock.queue"})
public class StockReleaseListener {
    @Autowired
    WareSkuService wareSkuService;

    // 消费 库存发出来的消息
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁库存的消息...");
        try {
            wareSkuService.unlockStock(to);
            // 应答响应，消费消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }catch (Exception e){
            // 不该自己解锁或者解锁失败，将消息重新加入队列
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    // 消费 订单取消成功后 发的 解锁库存的消息
    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo orderTo, Message message, Channel channel) throws IOException {
        System.out.println("订单关闭准备解锁库存...");
        try{
            wareSkuService.unlockStock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }

}
