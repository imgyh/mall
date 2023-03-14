package com.imgyh.mall.search.service;

import com.imgyh.mall.common.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @ClassName : ProductSaveService
 * @Package : com.imgyh.mall.search.service
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/3/14 19:30
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/
public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
