package com.imgyh.mall.coupon.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class CoupontestController {
    @Value("${profilename}")
    String profilename;

    @GetMapping("/coupon/profilename")
    String getProfileName(){
        return profilename;
    }
}
