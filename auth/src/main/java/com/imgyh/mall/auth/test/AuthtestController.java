package com.imgyh.mall.auth.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName : AuthtestController
 * @Package : com.imgyh.mall.auth.test
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/3/23 15:31
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/
@RestController
@RefreshScope
public class AuthtestController {
    @Value("${profilename}")
    String profilename;

    @GetMapping("/auth/profilename")
    String getProfileName(){
        return profilename;
    }
}
