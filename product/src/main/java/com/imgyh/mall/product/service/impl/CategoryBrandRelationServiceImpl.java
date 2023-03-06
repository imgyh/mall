package com.imgyh.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.common.utils.Query;
import com.imgyh.mall.product.dao.BrandDao;
import com.imgyh.mall.product.dao.CategoryBrandRelationDao;
import com.imgyh.mall.product.dao.CategoryDao;
import com.imgyh.mall.product.entity.BrandEntity;
import com.imgyh.mall.product.entity.CategoryBrandRelationEntity;
import com.imgyh.mall.product.entity.CategoryEntity;
import com.imgyh.mall.product.service.CategoryBrandRelationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Resource
    private CategoryDao categoryDao;

    @Resource
    private BrandDao brandDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    // 保存分类与品牌关系，并且保存 brand_name catelog_name 这两个字段，默认前端不会传入这两个字段
    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        // 根据 brandId catelogId 查出 brand_name catelog_name
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        BrandEntity brandEntity = brandDao.selectById(brandId);
        categoryBrandRelation.setCatelogName(categoryEntity.getName());
        categoryBrandRelation.setBrandName(brandEntity.getName());
        // 保存
        this.save(categoryBrandRelation);

    }

    // 更新品牌名
    @Override
    public void updateBrandName(Long brandId, String name) {
        CategoryBrandRelationEntity entity = new CategoryBrandRelationEntity();
        entity.setBrandName(name);
        entity.setBrandId(brandId);
        baseMapper.update(entity,
                new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
    }

    @Override
    public void updateCatelogName(Long catId, String name) {
        CategoryBrandRelationEntity entity = new CategoryBrandRelationEntity();
        entity.setCatelogId(catId);
        entity.setCatelogName(name);
        baseMapper.update(entity,
                new UpdateWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
    }

    /**
     * 根据分类id, 查出该分类下所哟品牌详细信息
     *
     * @param catId
     * @return
     */
    @Override
    public List<BrandEntity> brandlist(Long catId) {
        // 根据catId查brandId
        List<CategoryBrandRelationEntity> relationEntityList = this.baseMapper.selectList(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));

        List<Long> brandIdList = relationEntityList.stream().map((item) -> {
            return item.getBrandId();
        }).collect(Collectors.toList());
        // 根据brandId集合查询详细信息
        List<BrandEntity> brandEntities = brandDao.selectBatchIds(brandIdList);

        return brandEntities;
    }

}