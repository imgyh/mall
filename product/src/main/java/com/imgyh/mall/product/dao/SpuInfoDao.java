package com.imgyh.mall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imgyh.mall.product.entity.SpuInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
 * 
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 12:40:24
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {
    void updaSpuStatus(@Param("spuId") Long spuId, @Param("code") int code);
}
