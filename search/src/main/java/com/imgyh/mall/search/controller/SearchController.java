package com.imgyh.mall.search.controller;

import com.imgyh.mall.search.service.MallSearchService;
import com.imgyh.mall.search.vo.SearchParam;
import com.imgyh.mall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName : SearchController
 * @Package : com.imgyh.mall.search.controller
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/3/19 15:26
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/
@Controller
public class SearchController {
    @Autowired
    private MallSearchService mallSearchService;

    @GetMapping("list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request){
        param.set_queryString(request.getQueryString());

        //1、根据传递来的页面的查询参数，去es中检索商品
        SearchResult result = mallSearchService.search(param);

        model.addAttribute("result",result);
        return "list";
    }
}
