package com.studyroom.controller;

import com.studyroom.entity.College;
import com.studyroom.service.CollegeService;
import com.studyroom.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "学院模块", description = "处理学院模块增删改查")
@RestController
@RequestMapping("/college")
public class CollegeController {

    @Autowired
    private CollegeService collegeService;

    @Operation(summary = "获取学院列表")
    @GetMapping("/list")
    public Result<?> getCollegeList() {
        return collegeService.getCollegeList();
    }

    @Operation(summary = "获取学院详情")
    @GetMapping("/detail/{id}")
    public Result<?> getCollegeDetail(@PathVariable Long id) {
        return collegeService.getCollegeDetail(id);
    }

    @Operation(summary = "创建学院")
    @PostMapping("/create")
    public Result<?> createCollege(@RequestBody College college) {
        return collegeService.createCollege(college);
    }

    @Operation(summary = "更新学院")
    @PutMapping("/update/{id}")
    public Result<?> updateCollege(@PathVariable Long id, @RequestBody College college) {
        return collegeService.updateCollege(id, college);
    }

    @Operation(summary = "删除学院")
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteCollege(@PathVariable Long id) {
        return collegeService.deleteCollege(id);
    }
}
