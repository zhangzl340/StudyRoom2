package com.studyroom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.entity.College;
import com.studyroom.mapper.CollegeMapper;
import com.studyroom.service.CollegeService;
import com.studyroom.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, College> implements CollegeService {

    @Autowired
    private CollegeMapper collegeMapper;

    @Override
    public Result<?> getCollegeList() {
        try {
            log.info("=== 获取学院列表 ===");
            List<College> colleges = collegeMapper.selectAll();
            log.info("学院列表获取成功，共 {} 个学院", colleges.size());
            return Result.success(colleges);
        } catch (Exception e) {
            log.error("获取学院列表失败", e);
            return Result.error("获取学院列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> getCollegeDetail(Long id) {
        try {
            log.info("=== 获取学院详情 ===");
            College college = collegeMapper.selectById(id);
            if (college == null) {
                return Result.error("学院不存在");
            }
            log.info("学院详情获取成功: {}", college);
            return Result.success(college);
        } catch (Exception e) {
            log.error("获取学院详情失败", e);
            return Result.error("获取学院详情失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> createCollege(College college) {
        try {
            log.info("=== 创建学院 ===");
            log.info("学院信息: {}", college);
            
            // 验证参数
            if (college.getName() == null || college.getName().isEmpty()) {
                return Result.error("学院名称不能为空");
            }
            if (college.getColor() == null || college.getColor().isEmpty()) {
                return Result.error("学院颜色不能为空");
            }
            
            int result = collegeMapper.insert(college);
            if (result > 0) {
                log.info("学院创建成功");
                return Result.success("学院创建成功");
            } else {
                return Result.error("学院创建失败");
            }
        } catch (Exception e) {
            log.error("创建学院失败", e);
            return Result.error("创建学院失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> updateCollege(Long id, College college) {
        try {
            log.info("=== 更新学院 ===");
            log.info("学院ID: {}, 学院信息: {}", id, college);
            
            // 验证参数
            if (college.getName() == null || college.getName().isEmpty()) {
                return Result.error("学院名称不能为空");
            }
            if (college.getColor() == null || college.getColor().isEmpty()) {
                return Result.error("学院颜色不能为空");
            }
            
            // 检查学院是否存在
            College existingCollege = collegeMapper.selectById(id);
            if (existingCollege == null) {
                return Result.error("学院不存在");
            }
            
            college.setId(id);
            int result = collegeMapper.update(college);
            if (result > 0) {
                log.info("学院更新成功");
                return Result.success("学院更新成功");
            } else {
                return Result.error("学院更新失败");
            }
        } catch (Exception e) {
            log.error("更新学院失败", e);
            return Result.error("更新学院失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> deleteCollege(Long id) {
        try {
            log.info("=== 删除学院 ===");
            log.info("学院ID: {}", id);
            
            // 检查学院是否存在
            College college = collegeMapper.selectById(id);
            if (college == null) {
                return Result.error("学院不存在");
            }
            
            int result = collegeMapper.deleteById(id);
            if (result > 0) {
                log.info("学院删除成功");
                return Result.success("学院删除成功");
            } else {
                return Result.error("学院删除失败");
            }
        } catch (Exception e) {
            log.error("删除学院失败", e);
            return Result.error("删除学院失败: " + e.getMessage());
        }
    }
}
