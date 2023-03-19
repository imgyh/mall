package com.imgyh.mall.search.service;

import com.imgyh.mall.search.vo.SearchParam;
import com.imgyh.mall.search.vo.SearchResult;

/**
 * @ClassName : MallSearchService
 * @Package : com.imgyh.mall.search.service
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/3/19 20:13
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/
public interface MallSearchService {
    SearchResult search(SearchParam param);
}
