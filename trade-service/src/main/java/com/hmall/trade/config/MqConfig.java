package com.hmall.trade.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.hmall.common.amqp.MqConstants.*;

/**
 * RabbitMQ 交换机、队列、绑定关系声明
 */
@Configuration
public class MqConfig {

    /** 业务主交换机 */
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(EXCHANGE_ORDER);
    }

    // ========== 订单创建事件 ==========

    /** 订单创建队列 — 商品服务（扣库存） */
    @Bean
    public Queue orderCreatedItemQueue() {
        return new Queue(QUEUE_ORDER_CREATED_ITEM, true);
    }

    /** 订单创建队列 — 购物车服务（清购物车） */
    @Bean
    public Queue orderCreatedCartQueue() {
        return new Queue(QUEUE_ORDER_CREATED_CART, true);
    }

    @Bean
    public Binding orderCreatedItemBinding(Queue orderCreatedItemQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderCreatedItemQueue)
                .to(orderExchange)
                .with(ROUTING_KEY_ORDER_CREATED);
    }

    @Bean
    public Binding orderCreatedCartBinding(Queue orderCreatedCartQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderCreatedCartQueue)
                .to(orderExchange)
                .with(ROUTING_KEY_ORDER_CREATED);
    }

    // ========== 订单支付成功事件 ==========

    /** 订单支付成功队列 — 交易服务（更新订单状态） */
    @Bean
    public Queue orderPaidQueue() {
        return new Queue(QUEUE_ORDER_PAID, true);
    }

    @Bean
    public Binding orderPaidBinding(Queue orderPaidQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderPaidQueue)
                .to(orderExchange)
                .with(ROUTING_KEY_ORDER_PAID);
    }

    // ========== 订单超时未支付（延迟队列 + 死信转发） ==========

    /** 订单超时等待队列 — 消息 TTL 30 分钟，过期后转死信交换机 */
    @Bean
    public Queue orderTimeoutWaitQueue() {
        Map<String, Object> args = new HashMap<>();
        // 消息过期后转发至死信交换机
        args.put("x-dead-letter-exchange", EXCHANGE_ORDER);
        // 死信转发的路由键
        args.put("x-dead-letter-routing-key", ROUTING_KEY_ORDER_TIMEOUT_CHECK);
        // 消息存活时间 30 分钟
        args.put("x-message-ttl", ORDER_TIMEOUT_TTL);
        return new Queue(QUEUE_ORDER_TIMEOUT_WAIT, true, false, false, args);
    }

    /** 订单超时等待队列绑定（消息发到此处，TTL 到期后转至死信交换机） */
    @Bean
    public Binding orderTimeoutWaitBinding(Queue orderTimeoutWaitQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderTimeoutWaitQueue)
                .to(orderExchange)
                .with(ROUTING_KEY_ORDER_TIMEOUT);
    }

    /** 订单超时检查队列 — 真正的消费队列 */
    @Bean
    public Queue orderTimeoutCheckQueue() {
        return new Queue(QUEUE_ORDER_TIMEOUT_CHECK, true);
    }

    @Bean
    public Binding orderTimeoutCheckBinding(Queue orderTimeoutCheckQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderTimeoutCheckQueue)
                .to(orderExchange)
                .with(ROUTING_KEY_ORDER_TIMEOUT_CHECK);
    }

    // ========== 库存扣减失败回滚 ==========

    /** 库存扣减失败队列 — 取消订单 */
    @Bean
    public Queue stockDeductFailedQueue() {
        return new Queue(QUEUE_STOCK_DEDUCT_FAILED, true);
    }

    @Bean
    public Binding stockDeductFailedBinding(Queue stockDeductFailedQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(stockDeductFailedQueue)
                .to(orderExchange)
                .with(ROUTING_KEY_STOCK_DEDUCT_FAILED);
    }
}
