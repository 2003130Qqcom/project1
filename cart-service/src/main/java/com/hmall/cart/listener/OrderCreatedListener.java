package com.hmall.cart.listener;

import com.hmall.cart.service.ICartService;
import com.hmall.common.amqp.MqConstants;
import com.hmall.common.amqp.message.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单创建事件消费者 — 清理购物车已购买商品
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final ICartService cartService;

    @RabbitListener(queues = MqConstants.QUEUE_ORDER_CREATED_CART)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("收到订单创建事件，开始清理购物车: orderId={}, userId={}", event.getOrderId(), event.getUserId());
        try {
            cartService.removeByItemIds(event.getUserId(), event.getItemIds());
            log.info("购物车清理成功: orderId={}, itemIds={}", event.getOrderId(), event.getItemIds());
        } catch (Exception e) {
            log.error("购物车清理失败: orderId={}", event.getOrderId(), e);
            throw e;
        }
    }
}
