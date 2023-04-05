package com.imgyh.mall.order.feign;

import com.imgyh.mall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @ClassName : CartFeignService
 * @Package : com.imgyh.mall.order.feign
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/4/5 15:52
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/
@FeignClient("cart")
public interface CartFeignService {
    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<OrderItemVo> getCurrentUserCartItems();
}
