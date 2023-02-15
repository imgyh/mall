package com.imgyh.mall.member.test;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

// product 为调用者的 spring.application.name
@FeignClient("product")
// 交给 Bean 容器 管理
@Component
public interface ProductFeignService {
    @GetMapping("/product")
    String product();
}
