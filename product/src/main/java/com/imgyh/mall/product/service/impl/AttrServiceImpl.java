package com.imgyh.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.common.utils.Query;
import com.imgyh.mall.product.dao.AttrAttrgroupRelationDao;
import com.imgyh.mall.product.dao.AttrDao;
import com.imgyh.mall.product.dao.AttrGroupDao;
import com.imgyh.mall.product.dao.CategoryDao;
import com.imgyh.mall.product.entity.AttrAttrgroupRelationEntity;
import com.imgyh.mall.product.entity.AttrEntity;
import com.imgyh.mall.product.entity.AttrGroupEntity;
import com.imgyh.mall.product.entity.CategoryEntity;
import com.imgyh.mall.product.service.AttrService;
import com.imgyh.mall.product.service.CategoryService;
import com.imgyh.mall.product.vo.AttrResponseVo;
import com.imgyh.mall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Resource
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Resource
    private AttrGroupDao attrGroupDao;

    @Resource
    private CategoryDao categoryDao;

    @Resource
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttrVo(AttrVo attrVo) {
        //保存到Attr表
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        this.save(attrEntity);
        // 保存attr与attr group的关系
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
        attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
        attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
        attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
    }

    @Override
    public PageUtils queryBaseListPage(Map<String, Object> params, Long catelogId) {
        // 查找Attr表中的信息
        String key = (String)params.get("key");
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();

        // 这个可以根据传入的 key 进一步查找
        if (key != null && !key.isEmpty()){
            wrapper.and((obj)->{
                obj.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        if (catelogId != 0){
            wrapper.eq("catelog_id", catelogId);
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        PageUtils pageUtils = new PageUtils(page);

        List<AttrEntity> records = page.getRecords();

        List<AttrResponseVo> list = records.stream().map(
                (attrEntity) -> {
                    AttrResponseVo attrResponseVo = new AttrResponseVo();
                    BeanUtils.copyProperties(attrEntity, attrResponseVo);

                    // 再根据attr_id 查找pms_attr_attrgroup_relation中的 attr_group_id，属性所属于哪个分组
                    AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(
                            new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                    // 再去 pms_attr_group 表找 attr_group_name
                    if (attrAttrgroupRelationEntity != null){
                        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                        attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                    }
                    // 查找所属分类 在 pms_category 表中根据 catelogid 查找catelog_name
                    CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
                    if (categoryEntity != null) {
                        attrResponseVo.setCatelogName(categoryEntity.getName());
                    }
                    return attrResponseVo;
                }
        ).collect(Collectors.toList());

        pageUtils.setList(list);
        return pageUtils;
    }

    @Override
    public AttrResponseVo getAttrResponse(Long attrId) {
        // 获取 attr 表相关信息
        AttrEntity attr = this.getById(attrId);
        AttrResponseVo attrResponseVo = new AttrResponseVo();
        BeanUtils.copyProperties(attr,attrResponseVo);
        // 获取 attr属性属于哪个属性组
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(
                new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attrId));

        if (attrAttrgroupRelationEntity != null) {
            Long attrGroupId = attrAttrgroupRelationEntity.getAttrGroupId();
            attrResponseVo.setAttrGroupId(attrGroupId);
            AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
            if (attrGroupEntity != null) {
                attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }
        }

        // 获取 attr属性属于哪个分类
        CategoryEntity categoryEntity = categoryDao.selectById(attr.getCatelogId());
        if (categoryEntity != null) {
            attrResponseVo.setCatelogName(categoryEntity.getName());
            List<Long> catelogPath = categoryService.findCatelogPath(attr.getCatelogId());
            attrResponseVo.setCatelogPath(catelogPath);
        }

        return attrResponseVo;
    }

    @Override
    public void updateRelation(AttrVo attr) {
        // 更新自己 attr表
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr,attrEntity);
        this.updateById(attrEntity);
        // 更新所属 属性组 更新 attrAttrGroup表
        // 有可能某个属性 没有设置分组，修改的时候需要插入一条属性与属性分组的关联记录
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
        attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());
        attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
        QueryWrapper<AttrAttrgroupRelationEntity> wrapper = new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId());
        Long count = attrAttrgroupRelationDao.selectCount(wrapper);
        if (count == 0){
            // 插入
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }else {
            // 修改
            attrAttrgroupRelationDao.update(attrAttrgroupRelationEntity, wrapper);
        }

    }

}