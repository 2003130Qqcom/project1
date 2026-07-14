package com.hmall.common.amqp.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单支付成功事件消息
 * 支付成功后发送，由交易服务消费
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaidEvent {
    /** 订单ID */
    private Long orderId;
}
