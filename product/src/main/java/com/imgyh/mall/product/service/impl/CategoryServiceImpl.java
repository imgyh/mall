package com.imgyh.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.common.utils.Query;
import com.imgyh.mall.product.dao.CategoryDao;
import com.imgyh.mall.product.entity.CategoryEntity;
import com.imgyh.mall.product.service.CategoryBrandRelationService;
import com.imgyh.mall.product.service.CategoryService;
import com.imgyh.mall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    // 获取树形结构的分类
    @Override
    public List<CategoryEntity> listTree() {
        // 1. 获取所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        // 2. 将分类转化成树形结构
        List<CategoryEntity> levelCategory = entities.stream()
                .filter((entity) -> {
                    return entity.getParentCid() == 0;
                }).map((entity) -> {
                    entity.setChildren(this.getChildren(entity, entities));
                    return entity;
                }).sorted((entity1, entity2) -> {
                    return entity1.getSort() - entity2.getSort();
                }).collect(Collectors.toList());

        return levelCategory;
    }

    @Override
    public void removeCategoryByIds(List<Long> asList) {
        // TODO 检查当前删除菜单是否在别的地方引用
        // 逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public List<Long> findCatelogPath(Long catelogId) {
        List<Long> list = new ArrayList<>();
        this.findParent(catelogId, list);
        // 找到的父分类id是[子,父,爷], 将其逆序
        Collections.reverse(list);

        return list;
    }

    // 更新自己这张表和其他相关的表
    @Transactional
    @Override
    public void updateAllRelatedTable(CategoryEntity category) {
        // 更新自己这张表
        this.updateById(category);
        // TODO 同步更新其他关联表中的数据
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationService.updateCatelogName(category.getCatId(), category.getName());
        }
    }

    /**
     * 查找一级分类
     *
     * @return
     */
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> categoryEntities = this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    /**
     * 三级分类嵌套
     *
     * @return
     */
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        /**
         * 1、空结果缓存：解决缓存穿透问题
         * 2、设置过期时间(加随机值)：解决缓存雪崩
         * 3、加锁：解决缓存击穿问题
         */
        // 1.先去redis中查有没有
        String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            // 2. redis中没有就去数据库中查, 并存到redis中
            // 2.1. 无锁的情况
            // Map<String, List<Catelog2Vo>> catalogJsonFromDB = getCatalogJsonFromDB();
            // String s = JSON.toJSONString(catalogJsonFromDB);
            // redisTemplate.opsForValue().set("catalogJSON",s);
            // 2.2 本地锁
            // Map<String, List<Catelog2Vo>> catalogJsonFromDB = getCatalogJsonFromDBWithLocalLock();
            // 2.3 redis分布式锁
            Map<String, List<Catelog2Vo>> catalogJsonFromDB = getCatalogJsonFromDBWithRedisLock();

            return catalogJsonFromDB;
        }
        // 3. redis中有，转化为相应类型
        Map<String, List<Catelog2Vo>> map = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return map;
    }

    /**
     * redis分布式锁
     *
     * @return
     */
    private Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedisLock() {
        // 1. 占分布式锁, setnxex("lock","1111",30s) 设置锁并设置过期时间保证原子操作
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);

        if (lock) {
            System.out.println("获取分布式锁成功...");
            // 2. 加锁成功...执行业务
            Map<String, List<Catelog2Vo>> catalogJsonFromDB;
            try {
                // 1). 拿到锁后再去缓存中查一次。如果大量请求都在等待锁释放, 拿到锁后, 如果不去缓存中再拿一次, 还是有大量请求去请求数据库
                String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
                if (!StringUtils.isEmpty(catalogJSON)) {
                    Map<String, List<Catelog2Vo>> map = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
                    });
                    return map;
                }

                // 2). 查询数据库
                catalogJsonFromDB = getCatalogJsonFromDB();

                // 3). 从数据库中查到的数据存到redis中
                // 要锁到这里, 如果只锁到查询数据库完成, 在第一个请求查询完数据库，把数据放入缓存时会花费时间
                // 这段时间第二个请求拿到锁, 发现缓存中还是没有数据，又会再去查询数据库，这将增加一次查询数据库
                String s = JSON.toJSONString(catalogJsonFromDB);
                redisTemplate.opsForValue().set("catalogJSON", s);

            } finally {
                // 3. 删除锁, 由于没设置过期时间，如果正好在这之前机器断电，无法执行删除锁，会造成死锁
                // 判断是自己加的锁 + 删除锁 要保证原子操作
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }
            return catalogJsonFromDB;
        } else {
            System.out.println("获取分布式锁失败...等待重试...");
            // 4.加锁失败...重试, 休眠100ms重试
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDBWithLocalLock(); // 自旋的方式
        }
    }

    /**
     * 本地锁
     *
     * @return
     */
    private Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithLocalLock() {
        // 只要是同一把锁，就能锁住这个锁的所有线程
        // synchronized (this)：SpringBoot所有的组件在容器中都是单例的。
        // 本地锁：synchronized，JUC（Lock),在分布式情况下，想要锁住所有，必须使用分布式锁
        synchronized (this) {
            // 1. 拿到锁后再去缓存中查一次。如果大量请求都在等待锁释放, 拿到锁后, 如果不去缓存中再拿一次, 还是有大量请求去请求数据库
            String catalogJSON = redisTemplate.opsForValue().get("catalogJSON");
            if (!StringUtils.isEmpty(catalogJSON)) {
                Map<String, List<Catelog2Vo>> map = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
                });
                return map;
            }

            // 2. 查询数据库
            Map<String, List<Catelog2Vo>> catalogJsonFromDB = getCatalogJsonFromDB();

            // 3. 从数据库中查到的数据存到redis中
            // 要锁到这里, 如果只锁到查询数据库完成, 在第一个请求查询完数据库，把数据放入缓存时会花费时间
            // 这段时间第二个请求拿到锁, 发现缓存中还是没有数据，又会再去查询数据库，这将增加一次查询数据库
            String s = JSON.toJSONString(catalogJsonFromDB);
            redisTemplate.opsForValue().set("catalogJSON", s);
            return catalogJsonFromDB;
        }
    }

    /**
     * 查询数据库, 并解析为相应的格式
     *
     * @return
     */
    private Map<String, List<Catelog2Vo>> getCatalogJsonFromDB() {
        List<CategoryEntity> categoryEntities = this.list();
        List<CategoryEntity> l1 = categoryEntities.stream().filter(categoryEntity -> {
            return categoryEntity.getCatLevel() == 1;
        }).collect(Collectors.toList());

        Map<String, List<Catelog2Vo>> map = l1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<Catelog2Vo> list = categoryEntities.stream().filter(categoryEntity2 -> {
                return categoryEntity2.getCatLevel() == 2 && categoryEntity2.getParentCid() == v.getCatId();
            }).map(categoryEntity2 -> {
                Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, categoryEntity2.getCatId().toString(), categoryEntity2.getName());
                List<Catelog2Vo.Category3Vo> collect = categoryEntities.stream().filter(categoryEntity3 -> {
                    return categoryEntity3.getCatLevel() == 3 && categoryEntity3.getParentCid() == categoryEntity2.getCatId();
                }).map(categoryEntity3 -> {
                    Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo(categoryEntity2.getCatId().toString(), categoryEntity3.getCatId().toString(), categoryEntity3.getName());
                    return category3Vo;
                }).collect(Collectors.toList());
                catelog2Vo.setCatalog3List(collect);
                return catelog2Vo;
            }).collect(Collectors.toList());
            return list;
        }));
        return map;
    }

    // 递归查找父分类的id
    private void findParent(Long catelogId, List<Long> list) {
        // 收集id
        list.add(catelogId);
        CategoryEntity category = this.getById(catelogId);
        if (category.getParentCid() != 0) {
            findParent(category.getParentCid(), list);
        }
    }


    // 递归获取某个分类的子分类
    private List<CategoryEntity> getChildren(CategoryEntity entity, List<CategoryEntity> entities) {
        List<CategoryEntity> list = entities.stream()
                .filter((e) -> {
                    return e.getParentCid().equals(entity.getCatId());
                }).map((e) -> {
                    e.setChildren(this.getChildren(e, entities));
                    return e;
                }).sorted((entity1, entity2) -> {
                    return (entity1.getSort() == null ? 0 : entity1.getSort()) - (entity2.getSort() == null ? 0 : entity2.getSort());
                }).collect(Collectors.toList());
        return list;
    }

}