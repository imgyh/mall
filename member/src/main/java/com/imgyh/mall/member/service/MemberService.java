package com.imgyh.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.member.entity.MemberEntity;
import com.imgyh.mall.member.vo.GithubUser;
import com.imgyh.mall.member.vo.MemberLoginVo;
import com.imgyh.mall.member.vo.MemberRegistVo;

import java.util.Map;

/**
 * 会员
 *
 * @author imgyh
 * @email admin@imgyh.com
 * @date 2023-02-14 14:34:16
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo);

    MemberEntity login(MemberLoginVo vo);

    MemberEntity oauthlogin(GithubUser githubUser);
}

