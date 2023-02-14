package com.imgyh.mall.coupon.dao;

import com.imgyh.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 14:25:50
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
