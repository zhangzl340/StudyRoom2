package com.studyroom.controller;

import com.studyroom.entity.Reservation;
import com.studyroom.service.ReservationService;
import com.studyroom.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Tag(name = "预约模块",description = "预约模块增删改查")
@RestController
@RequestMapping("/reservation")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Operation(summary = "获取预约列表")
    @GetMapping("/list")
    public Result<?> getReservationList(@RequestParam(required = false) Long userId, @RequestParam(required = false) String status, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        return reservationService.getReservationList(userId, status, page, size);
    }

    @Operation(summary = "获取预约详情")
    @GetMapping("/detail/{id}")
    public Result<?> getReservationDetail(@PathVariable Long id) {
        return reservationService.getReservationDetail(id);
    }

    @Operation(summary = "创建预约")
    @PostMapping("/create")
    public Result<?> createReservation(@RequestBody Reservation reservation) {
        return reservationService.createReservation(reservation);
    }

    @Operation(summary = "修改预约")
    @PutMapping("/update/{id}")
    public Result<?> updateReservation(@PathVariable Long id, @RequestBody Reservation reservation) {
        return reservationService.updateReservation(id, reservation);
    }

    @Operation(summary = "取消预约")
    @PostMapping("/cancel")
    public Result<?> cancelReservation(@RequestParam Long id) {
        return reservationService.cancelReservation(id);
    }

    @Operation(summary = "确认预约")
    @PostMapping("/check")
    public Result<?> confirmReservation(@RequestParam Long id) {
        return reservationService.confirmReservation(id);
    }

    @Operation(summary = "计算预约费用")
    @PostMapping("/fee")
    public Result<?> calculateReservationFee(@RequestParam Long id) {
        return reservationService.calculateReservationFee(id);
    }

    @Operation(summary = "获取即将开始的预约")
    @GetMapping("/upcoming")
    public Result<?> getUpcomingReservations(@RequestParam Long userId) {
        return reservationService.getUpcomingReservations(userId);
    }

    @Operation(summary = "获取今日预约")
    @GetMapping("/today")
    public Result<?> getTodayReservations(@RequestParam Long userId) {
        return reservationService.getTodayReservations(userId);
    }

//    @Operation(summary = "检查预约冲突")
//    @GetMapping("/availability")
//    public Result<?> checkAvailability(@RequestParam Long seatId, @RequestParam String startTime, @RequestParam String endTime) {
//        return reservationService.checkAvailability(seatId, startTime, endTime);
//    }

    @Operation(summary = "检查座位可用性")
    public Result<?> checkAvailability(
            @RequestParam Long seatId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {

        return reservationService.checkAvailability(seatId, startTime, endTime);
    }


    @Operation(summary = "获取今日预约统计")
    @GetMapping("/statistics")
    public Result<?> getReservationStatistics() {
        return reservationService.getReservationStatistics();
    }

    @Operation(summary = "更新预约状态")
    @PutMapping("/status/{id}")
    public Result<?> updateReservationStatus(@PathVariable Long id, @RequestParam String status) {
        return reservationService.updateReservationStatus(id, status);
    }
}