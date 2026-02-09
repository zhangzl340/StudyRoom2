package com.studyroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.entity.College;

import java.util.List;

public interface CollegeMapper extends BaseMapper<College> {

    List<College> selectAll();

    College selectById(Long id);

    int insert(College college);

    int update(College college);

    int deleteById(Long id);
}
