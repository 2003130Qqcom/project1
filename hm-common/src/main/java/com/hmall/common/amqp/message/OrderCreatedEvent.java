package com.hmall.common.amqp.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单创建事件消息
 * 订单创建成功后发送，由商品服务和购物车服务消费
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    /** 订单ID */
    private Long orderId;
    /** 用户ID */
    private Long userId;
    /** 商品ID列表（用于清理购物车） */
    private List<Long> itemIds;
    /** 订单明细（含商品ID和数量，用于扣减库存） */
    private List<OrderDetailMsg> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDetailMsg {
        private Long itemId;
        private Integer num;
    }
}
