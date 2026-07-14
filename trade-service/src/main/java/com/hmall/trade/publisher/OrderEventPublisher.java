package com.hmall.trade.publisher;

import com.hmall.common.amqp.MqConstants;
import com.hmall.common.amqp.message.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单事件发布者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发布订单创建事件
     */
    public void publishOrderCreated(Long orderId, Long userId,
                                     List<Long> itemIds,
                                     List<OrderCreatedEvent.OrderDetailMsg> items) {
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, itemIds, items);
        log.info("发布订单创建事件: orderId={}, userId={}, items={}", orderId, userId,
                items.stream().map(i -> i.getItemId() + "x" + i.getNum()).collect(Collectors.joining(",")));
        rabbitTemplate.convertAndSend(
                MqConstants.EXCHANGE_ORDER,
                MqConstants.ROUTING_KEY_ORDER_CREATED,
                event
        );
    }

    /**
     * 发送订单超时检查消息（延迟队列，30 分钟后消费）
     * 消息进入超时等待队列，TTL 到期后死信转发至超时检查队列
     */
    public void sendOrderTimeoutCheck(Long orderId) {
        log.info("发送订单超时检查消息: orderId={}", orderId);
        rabbitTemplate.convertAndSend(
                MqConstants.EXCHANGE_ORDER,
                MqConstants.ROUTING_KEY_ORDER_TIMEOUT,
                orderId
        );
    }
}
