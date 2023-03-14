package com.imgyh.mall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imgyh.mall.product.entity.AttrEntity;
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

    List<Long> selectSearchAttrIds(@Param("attrIds") List<Long> attrIds);
}
