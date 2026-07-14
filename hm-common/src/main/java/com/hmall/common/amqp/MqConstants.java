package com.hmall.common.amqp;

/**
 * RabbitMQ 常量定义
 * 包含交换机、队列、路由键的名称
 */
public class MqConstants {

    // ========== 交换机 ==========
    /** 业务主交换机（Direct 类型） */
    public static final String EXCHANGE_ORDER = "hmall.direct";

    // ========== 订单创建事件队列 ==========
    /** 订单创建事件 — 商品服务扣库存 */
    public static final String QUEUE_ORDER_CREATED_ITEM = "hmall.queue.order.created.item";
    /** 订单创建事件 — 购物车服务清理 */
    public static final String QUEUE_ORDER_CREATED_CART = "hmall.queue.order.created.cart";
    /** 订单创建路由键 */
    public static final String ROUTING_KEY_ORDER_CREATED = "order.created";

    // ========== 订单支付成功事件 ==========
    /** 订单支付成功 — 交易服务更新订单状态 */
    public static final String QUEUE_ORDER_PAID = "hmall.queue.order.paid";
    /** 订单支付成功路由键 */
    public static final String ROUTING_KEY_ORDER_PAID = "order.paid";

    // ========== 订单超时未支付（延迟队列方案） ==========
    /** 订单超时等待队列（TTL = 30min，到期后转至死信交换机） */
    public static final String QUEUE_ORDER_TIMEOUT_WAIT = "hmall.queue.order.timeout.wait";
    /** 订单超时检查队列 */
    public static final String QUEUE_ORDER_TIMEOUT_CHECK = "hmall.queue.order.timeout.check";
    /** 订单超时检查路由键 */
    public static final String ROUTING_KEY_ORDER_TIMEOUT_CHECK = "order.timeout.check";
    /** 订单超时路由键（发往等待队列，30 分钟后被死信转发） */
    public static final String ROUTING_KEY_ORDER_TIMEOUT = "order.timeout";
    /** 订单超时等待时间（毫秒） */
    public static final long ORDER_TIMEOUT_TTL = 30 * 60 * 1000L; // 30 分钟

    // ========== 库存扣减失败回滚 ==========
    /** 库存不足回滚 — 取消订单 */
    public static final String QUEUE_STOCK_DEDUCT_FAILED = "hmall.queue.stock.deduct.failed";
    /** 库存不足回滚路由键 */
    public static final String ROUTING_KEY_STOCK_DEDUCT_FAILED = "stock.deduct.failed";
}
