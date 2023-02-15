package com.imgyh.mall.member.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RefreshScope
public class MembertestController {
    @Resource
    ProductFeignService productFeignService;

    @GetMapping("/test")
    String member(){
        return productFeignService.product();
    }

    @Value("${profilename}")
    String profilename;

    @GetMapping("/profilename")
    String getProfileName(){
        return profilename;
    }

}
