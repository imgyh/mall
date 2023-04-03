package com.imgyh.mall.cart.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RefreshScope
public class CarttestController {
    @Value("${profilename}")
    String profilename;

    @GetMapping("/cart/profilename")
    String getProfileName(){
        return profilename;
    }
}
