package com.studyroom.controller;

import com.studyroom.entity.Seat;
import com.studyroom.service.SeatService;
import com.studyroom.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "座位模块", description = "处理座位模块增删改查")
@RestController
@RequestMapping("/seat")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @Operation(summary = "获取自习室座位列表")
    @GetMapping("/room/{roomId}")
    public Result<?> getSeatsByRoomId(@PathVariable Long roomId) {
        return seatService.getSeatsByRoomId(roomId);
    }

    @Operation(summary = "获取自习室可用座位")
    @GetMapping("/room/{roomId}/available")
    public Result<?> getAvailableSeatsByRoomId(@PathVariable Long roomId) {
        return seatService.getAvailableSeatsByRoomId(roomId);
    }

    @Operation(summary = "创建座位")
    @PostMapping("/create")
    public Result<?> createSeats(@RequestParam Long roomId, @RequestBody List<Seat> seats) {
        return seatService.createSeats(roomId, seats);
    }

    @Operation(summary = "更新座位状态")
    @PutMapping("/status/{id}")
    public Result<?> updateSeatStatus(@PathVariable Long id, @RequestParam String status) {
        return seatService.updateSeatStatus(id, status);
    }

    @Operation(summary = "更新座位布局")
    @PutMapping("/layout")
    public Result<?> updateSeatLayout(@RequestParam Long roomId, @RequestBody List<Seat> seats) {
        return seatService.updateSeatLayout(roomId, seats);
    }

    @Operation(summary = "批量导入座位")
    @PostMapping("/import")
    public Result<?> importSeats(@RequestParam Long roomId, @RequestBody List<Seat> seats) {
        return seatService.importSeats(roomId, seats);
    }

    @Operation(summary = "检查座位可用性")
    @GetMapping("/availability")
    public Result<?> checkSeatAvailability(@RequestParam Long seatId, @RequestParam String startTime, @RequestParam String endTime) {
        return seatService.checkSeatAvailability(seatId, startTime, endTime);
    }
}
