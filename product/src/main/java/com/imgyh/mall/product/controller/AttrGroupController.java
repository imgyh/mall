package com.imgyh.mall.product.controller;

import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.common.utils.R;
import com.imgyh.mall.product.entity.AttrAttrgroupRelationEntity;
import com.imgyh.mall.product.entity.AttrEntity;
import com.imgyh.mall.product.entity.AttrGroupEntity;
import com.imgyh.mall.product.service.AttrAttrgroupRelationService;
import com.imgyh.mall.product.service.AttrGroupService;
import com.imgyh.mall.product.service.CategoryService;
import com.imgyh.mall.product.vo.AttrgroupWithAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 属性分组
 *
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 12:40:23
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Resource
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    /**
     *
     * 根据传入的 catelogId(三级分类中的第三级子分类的id) 查询该 catelogId 下所有的属性值, 并分页返回
     * 如果 catelogId==0则返回全部属性
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId){
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        // 还要返回 catelogPath, 这是三级分类的完整路径
        // 在 categoryService 中完成 根据当前属性组所属的分类id 找catelogPath的工作
        List<Long> list = categoryService.findCatelogPath(attrGroup.getCatelogId());

        attrGroup.setCatelogPath(list);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    // 获取分组关联的所有属性
    @GetMapping("{attrgroupId}/attr/relation")
    public R getAttrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        // 去 attrAttrGroupRelation 表中找
        List<AttrEntity> data = attrAttrgroupRelationService.listAttrRelation(attrgroupId);
        return R.ok().put("data",data);
    }

    // 获取分组关联的所有属性
    @GetMapping("{attrgroupId}/noattr/relation")
    public R getNoAttrRelation(@RequestParam Map<String, Object> params,
                               @PathVariable("attrgroupId") Long attrgroupId){

        PageUtils page = attrAttrgroupRelationService.listNoAttrRelation(params, attrgroupId);
        return R.ok().put("page",page);
    }

    // 删除属性与分组的关联关系
    @PostMapping("/attr/relation/delete")
    public R deleteAttrRelation(@RequestBody List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntityList){
        // 去 attrAttrGroupRelation 表中删除
        attrAttrgroupRelationService.deleteAttrRelation(attrAttrgroupRelationEntityList);
        return R.ok();
    }

    // 删除属性与分组的关联关系
    @PostMapping("/attr/relation")
    public R saveAttrRelation(@RequestBody List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntityList){
        // 去 attrAttrGroupRelation 表中保存
        attrAttrgroupRelationService.saveAttrRelation(attrAttrgroupRelationEntityList);
        return R.ok();
    }

    @GetMapping("/{catelogId}/withattr")
    public R attrgroupWithAttr(@PathVariable("catelogId") Long catelogId){
        List<AttrgroupWithAttrVo> vos = attrGroupService.getAttrgroupWithAttr(catelogId);
        return R.ok().put("data", vos);
    }
}
