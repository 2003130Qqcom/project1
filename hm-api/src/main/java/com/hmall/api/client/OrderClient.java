package com.hmall.api.client;

import com.hmall.api.dto.OrderFormDTO;
import com.hmall.api.dto.OrderVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("order-service")
public interface OrderClient {

    @GetMapping("/orders/{id}")
    OrderVO queryOrderById(@PathVariable("id") Long orderId);

    @PostMapping("/orders")
    Long createOrder(@RequestBody OrderFormDTO orderFormDTO);

    @PutMapping("/orders/{orderId}")
    void markOrderPaySuccess(@PathVariable("orderId") Long orderId);
}
