package com.imgyh.mall.thirdparty.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName : ThirdpartyTestController
 * @Package : com.imgyh.mall.thirdparty.test
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/2/22 19:11
 * @Version : v1.0
 * @ChangeLog * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/

@RestController
@RefreshScope
public class ThirdpartyTestController {
    @Value("${profilename}")
    String profilename;

    @GetMapping("/profilename")
    String getProfileName() {
        return profilename;
    }
}
