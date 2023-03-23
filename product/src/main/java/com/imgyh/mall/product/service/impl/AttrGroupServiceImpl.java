package com.imgyh.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.common.utils.Query;
import com.imgyh.mall.product.dao.AttrGroupDao;
import com.imgyh.mall.product.entity.AttrEntity;
import com.imgyh.mall.product.entity.AttrGroupEntity;
import com.imgyh.mall.product.service.AttrAttrgroupRelationService;
import com.imgyh.mall.product.service.AttrGroupService;
import com.imgyh.mall.product.vo.AttrgroupWithAttrVo;
import com.imgyh.mall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Resource
    private AttrAttrgroupRelationService attrAttrgroupRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    /**
     *
     * 根据传入的 catelogId(三级分类中的第三级子分类的id) 查询该 catelogId 下所有的属性值, 并分页返回
     * 如果 catelogId==0则返回全部属性, 同时能模糊查询
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String)params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        // select * from pms_attr_group where catelog_id=? and (attr_group_id=key or attr_group_name like key)
        // 这个可以根据传入的 key 进一步查找
        if (key != null && !key.isEmpty()){
            wrapper.and((obj)->{
                obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }
        if (catelogId != 0){
            wrapper.eq("catelog_id", catelogId);
        }

        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 获取分类下所有分组&关联属性
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrgroupWithAttrVo> getAttrgroupWithAttr(Long catelogId) {
        // 查catelogId下的属性分组
        List<AttrGroupEntity> attrGroupEntityList = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<AttrgroupWithAttrVo> collect = attrGroupEntityList.stream().map((item) -> {
            AttrgroupWithAttrVo attrgroupWithAttrVo = new AttrgroupWithAttrVo();
            BeanUtils.copyProperties(item, attrgroupWithAttrVo);
            // 查每个属性分组下的属性
            List<AttrEntity> attrEntities = attrAttrgroupRelationService.listAttrRelation(item.getAttrGroupId());
            attrgroupWithAttrVo.setAttrs(attrEntities);
            return attrgroupWithAttrVo;
        }).collect(Collectors.toList());

        return collect;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {

        return baseMapper.getAttrGroupWithAttrsBySpuId(spuId,catalogId);
    }

}