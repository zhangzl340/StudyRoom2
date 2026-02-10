package com.studyroom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.entity.User;
import com.studyroom.exception.BusinessException;
import com.studyroom.exception.UnauthorizedException;
import com.studyroom.mapper.UserMapper;
import com.studyroom.service.UserService;
import com.studyroom.utils.JwtUtil;
import com.studyroom.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Result<?> login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UnauthorizedException("用户名或密码错误");
        }

        if (!user.getStatus().equals("active")) {
            throw new UnauthorizedException("账号已被禁用");
        }
        log.error("密码：" + user.getPassword());
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        log.error("加密后的密码为：" + encryptedPassword);
        if (!user.getPassword().equals(encryptedPassword)) {
            throw new UnauthorizedException("密码错误");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());

        String token = jwtUtil.generateToken(user.getUsername(), claims);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("user", user);

        return Result.success(result);
    }

    @Override
    public Result<?> register(User user) {
        if (userMapper.selectByUsername(user.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        if (userMapper.selectByEmail(user.getEmail()) != null) {
            throw new BusinessException("邮箱已被注册");
        }

        if (userMapper.selectByPhone(user.getPhone()) != null) {
            throw new BusinessException("手机号已被注册");
        }

        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        user.setStatus("active");
        user.setCreditScore(100);

        if (save(user)) {
            return Result.success("注册成功");
        } else {
            throw new BusinessException("注册失败");
        }
    }

    @Override
    public Result<?> refreshToken(String refreshToken) {
        try {
            String username = jwtUtil.getSubject(refreshToken);
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                throw new UnauthorizedException("用户不存在");
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("username", user.getUsername());
            claims.put("role", user.getRole());

            String newToken = jwtUtil.generateToken(user.getUsername(), claims);
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername());

            Map<String, Object> result = new HashMap<>();
            result.put("token", newToken);
            result.put("refreshToken", newRefreshToken);

            return Result.success(result);
        } catch (Exception e) {
            throw new UnauthorizedException("刷新令牌失败");
        }
    }

    @Override
    public Result<?> changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String encryptedOldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!user.getPassword().equals(encryptedOldPassword)) {
            throw new BusinessException("原密码错误");
        }

        user.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        if (updateById(user)) {
            return Result.success("密码修改成功");
        } else {
            throw new BusinessException("密码修改失败");
        }
    }

    @Override
    public Result<?> resetPassword(String email, String newPassword, String verificationCode) {
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 这里应该验证验证码，暂时跳过
        user.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        if (updateById(user)) {
            return Result.success("密码重置成功");
        } else {
            throw new BusinessException("密码重置失败");
        }
    }

    @Override
    public Result<?> verifyIdentity(Long userId, String realName, String idCard) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setRealName(realName);
        // 这里应该保存身份证信息，暂时跳过
        if (updateById(user)) {
            return Result.success("实名认证成功");
        } else {
            throw new BusinessException("实名认证失败");
        }
    }

    @Override
    public Result<?> getUserInfo(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return Result.success(user);
    }

    @Override
    public Result<?> updateUserInfo(Long userId, User user) {
        User existingUser = getById(userId);
        if (existingUser == null) {
            throw new BusinessException("用户不存在");
        }

        existingUser.setRealName(user.getRealName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setAvatar(user.getAvatar());

        if (updateById(existingUser)) {
            return Result.success("个人信息更新成功");
        } else {
            throw new BusinessException("个人信息更新失败");
        }
    }

    @Override
    public Result<?> updateCreditScore(Long userId, Integer score) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setCreditScore(score);
        if (updateById(user)) {
            return Result.success("信用分更新成功");
        } else {
            throw new BusinessException("信用分更新失败");
        }
    }

    @Override
    public Result<?> logout(String token) {
        // 这里应该将token加入黑名单，暂时跳过
        return Result.success("登出成功");
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public User findByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> getUserCollegeDistribution() {
        try {
            log.info("=== 查询用户学院分布 ===");
            return userMapper.selectUserCollegeDistribution();
        } catch (Exception e) {
            log.error("查询用户学院分布失败", e);
            return new java.util.ArrayList<>();
        }
    }
}