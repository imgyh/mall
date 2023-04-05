package com.imgyh.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.ware.entity.WareInfoEntity;
import com.imgyh.mall.ware.vo.FareVo;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 14:54:57
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    FareVo getFare(Long addrId);
}

