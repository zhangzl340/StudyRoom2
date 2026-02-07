package com.studyroom.controller;

import com.studyroom.entity.Announcement;
import com.studyroom.service.AnnouncementService;
import com.studyroom.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "公告模块",description = "处理公告模块增删改查")
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Operation(summary = "获取公告列表")
    @GetMapping("/list")
    public Result<?> getAnnouncementList(@RequestParam(required = false) String type, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        return announcementService.getAnnouncementList(type, page, size);
    }

    @Operation(summary = "获取公告详情")
    @GetMapping("/detail/{id}")
    public Result<?> getAnnouncementDetail(@PathVariable Long id) {
        return announcementService.getAnnouncementDetail(id);
    }

    @Operation(summary = "创建公告")
    @PostMapping("/create")
    public Result<?> createAnnouncement(@RequestBody Announcement announcement) {
        return announcementService.createAnnouncement(announcement);
    }

    @Operation(summary = "更新公告")
    @PutMapping("/update/{id}")
    public Result<?> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        return announcementService.updateAnnouncement(id, announcement);
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteAnnouncement(@PathVariable Long id) {
        return announcementService.deleteAnnouncement(id);
    }

    @Operation(summary = "获取活跃的公告")
    @GetMapping("/active")
    public Result<?> getActiveAnnouncements() {
        return announcementService.getActiveAnnouncements();
    }

    @Operation(summary = "更新公告状态")
    @PutMapping("/status/{id}")
    public Result<?> updateAnnouncementStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        return announcementService.updateAnnouncementStatus(id, isActive);
    }

    @Operation(summary = "获取最近的公告")
    @GetMapping("/recent")
    public Result<?> getRecentAnnouncements(@RequestParam(defaultValue = "5") Integer limit) {
        return announcementService.getRecentAnnouncements(limit);
    }
}
