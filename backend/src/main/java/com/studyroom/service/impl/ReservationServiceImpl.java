package com.studyroom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.entity.Reservation;
import com.studyroom.exception.BusinessException;
import com.studyroom.mapper.ReservationMapper;
import com.studyroom.service.ReservationService;
import com.studyroom.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Autowired
    private ReservationMapper reservationMapper;

    @Override
    public Result<?> createReservation(Reservation reservation) {

        // 检查预约冲突
        Result<?> conflictResult = checkReservationConflict(reservation.getSeatId(), reservation.getReservationInTime().toString(), reservation.getReservationOutTime().toString());
        if (!conflictResult.isSuccess()) {
            return conflictResult;
        }

        // 设置默认状态
        reservation.setStatus("pending");

        if (save(reservation)) {
            return Result.success("预约创建成功");
        } else {
            throw new BusinessException("预约创建失败");
        }
    }

    @Override
    public Result<?> cancelReservation(Long id) {
        Reservation reservation = getById(id);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }

        reservation.setStatus("cancelled");
        if (updateById(reservation)) {
            return Result.success("预约取消成功");
        } else {
            throw new BusinessException("预约取消失败");
        }
    }

    @Override
    public Result<?> getReservationList(Long userId, String status, Integer page, Integer size) {
        // 这里应该实现分页查询，暂时跳过
        List<Reservation> reservations;
        if (userId != null) {
            reservations = reservationMapper.selectByUserId(userId);
        } else if (status != null) {
            reservations = reservationMapper.selectByStatus(status);
        } else {
            reservations = list();
        }
        return Result.success(reservations);
    }

    @Override
    public Result<?> getReservationDetail(Long id) {
        Reservation reservation = getById(id);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }
        return Result.success(reservation);
    }

    @Override
    public Result<?> updateReservation(Long id, Reservation reservation) {
        Reservation existingReservation = getById(id);
        if (existingReservation == null) {
            throw new BusinessException("预约不存在");
        }

        reservation.setId(id);
        if (updateById(reservation)) {
            return Result.success("预约更新成功");
        } else {
            throw new BusinessException("预约更新失败");
        }
    }

    @Override
    public Result<?> checkReservationConflict(Long seatId, String startTime, String endTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);

            List<Reservation> conflicts = reservationMapper.selectByTimeRange(seatId, start, end);
            if (!conflicts.isEmpty()) {
                return Result.error("该时间段座位已被预约");
            }
            return Result.success("无预约冲突");
        } catch (Exception e) {
            throw new BusinessException("时间格式错误");
        }
    }

    @Override
    public Result<?> calculateReservationFee(Long id) {
        Reservation reservation = getById(id);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }

        // 计算预约时长（小时）
        long duration = (reservation.getReservationOutTime().getTime() - reservation.getReservationInTime().getTime()) / (1000 * 60 * 60);
        // 假设每小时1元
        double fee = duration * 1.0;

        return Result.success(fee);
    }

    @Override
    public Result<?> getUpcomingReservations(Long userId) {
        return Result.success(reservationMapper.selectUpcomingByUserId(userId));
    }

    @Override
    public Result<?> getTodayReservations(Long userId) {
        return Result.success(reservationMapper.selectTodayByUserId(userId));
    }

    @Override
    public Result<?> checkAvailability(Long seatId, String startTime, String endTime) {
        return checkReservationConflict(seatId, startTime, endTime);
    }

    @Override
    public Result<?> getReservationStatistics() {
        // 这里应该实现统计逻辑，暂时跳过
        return Result.success("统计成功");
    }

    @Override
    public Result<?> confirmReservation(Long id) {
        Reservation reservation = getById(id);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }

        reservation.setStatus("active");
        if (updateById(reservation)) {
            return Result.success("预约确认成功");
        } else {
            throw new BusinessException("预约确认失败");
        }
    }

    @Override
    public Result<?> updateReservationStatus(Long id, String status) {
        Reservation reservation = getById(id);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }

        reservation.setStatus(status);
        if (updateById(reservation)) {
            return Result.success("预约状态更新成功");
        } else {
            throw new BusinessException("预约状态更新失败");
        }
    }
}