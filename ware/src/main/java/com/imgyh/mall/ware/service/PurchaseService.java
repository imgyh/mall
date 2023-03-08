package com.imgyh.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.ware.entity.PurchaseEntity;
import com.imgyh.mall.ware.vo.MergeVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 14:54:58
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    void received(List<Long> ids);
}

