package com.imgyh.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imgyh.common.utils.PageUtils;
import com.imgyh.mall.coupon.entity.SkuLadderEntity;

import java.util.Map;

/**
 * 商品阶梯价格
 *
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 14:25:50
 */
public interface SkuLadderService extends IService<SkuLadderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

