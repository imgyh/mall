package com.imgyh.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imgyh.common.utils.PageUtils;
import com.imgyh.mall.product.entity.AttrEntity;

import java.util.Map;

/**
 * 商品属性
 *
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 12:40:23
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

