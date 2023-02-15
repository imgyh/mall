package com.imgyh.mall.product.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class ProducttestController {
    @GetMapping("/product")
    String product(){
        return "product";
    }

    @Value("${profilename}")
    String profilename;

    @GetMapping("/profilename")
    String getProfileName(){
        return profilename;
    }
}
