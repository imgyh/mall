package com.imgyh.mall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName : AttrResponseVo
 * @Package : com.imgyh.mall.product.vo
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/2/27 20:38
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/
@Data
public class AttrResponseVo extends AttrVo{
    private String catelogName;
    private String groupName;
    private List<Long> catelogPath;
}
