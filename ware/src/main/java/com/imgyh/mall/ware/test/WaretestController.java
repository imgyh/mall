package com.imgyh.mall.ware.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class WaretestController {
    @Value("${profilename}")
    String profilename;

    @GetMapping("/ware/profilename")
    String getProfileName(){
        return profilename;
    }
}
