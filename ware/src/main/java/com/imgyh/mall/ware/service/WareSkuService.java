package com.imgyh.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imgyh.mall.common.to.SkuHasStockVo;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 14:54:57
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);
}

