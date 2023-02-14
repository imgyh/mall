package com.imgyh.mall.product.dao;

import com.imgyh.mall.product.entity.CommentReplayEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价回复关系
 * 
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 12:40:24
 */
@Mapper
public interface CommentReplayDao extends BaseMapper<CommentReplayEntity> {
	
}
