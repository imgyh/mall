package com.imgyh.mall.product.dao;

import com.imgyh.mall.product.entity.AttrAttrgroupRelationEntity;
import com.imgyh.mall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 12:40:23
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    void deleteAttrRelation(@Param("entitis") List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntityList);
}
