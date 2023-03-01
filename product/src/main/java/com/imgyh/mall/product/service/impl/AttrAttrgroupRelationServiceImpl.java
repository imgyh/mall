package com.imgyh.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imgyh.mall.common.constant.ProductConstant;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.common.utils.Query;
import com.imgyh.mall.product.dao.AttrAttrgroupRelationDao;
import com.imgyh.mall.product.dao.AttrDao;
import com.imgyh.mall.product.dao.AttrGroupDao;
import com.imgyh.mall.product.entity.AttrAttrgroupRelationEntity;
import com.imgyh.mall.product.entity.AttrEntity;
import com.imgyh.mall.product.entity.AttrGroupEntity;
import com.imgyh.mall.product.service.AttrAttrgroupRelationService;
import com.imgyh.mall.product.service.AttrService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Resource
    private AttrDao attrDao;

    @Resource
    private AttrGroupDao attrGroupDao;

    @Resource
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Resource
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<AttrEntity> listAttrRelation(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> entities = this.list(
                new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        List<Long> list = entities.stream().map((item) -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        if (list == null || list.size() == 0) {
            return null;
        }
        List<AttrEntity> attrEntities = attrDao.selectBatchIds(list);
        return attrEntities;
    }

    @Override
    public void deleteAttrRelation(List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntityList) {
        this.baseMapper.deleteAttrRelation(attrAttrgroupRelationEntityList);
    }

    @Override
    public PageUtils listNoAttrRelation(Map<String, Object> params, Long attrgroupId) {
        // 要在 属性组所在的 第三级分类的前提下 找属性

        // 1.当前分组只能关联自己所属的分类里面的所有属性
        // 1.1. 根据 attrgroupId 在 pms_attr_group 表查找 catelogId
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();


        // 2. 当前分组只能关联自己分类下的所有分组没有引用的属性
        // 2.1. 根据 catelogId 在 pms_attr_group 表中找到所有的属性分组id attrgroupId
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> attrgroupIdList = attrGroupEntities.stream().map((e) -> {
            return e.getAttrGroupId();
        }).collect(Collectors.toList());
        // 2.2. 根据 attrgroupId 在 pms_attr_attrgroup_relation 找到已经被关联的属性id attr_id
        List<AttrAttrgroupRelationEntity> relationEntityList = attrAttrgroupRelationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", attrgroupIdList));
        List<Long> needRemoveAttrIdList = relationEntityList.stream().map((e) -> {
            return e.getAttrId();
        }).collect(Collectors.toList());

        // 3. 移除 已经关联的属性
        // 3.1. 根据 catelogId 在 pms_attr 表中找到所属于该分类下的所有符合条件的属性
        // 符合条件的属性要去除: 已经被关联的属性，销售属性不要只需要基础属性
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).ne("attr_type", ProductConstant.AttrType.SALE_TYPE);
        if (needRemoveAttrIdList != null && needRemoveAttrIdList.size() > 0) {
            wrapper.notIn("attr_id", needRemoveAttrIdList);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = attrService.page(new Query<AttrEntity>().getPage(params), wrapper);

        PageUtils pageUtils = new PageUtils(page);

        return pageUtils;

    }

    @Override
    public void saveAttrRelation(List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntityList) {
        this.saveBatch(attrAttrgroupRelationEntityList);
    }

}