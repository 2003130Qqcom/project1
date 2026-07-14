package com.hmall.pay.publisher;

import com.hmall.common.amqp.MqConstants;
import com.hmall.common.amqp.message.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 支付事件发布者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发布订单支付成功事件
     */
    public void publishOrderPaid(Long orderId) {
        OrderPaidEvent event = new OrderPaidEvent(orderId);
        log.info("发布订单支付成功事件: orderId={}", orderId);
        rabbitTemplate.convertAndSend(
                MqConstants.EXCHANGE_ORDER,
                MqConstants.ROUTING_KEY_ORDER_PAID,
                event
        );
    }
}
