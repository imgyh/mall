package com.imgyh.mall.order.dao;

import com.imgyh.mall.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 14:52:18
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {
	
}
