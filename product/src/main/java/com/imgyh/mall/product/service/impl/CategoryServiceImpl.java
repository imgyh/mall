package com.imgyh.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.common.utils.Query;
import com.imgyh.mall.product.dao.CategoryDao;
import com.imgyh.mall.product.entity.CategoryEntity;
import com.imgyh.mall.product.service.CategoryBrandRelationService;
import com.imgyh.mall.product.service.CategoryService;
import com.imgyh.mall.product.vo.Catelog2Vo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;
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

    @Override
    public void removeCategoryByIds(List<Long> asList) {
        // TODO 检查当前删除菜单是否在别的地方引用
        // 逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public List<Long> findCatelogPath(Long catelogId) {
        List<Long> list = new ArrayList<>();
        this.findParent(catelogId, list);
        // 找到的父分类id是[子,父,爷], 将其逆序
        Collections.reverse(list);

        return list;
    }

    // 更新自己这张表和其他相关的表
    @Transactional
    @Override
    public void updateAllRelatedTable(CategoryEntity category) {
        // 更新自己这张表
        this.updateById(category);
        // TODO 同步更新其他关联表中的数据
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationService.updateCatelogName(category.getCatId(), category.getName());
        }
    }

    /**
     * 查找一级分类
     * @return
     */
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> categoryEntities = this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    /**
     * 三级分类嵌套
     * @return
     */
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        List<CategoryEntity> categoryEntities = this.list();
        List<CategoryEntity> l1 = categoryEntities.stream().filter(categoryEntity -> {
            return categoryEntity.getCatLevel() == 1;
        }).collect(Collectors.toList());

        Map<String, List<Catelog2Vo>> map = l1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<Catelog2Vo> list = categoryEntities.stream().filter(categoryEntity2 -> {
                return categoryEntity2.getCatLevel() == 2 && categoryEntity2.getParentCid() == v.getCatId();
            }).map(categoryEntity2 -> {
                Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, categoryEntity2.getCatId().toString(), categoryEntity2.getName());
                List<Catelog2Vo.Category3Vo> collect = categoryEntities.stream().filter(categoryEntity3 -> {
                    return categoryEntity3.getCatLevel() == 3 && categoryEntity3.getParentCid() == categoryEntity2.getCatId();
                }).map(categoryEntity3 -> {
                    Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo(categoryEntity2.getCatId().toString(), categoryEntity3.getCatId().toString(), categoryEntity3.getName());
                    return category3Vo;
                }).collect(Collectors.toList());
                catelog2Vo.setCatalog3List(collect);
                return catelog2Vo;
            }).collect(Collectors.toList());
            return list;
        }));
        return map;
    }

    // 递归查找父分类的id
    private void findParent(Long catelogId, List<Long> list) {
        // 收集id
        list.add(catelogId);
        CategoryEntity category = this.getById(catelogId);
        if (category.getParentCid() != 0){
            findParent(category.getParentCid(),list);
        }
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