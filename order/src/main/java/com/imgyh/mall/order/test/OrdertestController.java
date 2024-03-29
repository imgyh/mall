package com.imgyh.mall.order.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class OrdertestController {
    @Value("${profilename}")
    String profilename;

    @GetMapping("/order/profilename")
    String getProfileName(){
        return profilename;
    }
}
