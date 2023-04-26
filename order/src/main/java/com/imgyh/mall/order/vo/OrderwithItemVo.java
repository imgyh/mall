package com.imgyh.mall.order.vo;

import com.imgyh.mall.order.entity.OrderEntity;
import com.imgyh.mall.order.entity.OrderItemEntity;
import lombok.Data;

import java.util.List;

/**
 * @ClassName : OrderwithItemVo
 * @Package : com.imgyh.mall.order.vo
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/4/26 14:53
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/
@Data
public class OrderwithItemVo extends OrderEntity {
    List<OrderItemEntity> items;
}
