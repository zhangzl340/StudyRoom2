package com.studyroom.controller;

import com.studyroom.entity.Room;
import com.studyroom.service.RoomService;
import com.studyroom.utils.FileUploadUtil;
import com.studyroom.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "自习室模块",description ="处理自习室模块增删改查")
@RestController
@RequestMapping("/room")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Operation(summary = "获取自习室列表")
    @GetMapping("/list")
    public Result<?> getRoomList(@RequestParam(required = false) String building, @RequestParam(required = false) Integer floor, @RequestParam(required = false) String status, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        return roomService.getRoomList(building, floor, status, page, size);
    }

    @Operation(summary = "获取自习室详情")
    @GetMapping("/detail/{id}")
    public Result<?> getRoomDetail(@PathVariable Long id) {
        return roomService.getRoomDetail(id);
    }

    @Operation(summary = "获取自习室实时状态")
    @GetMapping("/status/{id}")
    public Result<?> getRoomStatus(@PathVariable Long id) {
        return roomService.getRoomStatus(id);
    }

    @Operation(summary = "获取可用的自习室")
    @GetMapping("/available")
    public Result<?> getAvailableRooms() {
        return roomService.getAvailableRooms();
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

    @Operation(summary = "创建自习室")
    @PostMapping("/create")
    public Result<?> createRoom(@RequestBody Room room) {
        return roomService.createRoom(room);
    }

    @Operation(summary = "更新自习室")
    @PutMapping("/update/{id}")
    public Result<?> updateRoom(@PathVariable Long id, @RequestBody Room room) {
        return roomService.updateRoom(id, room);
    }

    @Operation(summary = "删除自习室")
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteRoom(@PathVariable Long id) {
        return roomService.deleteRoom(id);
    }

    @Operation(summary = "更新自习室状态")
    @PutMapping("/status/{id}")
    public Result<?> updateRoomStatus(@PathVariable Long id, @RequestParam String status) {
        return roomService.updateRoomStatus(id, status);
    }
}