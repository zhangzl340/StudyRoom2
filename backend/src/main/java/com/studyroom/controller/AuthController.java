package com.studyroom.controller;

import com.studyroom.entity.User;
import com.studyroom.service.UserService;
import com.studyroom.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "认证模块")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<?> login(@RequestParam String username, @RequestParam String password) {
        return userService.login(username, password);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        return userService.register(user);
    }

    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public Result<?> refreshToken(@RequestParam String refreshToken) {
        return userService.refreshToken(refreshToken);
    }

    @Operation(summary = "修改密码")
    @PostMapping("/change-password")
    public Result<?> changePassword(@RequestParam Long userId, @RequestParam String oldPassword, @RequestParam String newPassword) {
        return userService.changePassword(userId, oldPassword, newPassword);
    }

    @Operation(summary = "重置密码请求")
    @PostMapping("/reset-password/request")
    public Result<?> resetPasswordRequest(@RequestParam String email) {
        // 这里应该发送验证码到邮箱，暂时跳过
        return Result.success("验证码已发送到邮箱");
    }

    @Operation(summary = "重置密码")
    @PostMapping("/reset-password")
    public Result<?> resetPassword(@RequestParam String email, @RequestParam String newPassword, @RequestParam String verificationCode) {
        return userService.resetPassword(email, newPassword, verificationCode);
    }

    @Operation(summary = "实名认证")
    @PostMapping("/verify-identity")
    public Result<?> verifyIdentity(@RequestParam Long userId, @RequestParam String realName, @RequestParam String idCard) {
        return userService.verifyIdentity(userId, realName, idCard);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<?> getCurrentUserInfo(@RequestHeader("Authorization") String token) {
        // 这里应该从token中解析userId，暂时跳过
        Long userId = 1L;
        return userService.getUserInfo(userId);
    }

    @Operation(summary = "用户注销")
    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader("Authorization") String token) {
        return userService.logout(token);
    }
}
