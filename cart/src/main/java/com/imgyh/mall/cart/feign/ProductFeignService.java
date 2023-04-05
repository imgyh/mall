package com.imgyh.mall.cart.feign;

import com.imgyh.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @ClassName : ProductFeignService
 * @Package : com.imgyh.mall.cart.feign
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/4/3 15:06
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/
@FeignClient("product")
public interface ProductFeignService {
    @RequestMapping("product/skuinfo/info/{skuId}")
    //@RequiresPermissions("product:skuinfo:info")
    public R getSkuinfo(@PathVariable("skuId") Long skuId);

    @GetMapping("product/skusaleattrvalue/stringlist/{skuId}")
    public List<String> getSkuSaleAttrValues(@PathVariable("skuId") Long skuId);

    @GetMapping("product/skuinfo/{skuId}/price")
    public R getPrice(@PathVariable("skuId") Long skuId);
}
