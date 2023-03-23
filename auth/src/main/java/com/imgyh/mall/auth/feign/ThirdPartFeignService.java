package com.imgyh.mall.auth.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @ClassName : ThirdPartFeignService
 * @Package : com.imgyh.mall.auth.feign
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/3/23 19:15
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/
@FeignClient("thirdparty")
public interface ThirdPartFeignService {
    @GetMapping("/thirdparty/sms")
    public void sendSms(@RequestParam("phone") String phone, @RequestParam("code")String code);
}
