package com.studyroom.controller;

import com.studyroom.entity.Carousel;
import com.studyroom.service.CarouselService;
import com.studyroom.utils.FileUploadUtil;
import com.studyroom.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "轮播图模块",description = "处理轮播图模块增删改查")
@RestController
@RequestMapping("/carousel")
public class CarouselController {

    @Autowired
    private CarouselService carouselService;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Operation(summary = "获取轮播图列表")
    @GetMapping("/list")
    public Result<?> getCarouselList(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        return carouselService.getCarouselList(page, size);
    }

    @Operation(summary = "获取轮播图详情")
    @GetMapping("/detail/{id}")
    public Result<?> getCarouselDetail(@PathVariable Long id) {
        return carouselService.getCarouselDetail(id);
    }

    @Operation(summary = "获取活跃的轮播图")
    @GetMapping("/active")
    public Result<?> getActiveCarousels() {
        return carouselService.getActiveCarousels();
    }

    @Operation(summary = "上传图片")
    @PostMapping("/upload/image")
    public Result<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = fileUploadUtil.uploadImage(file);
            if (imageUrl != null) {
                return Result.success("上传成功", imageUrl);
            } else {
                return Result.error("上传失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    @Operation(summary = "创建轮播图")
    @PostMapping("/create")
    public Result<?> createCarousel(@RequestBody Carousel carousel) {
        return carouselService.createCarousel(carousel);
    }

    @Operation(summary = "更新轮播图")
    @PutMapping("/update/{id}")
    public Result<?> updateCarousel(@PathVariable Long id, @RequestBody Carousel carousel) {
        return carouselService.updateCarousel(id, carousel);
    }

    @Operation(summary = "删除轮播图")
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteCarousel(@PathVariable Long id) {
        return carouselService.deleteCarousel(id);
    }

    @Operation(summary = "更新轮播图状态")
    @PutMapping("/status/{id}")
    public Result<?> updateCarouselStatus(@PathVariable Long id, @RequestParam String status) {
        return carouselService.updateCarouselStatus(id, status);
    }
}
