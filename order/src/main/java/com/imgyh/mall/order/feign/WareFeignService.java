package com.imgyh.mall.order.feign;

import com.imgyh.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @ClassName : WareFeignService
 * @Package : com.imgyh.mall.order.feign
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/4/5 16:01
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/
@FeignClient("ware")
public interface WareFeignService {
    @PostMapping(value = "ware/waresku/hasStock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds);
}
