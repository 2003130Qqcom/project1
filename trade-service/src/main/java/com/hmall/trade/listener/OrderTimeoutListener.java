package com.hmall.trade.listener;

import com.hmall.common.amqp.MqConstants;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单超时未支付消费者
 * 监听延迟队列（30 分钟 TTL），检查并取消超时未支付订单
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutListener {

    private final IOrderService orderService;

    @RabbitListener(queues = MqConstants.QUEUE_ORDER_TIMEOUT_CHECK)
    public void handleOrderTimeout(Long orderId) {
        log.info("收到订单超时检查消息: orderId={}", orderId);
        try {
            Order order = orderService.getById(orderId);
            if (order == null) {
                log.warn("订单不存在: orderId={}", orderId);
                return;
            }
            // 状态 1=未支付, 2=已支付
            if (order.getStatus() == 1) {
                // 取消订单（状态置为 5 表示已取消）
                order.setStatus(5);
                orderService.updateById(order);
                log.info("订单已超时取消: orderId={}", orderId);
                // TODO: 可发送消息通知商品服务恢复库存
            } else {
                log.info("订单已支付，无需取消: orderId={}, status={}", orderId, order.getStatus());
            }
        } catch (Exception e) {
            log.error("订单超时处理失败: orderId={}", orderId, e);
            throw e;
        }
    }
}
