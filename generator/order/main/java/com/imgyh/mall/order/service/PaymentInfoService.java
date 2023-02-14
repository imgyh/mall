package com.imgyh.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imgyh.common.utils.PageUtils;
import com.imgyh.mall.order.entity.PaymentInfoEntity;

import java.util.Map;

/**
 * 支付信息表
 *
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 14:52:18
 */
public interface PaymentInfoService extends IService<PaymentInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

