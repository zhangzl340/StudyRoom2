package com.studyroom.controller;

import com.studyroom.utils.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Tag(name= "测试接口")
public class TestController {
    @GetMapping("/hello")
    public Result<?> hello() {
        return Result.success("Hello, Study Room System!");
    }

    @GetMapping("/health")
    public Result<?> health() {
        return Result.success("System is healthy!");
    }
}
