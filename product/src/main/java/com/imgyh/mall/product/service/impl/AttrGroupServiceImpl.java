package com.imgyh.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.common.utils.Query;
import com.imgyh.mall.product.dao.AttrGroupDao;
import com.imgyh.mall.product.entity.AttrGroupEntity;
import com.imgyh.mall.product.service.AttrGroupService;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

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

}