package com.hmall.trade.listener;

import com.hmall.common.amqp.MqConstants;
import com.hmall.common.amqp.message.OrderPaidEvent;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 支付状态变更消费者
 * 监听订单支付成功事件，更新订单状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayStatusListener {

    private final IOrderService orderService;

    @RabbitListener(queues = MqConstants.QUEUE_ORDER_PAID)
    public void handleOrderPaid(OrderPaidEvent event) {
        log.info("收到订单支付成功消息: orderId={}", event.getOrderId());
        try {
            orderService.markOrderPaySuccess(event.getOrderId());
            log.info("订单状态更新成功: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("订单状态更新失败: orderId={}", event.getOrderId(), e);
            throw e; // 消息重回队列或进入 DLQ
        }
    }
}
