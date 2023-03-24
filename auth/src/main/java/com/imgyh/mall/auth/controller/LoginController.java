package com.imgyh.mall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.imgyh.mall.auth.feign.MemberFeignService;
import com.imgyh.mall.auth.feign.ThirdPartFeignService;
import com.imgyh.mall.auth.vo.UserLoginVo;
import com.imgyh.mall.auth.vo.UserRegistVo;
import com.imgyh.mall.common.constant.AuthServerConstant;
import com.imgyh.mall.common.exception.BizCodeEnume;
import com.imgyh.mall.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName : LoginController
 * @Package : com.imgyh.mall.auth.controller
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/3/23 15:38
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/

@Controller
public class LoginController {
    @Autowired
    ThirdPartFeignService thirdPartFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/login.html")
    public String loginPage(HttpSession session) {
        // Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        // if(attribute == null){
        //     //没登录
        //     return "login";
        // }else {
        //     return "redirect:http://mall.gyh.im";
        // }
        return "login";
    }

    @GetMapping("/reg.html")
    public String regPage() {

        return "reg";
    }

    /**
     * 获取短信验证码
     *
     * @param phone
     * @return
     */
    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
        // TODO 接口防刷

        // 从redis中查看是否有验证码
        // 有验证码就不发送
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000) {
                // 60秒内不能再发
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        // 生成验证码
        StringBuilder str = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            str.append(random.nextInt(10));
        }
        String code = str.toString();
        System.out.println("验证码是:" + code);

        // 保存到redis, 10分钟有效
        String redisValue = code + "_" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, redisValue, 10, TimeUnit.MINUTES);

        // 发送验证码
        thirdPartFeignService.sendSms(phone, code);

        return R.ok();
    }

    /**
     * //TODO 重定向携带数据，利用session原理。将数据放在session中。
     * 只要跳到下一个页面取出这个数据以后，session里面的数据就会删掉
     * <p>
     * //TODO 1、分布式下的session问题。
     * RedirectAttributes redirectAttributes：模拟重定向携带数据
     *
     * @param vo
     * @param result
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result,
                         RedirectAttributes redirectAttributes,
                         HttpSession session) {
        // 参数有错, 报错误返回给前端显示
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);
            // 校验出错，转发到注册页
            return "redirect:http://auth.mall.gyh.im/reg.html";
        }
        // 校验验证码
        String code = vo.getCode();
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (!StringUtils.isEmpty(redisCode) && code.equals(redisCode.split("_")[0])) {
            // 验证码通过
            // 删除验证码
            redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());

            // 远程调用member微服务
            R r = memberFeignService.regist(vo);
            if (r.getCode() == 0){
                return "redirect:http://auth.mall.gyh.im/login.html";
            }else {
                Map<String, String> errors = new HashMap<>();
                errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
                redirectAttributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.mall.gyh.im/reg.html";
            }

        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            // 验证码不一致，转发到注册页
            return "redirect:http://auth.mall.gyh.im/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes){
        R login = memberFeignService.login(vo);
        if (login != null){
            return "redirect:http://mall.gyh.im";
        }else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg",login.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.mall.gyh.im/login.html";
        }
    }
}
