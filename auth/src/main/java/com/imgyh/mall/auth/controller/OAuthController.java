package com.imgyh.mall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.imgyh.mall.auth.feign.MemberFeignService;
import com.imgyh.mall.auth.github.GitHubOAuthInfo;
import com.imgyh.mall.auth.github.GithubAccessToken;
import com.imgyh.mall.auth.utils.RestTemplateUtil;
import com.imgyh.mall.auth.vo.GithubUser;
import com.imgyh.mall.common.utils.R;
import com.imgyh.mall.common.vo.MemberRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @ClassName : OAuthController
 * @Package : com.imgyh.mall.auth.controller
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/4/2 14:00
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/

@Controller
public class OAuthController {
    @Autowired
    RestTemplateUtil restTemplateUtil;

    @Autowired
    private GitHubOAuthInfo gitHubOAuthInfo;

    @Autowired
    private MemberFeignService memberFeignService;


    @GetMapping("/oauth/github/redirect")
    public String githubRedirect(String code, HttpSession session){
        String getAccessTokenUrl = "https://github.com/login/oauth/access_token";
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> mediaTypes = new ArrayList<>();
        //请求头accept设置为：application/json
        mediaTypes.add(MediaType.APPLICATION_JSON);
        headers.setAccept(mediaTypes);
        HashMap<String, String> requestBody = new HashMap<>(16);
        requestBody.put("client_id",gitHubOAuthInfo.getClientId());
        requestBody.put("client_secret",gitHubOAuthInfo.getClientSecrets());
        requestBody.put("code",code);
        //请求路径，请求头，请求体，请求响应结果类型，路径参数
        ResponseEntity<String> responseEntity = restTemplateUtil.post(getAccessTokenUrl,headers, requestBody,String.class,new HashMap<>(16));
        String accessTokenJson = responseEntity.getBody();
        System.out.println("令牌Json数据："+accessTokenJson);

        ObjectMapper objectMapper = new ObjectMapper();
        //转换带下划线的属性为驼峰属性
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        try {
            GithubAccessToken accessToken = objectMapper.readValue(accessTokenJson, GithubAccessToken.class);
            headers.set("Authorization","token "+accessToken.getAccessToken());
            String getUserInfoUrl = "https://api.github.com/user";
            ResponseEntity<String> response = restTemplateUtil.get(getUserInfoUrl, headers, String.class, new HashMap<>(16));
            // return response.getBody();

            GithubUser githubUser = JSON.parseObject(response.getBody(), GithubUser.class);
            if (githubUser != null) {
                githubUser.setAccessToken(accessToken.getAccessToken());

                R oauthlogin = memberFeignService.oauthlogin(githubUser);
                if(oauthlogin.getCode() == 0) {
                    MemberRespVo data = oauthlogin.getData("data", new TypeReference<MemberRespVo>() {
                    });
                    //1、第一次使用session；命令浏览器保存卡号。JSESSIONID这个cookie；
                    //以后浏览器访问哪个网站就会带上这个网站的cookie；
                    //子域之间； gulimall.com  auth.gulimall.com  order.gulimall.com
                    //发卡的时候(指定域名为父域名)，即使是子域系统发的卡，也能让父域直接使用。
                    //TODO 1、默认发的令牌。session=dsajkdjl。作用域：当前域；（解决子域session共享问题）
                    //TODO 2、使用JSON的序列化方式来序列化对象数据到redis中
                    session.setAttribute("loginUser", data);
//                new Cookie("JSESSIONID","dadaa").setDomain("");
//                servletResponse.addCookie();
                    //2、登录成功就跳回首页
                    return "redirect:http://mall.gyh.im";
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:http://auth.mall.gyh.im/login.html";
    }
}
