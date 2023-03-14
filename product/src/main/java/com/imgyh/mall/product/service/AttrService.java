package com.imgyh.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.product.entity.AttrEntity;
import com.imgyh.mall.product.vo.AttrResponseVo;
import com.imgyh.mall.product.vo.AttrVo;

import java.util.List;
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

    void saveAttrVo(AttrVo attrVo);

    PageUtils queryListPage(Map<String, Object> params, Long catelogId, String attrType);

    AttrResponseVo getAttrResponse(Long attrId);

    void updateRelation(AttrVo attr);

    List<Long> selectSearchAttrs(List<Long> attrIds);
}

