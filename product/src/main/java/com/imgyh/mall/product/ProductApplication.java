package com.imgyh.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession  //整合redis作为session存储
@SpringBootApplication
// mapper扫描, dao加了@Mapper 可不配
@MapperScan("com.imgyh.mall.product.dao")
// 服务注册
@EnableDiscoveryClient
// Feign
@EnableFeignClients
// 缓存框架
@EnableCaching
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }

}
