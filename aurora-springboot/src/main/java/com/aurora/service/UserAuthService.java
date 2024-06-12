package com.aurora.service;

import com.aurora.model.dto.*;
import com.aurora.model.vo.*;
import me.zhyd.oauth.model.AuthUser;

import java.util.List;

public interface UserAuthService {

    void sendCode(String username);

    List<UserAreaDTO> listUserAreas(ConditionVO conditionVO);

    void register(UserVO userVO);

    void updatePassword(UserVO userVO);

    void updateAdminPassword(PasswordVO passwordVO);

    PageResultDTO<UserAdminDTO> listUsers(ConditionVO condition);

    UserLogoutStatusDTO logout();

    UserInfoDTO thirdLogin(AuthUser authUser, String loginType);

}
