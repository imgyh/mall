package com.imgyh.mall.seckill.service;


import com.imgyh.mall.seckill.to.SecKillSkuRedisTo;

import java.util.List;


public interface SeckillService {
    void uploadSeckillSkuLatest3Days();


    List<SecKillSkuRedisTo> getCurrentSeckillSkus();


    SecKillSkuRedisTo getSkuSeckillInfo(Long skuId);


    String kill(String killId, String key, Integer num);


}
