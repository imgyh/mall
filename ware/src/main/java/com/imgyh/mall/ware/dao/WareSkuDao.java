package com.imgyh.mall.ware.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imgyh.mall.ware.entity.WareSkuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 14:54:57
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Long getSkuStock(@Param("skuId") Long skuId);

    List<Long> listWareIdHasSkuStock(@Param("skuId") Long skuId);


    Long lockSkuStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);

    void unlockStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);
}
