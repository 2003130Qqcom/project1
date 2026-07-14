package com.hmall.item.listener;

import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.amqp.MqConstants;
import com.hmall.common.amqp.message.OrderCreatedEvent;
import com.hmall.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单创建事件消费者 — 扣减商品库存
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final ItemService itemService;

    @RabbitListener(queues = MqConstants.QUEUE_ORDER_CREATED_ITEM)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("收到订单创建事件，开始扣减库存: orderId={}", event.getOrderId());
        try {
            // 将事件消息中的商品信息转换为 OrderDetailDTO
            List<OrderDetailDTO> items = event.getItems().stream()
                    .map(d -> new OrderDetailDTO().setItemId(d.getItemId()).setNum(d.getNum()))
                    .collect(Collectors.toList());

            itemService.deductStock(items);
            log.info("库存扣减成功: orderId={}, items={}", event.getOrderId(),
                    items.stream().map(i -> i.getItemId() + "x" + i.getNum())
                            .collect(Collectors.joining(",")));
        } catch (Exception e) {
            log.error("库存扣减失败: orderId={}", event.getOrderId(), e);
            // TODO: 可发送消息通知交易服务回滚订单
            throw e;
        }
    }
}
