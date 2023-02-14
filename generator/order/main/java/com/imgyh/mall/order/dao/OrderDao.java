package com.imgyh.mall.order.dao;

import com.imgyh.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 14:52:18
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
