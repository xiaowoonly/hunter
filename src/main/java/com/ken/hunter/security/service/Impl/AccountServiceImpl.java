package com.ken.hunter.security.service.Impl;


import com.ken.hunter.domain.UserInfoDTO;
import com.ken.hunter.exception.UserAccountServiceException;
import com.ken.hunter.exception.UserInfoServiceException;
import com.ken.hunter.security.service.Interface.AccountService;
import com.ken.hunter.security.service.Interface.UserInfoService;
import com.ken.hunter.security.util.MD5Util;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * 账户Service
 *
 * @author Ken
 * @since 2017-3-1
 */
@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private UserInfoService userInfoService;

    private static final String OLD_PASSWORD = "oldPassword";
    private static final String NEW_PASSWORD = "newPassword";
    private static final String REPEAT_PASSWORD = "rePassword";

    /**
     * 密码更改
     */
    @Override
    public void passwordModify(Integer userID, Map<String, Object> passwordInfo) throws UserAccountServiceException {

        if (passwordInfo == null)
            throw new UserAccountServiceException(UserAccountServiceException.PASSWORD_ERROR);

        // 获取更改密码信息
        String rePassword = (String) passwordInfo.get(REPEAT_PASSWORD);
        String newPassword = (String) passwordInfo.get(NEW_PASSWORD);
        String oldPassword = (String) passwordInfo.get(OLD_PASSWORD);
        if (rePassword == null || newPassword == null || oldPassword == null)
            throw new UserAccountServiceException(UserAccountServiceException.PASSWORD_ERROR);

        try {
            // 获取用户的账户信息
            UserInfoDTO user = userInfoService.getUserInfo(userID);
            if (user == null) {
                throw new UserAccountServiceException(UserAccountServiceException.PASSWORD_ERROR);
            }

            // 新密码一致性验证
            if (!newPassword.equals(rePassword)) {
                throw new UserAccountServiceException(UserAccountServiceException.PASSWORD_UNMATCH);
            }

            // 原密码正确性验证
            String password;
            password = MD5Util.MD5(oldPassword + userID);
            if (!password.equals(user.getPassword()))
                throw new UserAccountServiceException(UserAccountServiceException.PASSWORD_ERROR);

            // 获得新的密码并加密
            password = MD5Util.MD5(newPassword + userID);

            // 验证成功后更新数据库
            user.setPassword(password);
            user.setFirstLogin(false);
            userInfoService.updateUserInfo(user);

            // 更新密码修改信息(是否为初次修改密码)
            Subject currentSubject = SecurityUtils.getSubject();
            Session session = currentSubject.getSession();
            session.setAttribute("firstLogin", false);

        } catch (NullPointerException | UserInfoServiceException e) {
            throw new UserAccountServiceException(UserAccountServiceException.PASSWORD_ERROR);
        }

    }

}
