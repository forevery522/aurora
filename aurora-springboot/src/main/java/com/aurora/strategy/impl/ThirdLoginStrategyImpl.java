package com.aurora.strategy.impl;

import com.alibaba.fastjson.JSON;
import com.aurora.model.dto.UserDetailsDTO;
import com.aurora.model.dto.UserInfoDTO;
import com.aurora.entity.UserAuth;
import com.aurora.entity.UserInfo;
import com.aurora.entity.UserRole;
import com.aurora.enums.RoleEnum;
import com.aurora.exception.BizException;
import com.aurora.mapper.UserAuthMapper;
import com.aurora.mapper.UserInfoMapper;
import com.aurora.mapper.UserRoleMapper;
import com.aurora.service.TokenService;
import com.aurora.service.impl.UserDetailServiceImpl;
import com.aurora.strategy.SocialLoginStrategy;
import com.aurora.util.BeanCopyUtil;
import com.aurora.util.IpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.aurora.constant.CommonConstant.TRUE;

@Service("thirdLoginStrategyImpl")
public class ThirdLoginStrategyImpl implements SocialLoginStrategy {

    @Autowired
    private UserAuthMapper userAuthMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private UserDetailServiceImpl userDetailService;

    @Autowired
    private TokenService tokenService;

    @Resource
    private HttpServletRequest request;

    @Override
    public UserInfoDTO login(String data, String loginType) {
        UserDetailsDTO userDetailsDTO;
//        SocialTokenDTO socialToken = getSocialToken(data);
        AuthUser socialToken = JSON.parseObject(data, AuthUser.class);
        String ipAddress = IpUtil.getIpAddress(request);
        String ipSource = IpUtil.getIpSource(ipAddress);
        UserAuth user = getUserAuth(socialToken, loginType);
        if (Objects.nonNull(user)) {
            userDetailsDTO = getUserDetail(user, ipAddress, ipSource);
        } else {
            userDetailsDTO = saveUserDetail(socialToken, ipAddress, ipSource, loginType);
        }
        if (userDetailsDTO.getIsDisable().equals(TRUE)) {
            throw new BizException("用户帐号已被锁定");
        }
//        userDetailsDTO.setExpireTime(LocalDateTime.now().plusSeconds(socialToken.getToken().getExpireIn()));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetailsDTO, null, userDetailsDTO.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        UserInfoDTO userInfoDTO = BeanCopyUtil.copyObject(userDetailsDTO, UserInfoDTO.class);
        String token = tokenService.createToken(userDetailsDTO);
        userInfoDTO.setToken(token);
        return userInfoDTO;
    }

//    public abstract SocialTokenDTO getSocialToken(String data);

//    public abstract SocialUserInfoDTO getSocialUserInfo(SocialTokenDTO socialTokenDTO);

    private UserAuth getUserAuth(AuthUser authUser, String loginType) {
        return userAuthMapper.selectOne(new LambdaQueryWrapper<UserAuth>()
                .eq(UserAuth::getUsername, authUser.getUsername())
                .eq(UserAuth::getLoginType, loginType));
    }

    private UserDetailsDTO getUserDetail(UserAuth user, String ipAddress, String ipSource) {
        userAuthMapper.update(new UserAuth(), new LambdaUpdateWrapper<UserAuth>()
                .set(UserAuth::getLastLoginTime, LocalDateTime.now())
                .set(UserAuth::getIpAddress, ipAddress)
                .set(UserAuth::getIpSource, ipSource)
                .eq(UserAuth::getId, user.getId()));
        return userDetailService.convertUserDetail(user, request);
    }

    private UserDetailsDTO saveUserDetail(AuthUser socialToken, String ipAddress, String ipSource, String loginType) {
//        SocialUserInfoDTO socialUserInfo = getSocialUserInfo(socialToken);
        UserInfo userInfo = UserInfo.builder()
                .nickname(socialToken.getNickname())
                .avatar(socialToken.getAvatar())
                .email(socialToken.getEmail())
                .build();
        userInfoMapper.insert(userInfo);
        UserAuth userAuth = UserAuth.builder()
                .userInfoId(userInfo.getId())
                .username(socialToken.getUsername())
                .password(socialToken.getToken().getAccessToken())
                .loginType(loginType)
                .lastLoginTime(LocalDateTime.now())
                .ipAddress(ipAddress)
                .ipSource(ipSource)
                .build();
        userAuthMapper.insert(userAuth);
        UserRole userRole = UserRole.builder()
                .userId(userInfo.getId())
                .roleId(RoleEnum.USER.getRoleId())
                .build();
        userRoleMapper.insert(userRole);
        return userDetailService.convertUserDetail(userAuth, request);
    }

}
