package com.studyroom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studyroom.entity.CheckIn;
import com.studyroom.entity.Violation;
import com.studyroom.utils.Result;

import java.util.List;

public interface CheckInService extends IService<CheckIn> {

    Result<?> checkIn(Long reservationId, Long userId, String method);

    Result<?> checkOut(Long id);

    Result<?> leave(Long id);

    Result<?> returnFromLeave(Long id);

    Result<?> getCheckInDetail(Long id);

    Result<?> getCheckInList(Long userId, String status, Integer page, Integer size);

    Result<?> getCurrentCheckIn(Long userId);

    Result<?> generateCheckInQrcode(Long reservationId);

    Result<?> verifyCheckInQrcode(String qrcodeContent);

    Result<?> getCheckInStatistics(String startDate, String endDate);

    Result<?> createViolation(Violation violation);

    Result<?> getViolationList(Long userId, String type, Integer page, Integer size);

    Result<?> getViolationDetail(Long id);

    Result<?> updateViolationStatus(Long id, String status);
}