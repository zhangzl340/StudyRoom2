package com.studyroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.entity.User;

public interface UserMapper extends BaseMapper<User> {

    User selectByUsername(String username);

    User selectByEmail(String email);

    User selectByPhone(String phone);
    
    java.util.List<java.util.Map<String, Object>> selectUserCollegeDistribution();
}