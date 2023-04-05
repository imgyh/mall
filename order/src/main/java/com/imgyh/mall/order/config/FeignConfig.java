package com.imgyh.mall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName : FeignConfig
 * @Package : com.imgyh.mall.order.config
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/4/5 15:16
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/

@Configuration
public class FeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor(){
            @Override
            public void apply(RequestTemplate template) {
                //1、RequestContextHolder拿到刚进来的这个请求
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if(attributes!=null){
                    System.out.println("RequestInterceptor线程...."+Thread.currentThread().getId());
                    HttpServletRequest request = attributes.getRequest(); //老请求
                    if(request != null){
                        //同步请求头数据，Cookie
                        String cookie = request.getHeader("Cookie");
                        //给新请求同步了老请求的cookie
                        template.header("Cookie",cookie);
                    }
                }
            }
        };
    }
}
