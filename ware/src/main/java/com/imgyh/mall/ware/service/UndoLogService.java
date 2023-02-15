package com.imgyh.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.ware.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 14:54:58
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

