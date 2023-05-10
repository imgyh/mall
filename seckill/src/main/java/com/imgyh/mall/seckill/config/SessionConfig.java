package com.imgyh.mall.seckill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;



@Configuration
public class SessionConfig {
    // 默认domain作用域是当前域名，将作用域提升至父域名，实现子域session共享问题
    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();

        cookieSerializer.setDomainName("mall.gyh.im");
        cookieSerializer.setCookieName("MALLSESSION");

        return cookieSerializer;
    }

    // 使用JSON的序列化方式来序列化对象数据到redis中
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
}
