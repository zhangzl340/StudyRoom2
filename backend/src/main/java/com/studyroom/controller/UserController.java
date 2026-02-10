package com.studyroom.controller;

import com.studyroom.entity.User;
import com.studyroom.service.UserService;
import com.studyroom.utils.FileUploadUtil;
import com.studyroom.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "用户模块",description = "处理用户CRUD")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Operation(summary = "获取用户列表")
    @GetMapping("/list")
    public Result<?> getUserList(@RequestParam(required = false) String role, @RequestParam(required = false) String status, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        // 这里应该实现分页查询，暂时跳过
        return Result.success(userService.list());
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/detail/{id}")
    public Result<?> getUserDetail(@PathVariable Long id) {
        return userService.getUserInfo(id);
    }

    @Operation(summary = "创建用户")
    @PostMapping("/create")
    public Result<?> createUser(@RequestBody User user) {
        String encryptedPassword = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        user.setPassword(encryptedPassword);
        if (userService.save(user)) {
            return Result.success("用户创建成功");
        } else {
            return Result.error("用户创建失败");
        }
    }

    @Operation(summary = "更新用户")
    @PutMapping("/update/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        if (userService.updateById(user)) {
            return Result.success("用户更新成功");
        } else {
            return Result.error("用户更新失败");
        }
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        if (userService.removeById(id)) {
            return Result.success("用户删除成功");
        } else {
            return Result.error("用户删除失败");
        }
    }

    @Operation(summary = "启用/禁用用户")
    @PatchMapping("/status/{id}")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestParam String status) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        if (userService.updateById(user)) {
            return Result.success("用户状态更新成功");
        } else {
            return Result.error("用户状态更新失败");
        }
    }

    @Operation(summary = "重置用户密码")
    @PostMapping("/reset-password/{id}")
    public Result<?> resetUserPassword(@PathVariable Long id, @RequestParam String newPassword) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(newPassword);
        if (userService.updateById(user)) {
            return Result.success("密码重置成功");
        } else {
            return Result.error("密码重置失败");
        }
    }

    @Operation(summary = "调整用户信用分")
    @PostMapping("/adjust-credit/{id}")
    public Result<?> adjustUserCredit(@PathVariable Long id, @RequestParam Integer score) {
        return userService.updateCreditScore(id, score);
    }

    @Operation(summary = "更新个人信息")
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestParam Long userId, @RequestBody User user) {
        return userService.updateUserInfo(userId, user);
    }

    @Operation(summary = "获取个人信息")
    @GetMapping("/profile")
    public Result<?> getProfile(@RequestParam Long userId) {
        return userService.getUserInfo(userId);
    }
    
    @Operation(summary = "上传头像")
    @PostMapping("/upload/avatar")
    public Result<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = fileUploadUtil.uploadImage(file);
            if (imageUrl != null) {
                return Result.success("上传成功", imageUrl);
            } else {
                return Result.error("上传失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("上传失败：" + e.getMessage());
        }
    }
}