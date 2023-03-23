package com.imgyh.mall.gateway.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class GatewaytestController {
    @Value("${profilename}")
    String profilename;

    @GetMapping("/gateway/profilename")
    String getProfileName(){
        return profilename;
    }
}
