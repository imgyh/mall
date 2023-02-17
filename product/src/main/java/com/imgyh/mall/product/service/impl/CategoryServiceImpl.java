package com.imgyh.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.common.utils.Query;
import com.imgyh.mall.product.dao.CategoryDao;
import com.imgyh.mall.product.entity.CategoryEntity;
import com.imgyh.mall.product.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    // 获取树形结构的分类
    @Override
    public List<CategoryEntity> listTree() {
        // 1. 获取所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        // 2. 将分类转化成树形结构
        List<CategoryEntity> levelCategory = entities.stream()
                .filter((entity) -> {
                    return entity.getParentCid() == 0;
                }).map((entity) -> {
                    entity.setChildren(this.getChildren(entity, entities));
                    return entity;
                }).sorted((entity1, entity2) -> {
                    return entity1.getSort() - entity2.getSort();
                }).collect(Collectors.toList());

        return levelCategory;
    }

    // 递归获取某个分类的子分类
    private List<CategoryEntity> getChildren(CategoryEntity entity, List<CategoryEntity> entities) {
        List<CategoryEntity> list = entities.stream()
                .filter((e) -> {
                    return e.getParentCid().equals(entity.getCatId());
                }).map((e) -> {
                    e.setChildren(this.getChildren(e, entities));
                    return e;
                }).sorted((entity1, entity2) -> {
                    return (entity1.getSort() == null ? 0 : entity1.getSort()) - (entity2.getSort() == null ? 0 : entity2.getSort());
                }).collect(Collectors.toList());
        return list;
    }

}