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
        // ?????? ?????????????????? ??????????????????????????? ?????????

        // 1.??????????????????????????????????????????????????????????????????
        // 1.1. ?????? attrgroupId ??? pms_attr_group ????????? catelogId
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();


        // 2. ???????????????????????????????????????????????????????????????????????????
        // 2.1. ?????? catelogId ??? pms_attr_group ?????????????????????????????????id attrgroupId
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> attrgroupIdList = attrGroupEntities.stream().map((e) -> {
            return e.getAttrGroupId();
        }).collect(Collectors.toList());
        // 2.2. ?????? attrgroupId ??? pms_attr_attrgroup_relation ??????????????????????????????id attr_id
        List<AttrAttrgroupRelationEntity> relationEntityList = attrAttrgroupRelationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", attrgroupIdList));
        List<Long> needRemoveAttrIdList = relationEntityList.stream().map((e) -> {
            return e.getAttrId();
        }).collect(Collectors.toList());

        // 3. ?????? ?????????????????????
        // 3.1. ?????? catelogId ??? pms_attr ???????????????????????????????????????????????????????????????
        // ??????????????????????????????: ??????????????????????????????????????????????????????????????????
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