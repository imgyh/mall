package com.imgyh.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.common.utils.Query;
import com.imgyh.mall.product.dao.BrandDao;
import com.imgyh.mall.product.entity.BrandEntity;
import com.imgyh.mall.product.service.BrandService;
import com.imgyh.mall.product.service.CategoryBrandRelationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {
    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 增加根据 key 模糊查询
        String key = (String) params.get("key");
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<BrandEntity>();
        // select * from pms_brand where brand_id=key or name like key

        if (key != null && !key.isEmpty()) {
            wrapper.eq("brand_id", key).or().like("name", key);
        }

        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    // 更新自己这张表和其他相关的表
    @Transactional //事务
    @Override
    public void updateAllRelatedTable(BrandEntity brand) {
        // 更新自己这张表
        this.updateById(brand);
        // TODO 同步更新其他关联表中的数据
        if (!StringUtils.isEmpty(brand.getName())) {
            categoryBrandRelationService.updateBrandName(brand.getBrandId(), brand.getName());
        }
    }

    @Override
    public List<BrandEntity> getBrandsByIds(List<Long> brandIds) {

        return baseMapper.selectList(new QueryWrapper<BrandEntity>().in("brand_id",brandIds));
    }

}