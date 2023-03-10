package com.imgyh.mall.ware.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imgyh.mall.ware.entity.WareSkuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * εεεΊε­
 * 
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 14:54:57
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Long getSkuStock(@Param("skuId") Long skuId);
}
