package com.studyroom.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.studyroom.entity.Violation;
import com.studyroom.service.CheckInService;
import com.studyroom.utils.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "签到与违规模块")
@RestController
@RequestMapping("/checkin")
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    @Operation(summary = "签到")
    @PostMapping("/in")
    public Result<?> checkIn(@RequestParam Long reservationId, @RequestParam Long userId, @RequestParam String method) {
        return checkInService.checkIn(reservationId, userId, method);
    }

    @Operation(summary = "签出")
    @PostMapping("/out")
    public Result<?> checkOut(@RequestParam Long id) {
        return checkInService.checkOut(id);
    }

    @Operation(summary = "暂离")
    @PostMapping("/leave")
    public Result<?> leave(@RequestParam Long id) {
        return checkInService.leave(id);
    }

    @Operation(summary = "暂离返回")
    @PostMapping("/return")
    public Result<?> returnFromLeave(@RequestParam Long id) {
        return checkInService.returnFromLeave(id);
    }

    @Operation(summary = "获取签到记录详情")
    @GetMapping("/detail/{id}")
    public Result<?> getCheckInDetail(@PathVariable Long id) {
        return checkInService.getCheckInDetail(id);
    }

    @Operation(summary = "获取用户签到记录")
    @GetMapping("/list")
    public Result<?> getCheckInList(@RequestParam(required = false) Long userId, @RequestParam(required = false) String status, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        return checkInService.getCheckInList(userId, status, page, size);
    }

    @Operation(summary = "获取当前签到信息")
    @GetMapping("/current")
    public Result<?> getCurrentCheckIn(@RequestParam Long userId) {
        return checkInService.getCurrentCheckIn(userId);
    }

    @Operation(summary = "生成签到二维码")
    @GetMapping("/qrcode")
    public Result<?> generateCheckInQrcode(@RequestParam Long reservationId) {
        return checkInService.generateCheckInQrcode(reservationId);
    }

    @Operation(summary = "验证签到二维码")
    @PostMapping("/verify-qrcode")
    public Result<?> verifyCheckInQrcode(@RequestParam String qrcodeContent) {
        return checkInService.verifyCheckInQrcode(qrcodeContent);
    }

    @Operation(summary = "获取签到统计")
    @GetMapping("/statistics")
    public Result<?> getCheckInStatistics(@RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate) {
        return checkInService.getCheckInStatistics(startDate, endDate);
    }

    @Operation(summary = "创建违规记录")
    @PostMapping("/violation/create")
    public Result<?> createViolation(@RequestBody Violation violation) {
        return checkInService.createViolation(violation);
    }

    @Operation(summary = "获取违规记录列表")
    @GetMapping("/violation/list")
    public Result<?> getViolationList(@RequestParam(required = false) Long userId, @RequestParam(required = false) String type, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        return checkInService.getViolationList(userId, type, page, size);
    }

    @Operation(summary = "获取违规记录详情")
    @GetMapping("/violation/detail/{id}")
    public Result<?> getViolationDetail(@PathVariable Long id) {
        return checkInService.getViolationDetail(id);
    }

    @Operation(summary = "更新违规状态")
    @PutMapping("/violation/status/{id}")
    public Result<?> updateViolationStatus(@PathVariable Long id, @RequestParam String status) {
        return checkInService.updateViolationStatus(id, status);
    }
}