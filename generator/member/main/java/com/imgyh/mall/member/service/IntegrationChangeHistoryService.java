package com.imgyh.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imgyh.common.utils.PageUtils;
import com.imgyh.mall.member.entity.IntegrationChangeHistoryEntity;

import java.util.Map;

/**
 * 积分变化历史记录
 *
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 14:34:16
 */
public interface IntegrationChangeHistoryService extends IService<IntegrationChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

