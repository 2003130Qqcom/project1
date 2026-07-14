package com.hmall.api.client;

import com.hmall.api.dto.PayApplyDTO;
import com.hmall.api.dto.PayOrderFormDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("pay-service")
public interface PayClient {

    @PostMapping("/pay-orders")
    String applyPayOrder(@RequestBody PayApplyDTO applyDTO);

    @PostMapping("/pay-orders/{id}")
    void tryPayOrderByBalance(@PathVariable("id") Long id, @RequestBody PayOrderFormDTO payOrderFormDTO);
}
