package com.studyroom.controller;

import com.studyroom.entity.Room;
import com.studyroom.entity.Seat;
import com.studyroom.service.RoomService;
import com.studyroom.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "自习室模块",description ="处理自习室模块增删改查")
@RestController
@RequestMapping("/room")
public class RoomController {

    @Autowired
    private RoomService roomService;

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

    @Operation(summary = "获取自习室座位列表")
    @GetMapping("/{id}/seats")
    public Result<?> getSeatsByRoomId(@PathVariable Long id) {
        return roomService.getSeatsByRoomId(id);
    }

    @Operation(summary = "获取自习室可用座位")
    @GetMapping("/{id}/available-seats")
    public Result<?> getAvailableSeatsByRoomId(@PathVariable Long id) {
        return roomService.getAvailableSeatsByRoomId(id);
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

    @Operation(summary = "创建座位")
    @PostMapping("/seats/create")
    public Result<?> createSeats(@RequestParam Long roomId, @RequestBody List<Seat> seats) {
        return roomService.createSeats(roomId, seats);
    }

    @Operation(summary = "更新座位状态")
    @PutMapping("/seat/status/{id}")
    public Result<?> updateSeatStatus(@PathVariable Long id, @RequestParam String status) {
        return roomService.updateSeatStatus(id, status);
    }

    @Operation(summary = "更新自习室布局")
    @PutMapping("/seat/layout")
    public Result<?> updateSeatLayout(@RequestParam Long roomId, @RequestBody List<Seat> seats) {
        return roomService.updateSeatLayout(roomId, seats);
    }

    @Operation(summary = "批量导入座位")
    @PostMapping("/seat/import")
    public Result<?> importSeats(@RequestParam Long roomId, @RequestBody List<Seat> seats) {
        return roomService.importSeats(roomId, seats);
    }

    @Operation(summary = "检查座位可用性")
    @GetMapping("/seat/availability")
    public Result<?> checkSeatAvailability(@RequestParam Long roomId, @RequestParam Long seatId, @RequestParam String startTime, @RequestParam String endTime) {
        return roomService.checkSeatAvailability(roomId, seatId, startTime, endTime);
    }
}