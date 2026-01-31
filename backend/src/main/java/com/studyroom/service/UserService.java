package com.studyroom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studyroom.entity.User;
import com.studyroom.utils.Result;

import java.util.Map;

public interface UserService extends IService<User> {

    Result<?> login(String username, String password);

    Result<?> register(User user);

    Result<?> refreshToken(String refreshToken);

    Result<?> changePassword(Long userId, String oldPassword, String newPassword);

    Result<?> resetPassword(String email, String newPassword, String verificationCode);

    Result<?> verifyIdentity(Long userId, String realName, String idCard);

    Result<?> getUserInfo(Long userId);

    Result<?> updateUserInfo(Long userId, User user);

    Result<?> updateCreditScore(Long userId, Integer score);

    Result<?> logout(String token);

    User findByUsername(String username);

    User findByEmail(String email);

    User findByPhone(String phone);
}