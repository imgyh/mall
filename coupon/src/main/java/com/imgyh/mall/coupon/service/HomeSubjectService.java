package com.imgyh.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imgyh.common.utils.PageUtils;
import com.imgyh.mall.coupon.entity.HomeSubjectEntity;

import java.util.Map;

/**
 * 首页专题表【jd首页下面很多专题，每个专题链接新的页面，展示专题商品信息】
 *
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 14:25:50
 */
public interface HomeSubjectService extends IService<HomeSubjectEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

