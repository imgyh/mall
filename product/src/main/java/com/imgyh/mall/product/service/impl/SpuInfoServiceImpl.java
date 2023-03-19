package com.imgyh.mall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imgyh.mall.common.constant.ProductConstant;
import com.imgyh.mall.common.es.SkuEsModel;
import com.imgyh.mall.common.to.SkuHasStockVo;
import com.imgyh.mall.common.to.SkuReductionTo;
import com.imgyh.mall.common.to.SpuBoundTo;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.common.utils.Query;
import com.imgyh.mall.common.utils.R;
import com.imgyh.mall.product.dao.SpuInfoDao;
import com.imgyh.mall.product.entity.*;
import com.imgyh.mall.product.feign.CouponFeignService;
import com.imgyh.mall.product.feign.SearchFeignService;
import com.imgyh.mall.product.feign.WareFeignService;
import com.imgyh.mall.product.service.*;
import com.imgyh.mall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    SpuImagesService spuImagesService;
    @Autowired
    AttrService attrService;
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    BrandService brandService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductAttrValueService productAttrValueService;
    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((w)->{
                w.eq("id",key).or().like("spu_name",key);
            });
        }
        // status=1 and (id=1 or spu_name like xxx)
        String status = (String) params.get("status");
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",status);
        }

        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId)&&!"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId)&&!"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id",catelogId);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 新增商品
     *
     * @param spuSaveVo
     */
    @Override
    @Transactional
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        //1、保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        Date date = new Date();
        spuInfoEntity.setCreateTime(date);
        spuInfoEntity.setUpdateTime(date);
        this.save(spuInfoEntity);
        Long spuId = spuInfoEntity.getId();

        //2、保存Spu的描述图片 pms_spu_info_desc
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuId);
        spuInfoDescEntity.setDecript(String.join(",",spuSaveVo.getDecript()));
        spuInfoDescService.save(spuInfoDescEntity);

        //3、保存spu的图片集 pms_spu_images
        List<String> images = spuSaveVo.getImages();
        if (!images.isEmpty()) {
            List<SpuImagesEntity> collect = images.stream().map((image) -> {
                SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
                spuImagesEntity.setSpuId(spuId);
                spuImagesEntity.setImgUrl(image);
                return spuImagesEntity;
            }).collect(Collectors.toList());
            spuImagesService.saveBatch(collect);
        }

        //4、保存spu的规格参数;pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        if (!baseAttrs.isEmpty()) {
            List<ProductAttrValueEntity> collect = baseAttrs.stream().map((baseAttr) -> {
                ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                productAttrValueEntity.setSpuId(spuId);
                productAttrValueEntity.setAttrId(baseAttr.getAttrId());
                AttrEntity attr = attrService.getById(baseAttr.getAttrId());
                productAttrValueEntity.setAttrName(attr.getAttrName());
                productAttrValueEntity.setAttrValue(baseAttr.getAttrValues());
                productAttrValueEntity.setQuickShow(baseAttr.getShowDesc());

                return productAttrValueEntity;
            }).collect(Collectors.toList());
        }

        //5、保存spu的积分信息；gulimall_sms->sms_spu_bounds
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(spuId);
        couponFeignService.saveSpuBounds(spuBoundTo);

        //6、保存当前spu对应的所有sku信息
        List<Skus> skus = spuSaveVo.getSkus();
        if (!skus.isEmpty()){
            skus.forEach((sku)->{
                //6.1）、sku的基本信息；pms_sku_info
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setSpuId(spuId);
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                //skuInfoEntity.setSkuDesc("");
                skuInfoEntity.setSaleCount(0L);
                String defaultImg = "";
                for (Images image : sku.getImages()) {
                    if(image.getDefaultImg() == 1){
                        defaultImg = image.getImgUrl();
                    }
                }
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.save(skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();

                //6.2）、sku的图片信息；pms_sku_image
                List<SkuImagesEntity> collect = sku.getImages().stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(image.getImgUrl());
                    skuImagesEntity.setDefaultImg(image.getDefaultImg());
                    return skuImagesEntity;
                }).filter(image->{
                    //返回true就是需要，false就是剔除
                    return !StringUtils.isEmpty(image.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(collect);

                //6.3）、sku的销售属性信息：pms_sku_sale_attr_value
                List<Attr> attr = sku.getAttr();
                List<SkuSaleAttrValueEntity> collect1 = attr.stream().map((a) -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(collect1);

                //6.4）、sku的优惠、满减等信息；gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if(skuReductionTo.getFullCount() >0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1){
                    couponFeignService.saveSkuReduction(skuReductionTo);
                }
            });
        }

    }

    // 上架商品
    @Override
    public void up(Long spuId) {
        //1、查出当前spuId对应的所有sku信息,品牌的名字
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));

        // 5.查出当前sku的所有可以被用来检索的规格属性
        // 5.1 查出spuId下的所有属性值
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseListforspu(spuId);
        // 5.2 获取所有的属性id
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());
        // 5.3 根据属性id看是否能被检索, 获取到spuId下的attrid所有能被检索的id pms_attr表 -> search_type == 1 能被检索
        List<Long> searchAttrIds = attrService.selectSearchAttrs(attrIds);
        //转换为Set集合
        Set<Long> idSet = searchAttrIds.stream().collect(Collectors.toSet());
        // 5.4 将所有能检索到的attr保存起来
        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());

        // 2. 发送远程调用，库存系统查询是否有库存
        List<Long> skuIdList = skuInfoEntities.stream()
                .map(SkuInfoEntity::getSkuId)
                .collect(Collectors.toList());
        Map<Long, Boolean> stockMap = null;
        try {
            R skuHasStock = wareFeignService.getSkuHasStock(skuIdList);
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {};
            stockMap = skuHasStock.getData(typeReference).stream()
                    .collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));
        } catch (Exception e) {
            log.error("库存服务查询异常：原因{}",e);
        }
        Map<Long, Boolean> finalStockMap = stockMap;
        ////////////////////////////////////////////////
        //封装每个sku的信息
        List<SkuEsModel> collect = skuInfoEntities.stream().map(sku -> {
            // 组装需要的数据
            SkuEsModel skuEsModel = new SkuEsModel();
            //1、查出当前spuId对应的所有sku信息,品牌的名字
            BeanUtils.copyProperties(sku, skuEsModel);

            // skuPrice skuImg
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());

            // 2.设置库存信息 hasStock
            // 2. 发送远程调用，库存系统查询是否有库存
            if (finalStockMap == null) {
                skuEsModel.setHasStock(true);
            } else {
                skuEsModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }

            // TODO 3.热度评分。先给默认0
            skuEsModel.setHotScore(0L);

            // 4.查询品牌和分类的名字信息 brandName brandImg catalogName
            BrandEntity brand = brandService.getById(sku.getBrandId());
            skuEsModel.setBrandName(brand.getName());
            skuEsModel.setBrandImg(brand.getLogo());
            CategoryEntity category = categoryService.getById(sku.getCatalogId());
            skuEsModel.setCatalogName(category.getName());

            // 5.设置检索属性 attrs -> attrId attrName attrValue
            // 5.查出当前sku的所有可以被用来检索的规格属性
            skuEsModel.setAttrs(attrsList);

            return skuEsModel;
        }).collect(Collectors.toList());


        // 将数据发给es进行保存：search
        R r = searchFeignService.productStatusUp(collect);
        if (r.getCode() == 0) {
            //远程调用成功
            //修改当前spu的状态

            this.baseMapper.updaSpuStatus(spuId, ProductConstant.ProductStatusEnum.SPU_UP.getCode());
        } else {
            //远程调用失败
            //重复调用？接口幂等性:重试机制
        }
    }

}