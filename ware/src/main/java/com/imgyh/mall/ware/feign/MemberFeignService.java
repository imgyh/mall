package com.imgyh.mall.ware.feign;

import com.imgyh.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @ClassName : MemberFeignService
 * @Package : com.imgyh.mall.ware.feign
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/4/5 16:20
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/
@FeignClient("member")
public interface MemberFeignService {
    @RequestMapping("member/memberreceiveaddress/info/{id}")
    public R addrInfo(@PathVariable("id") Long id);
}
