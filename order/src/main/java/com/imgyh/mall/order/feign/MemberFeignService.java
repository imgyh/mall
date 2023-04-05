package com.imgyh.mall.order.feign;

import com.imgyh.mall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @ClassName : MemberFeignService
 * @Package : com.imgyh.mall.order.feign
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/4/5 15:32
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/
@FeignClient("member")
public interface MemberFeignService {
    @GetMapping("member/memberreceiveaddress/{memeberId}/addresses")
    public List<MemberAddressVo> getAddress(@PathVariable("memeberId") Long memeberId);
}
