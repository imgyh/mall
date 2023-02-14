package com.imgyh.mall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.imgyh.mall.product.entity.BrandEntity;
import com.imgyh.mall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class ProductApplicationTests {

    @Resource
    BrandService brandService;
    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
//        brandEntity.setName("小米");
//        brandService.save(brandEntity);
        brandEntity.setBrandId(1L);
        brandEntity.setDescript("xiaomi");
        brandService.updateById(brandEntity);
//        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
//        for (BrandEntity entity : list) {
//            System.out.println(entity);
//        }

    }

}
