package com.imgyh.mall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imgyh.mall.common.utils.PageUtils;
import com.imgyh.mall.common.utils.Query;
import com.imgyh.mall.member.dao.MemberDao;
import com.imgyh.mall.member.entity.MemberEntity;
import com.imgyh.mall.member.entity.MemberLevelEntity;
import com.imgyh.mall.member.exception.PhoneExsitException;
import com.imgyh.mall.member.exception.UsernameExistException;
import com.imgyh.mall.member.service.MemberLevelService;
import com.imgyh.mall.member.service.MemberService;
import com.imgyh.mall.member.vo.GithubUser;
import com.imgyh.mall.member.vo.MemberLoginVo;
import com.imgyh.mall.member.vo.MemberRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        MemberEntity memberEntity = new MemberEntity();

        // 获取默认等级
        MemberLevelEntity memberLevelEntity = memberLevelService.getOne(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1));
        memberEntity.setLevelId(memberLevelEntity.getId());

        //检查用户名和手机号是否唯一。为了让controller能感知异常，异常机制
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUserName());

        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUserName());
        memberEntity.setNickname(vo.getUserName());


        // 密码要进行加密存储
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encode = encoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

        // 其他默认信息

        // 保存
        this.save(memberEntity);

    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        // 查账户名(用户名或者手机号)是否存在
        MemberEntity memberEntity = baseMapper.selectOne(
                new QueryWrapper<MemberEntity>().eq("username", vo.getLoginacct())
                .or().eq("mobile", vo.getPassword()));
        if (memberEntity == null){
            return null;
        }else {
            // 用户名存在, 验证密码
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            boolean matches = encoder.matches(vo.getPassword(), memberEntity.getPassword());
            if (matches){
                return memberEntity;
            }else {
                return null;
            }
        }

    }

    @Override
    public MemberEntity oauthlogin(GithubUser githubUser) {
        String name = githubUser.getName();
        Long id = githubUser.getId();
        String accessToken = githubUser.getAccessToken();

        // 判断是否登陆过
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("github_name", name).eq("github_id",id));
        if (memberEntity != null) {
            //这个用户已经注册，更新获取到的
            MemberEntity updateMember = new MemberEntity();
            updateMember.setId(memberEntity.getId());
            updateMember.setGithubId(id);
            updateMember.setAccessToken(accessToken);
            updateMember.setGithubName(name);
            baseMapper.updateById(updateMember);

            memberEntity.setGithubName(name);
            memberEntity.setGithubId(id);
            memberEntity.setAccessToken(accessToken);

            return memberEntity;
        }else {
            //没有注册过,注册一个
            MemberEntity entity = new MemberEntity();
            // entity.setUsername("github"+name);
            entity.setNickname(name);
            entity.setGithubId(id);
            entity.setGithubName(name);
            entity.setAccessToken(accessToken);
            baseMapper.insert(entity);

            return entity;
        }

    }

    private void checkUsernameUnique(String userName) throws UsernameExistException{
        long count = this.count(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (count>0){
            throw new UsernameExistException();
        }
    }

    private void checkPhoneUnique(String phone) throws PhoneExsitException{
        long count = this.count(new QueryWrapper<MemberEntity>().eq("username", phone));
        if (count>0){
            throw new PhoneExsitException();
        }
    }

}