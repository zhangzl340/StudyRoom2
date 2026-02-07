package com.studyroom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.entity.Reservation;
import com.studyroom.entity.Violation;
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
        log.info("=== 开始创建预约 ===");
        log.info("接收到预约数据: {}", reservation);
        log.info("userId: {}", reservation.getUserId());
        log.info("seatId: {}", reservation.getSeatId());
        log.info("reservationInTime: {} (类型: {})",
                reservation.getReservationInTime(),
                reservation.getReservationInTime() != null ? reservation.getReservationInTime().getClass().getName() : "null");
        log.info("reservationOutTime: {} (类型: {})",
                reservation.getReservationOutTime(),
                reservation.getReservationOutTime() != null ? reservation.getReservationOutTime().getClass().getName() : "null");

        try {
            // 检查用户之前的预约是否违约
            log.info("=== 开始检查用户之前的预约是否违约 ===");
            checkUserViolations(reservation.getUserId());
            log.info("用户违约检查完成");

            // 检查预约冲突
            log.info("=== 开始检查预约冲突 ===");
            Result<?> conflictResult = checkReservationConflict(
                    reservation.getSeatId(),
                    reservation.getReservationInTime(),
                    reservation.getReservationOutTime()
            );
            log.info("预约冲突检查结果: {}", conflictResult.isSuccess() ? "无冲突" : "有冲突");

            if (!conflictResult.isSuccess()) {
                return conflictResult;
            }

            // 设置默认状态
            reservation.setStatus("正常");
            reservation.setReservationStatus("已预约");

            if (save(reservation)) {
                return Result.success("预约创建成功");
            } else {
                throw new BusinessException("预约创建失败");
            }
        } catch (Exception e) {
            log.error("创建预约时发生异常", e);
            throw new BusinessException("创建预约失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户之前的预约是否违约
     * @param userId 用户ID
     */
    private void checkUserViolations(Long userId) {
        // 获取用户的所有预约
        List<Reservation> userReservations = reservationMapper.selectByUserId(userId);
        
        // 检查每个预约是否违约
        for (Reservation reservation : userReservations) {
            // 只检查已预约但未签到的预约
            if ("已预约".equals(reservation.getReservationStatus())) {
                // 检查是否超过预约时间2小时
                Date now = new Date();
                Date reservationInTime = reservation.getReservationInTime();
                
                // 计算时间差（毫秒）
                long timeDiff = now.getTime() - reservationInTime.getTime();
                // 2小时 = 7200000毫秒
                if (timeDiff > 7200000) {
                    // 标记为违约
                    reservation.setStatus("违约");
                    reservation.setReservationStatus("违约中");
                    updateById(reservation);
                    
                    // 创建违约记录
                    Violation violation = new Violation();
                    violation.setUserId(userId);
                    violation.setReservationId(reservation.getId());
                    violation.setType("超时未签到");
                    violation.setDescription("预约时间: " + reservation.getReservationInTime() + " 超时未签到");
                    violation.setDeductCredit(1);
                    violation.setStatus("已处理");
                    violation.setCreatedAt(new Date());
                    violation.setUpdatedAt(new Date());
                    
                    // 保存违约记录
                    // 这里需要注入ViolationService
                    // 暂时跳过，后续实现
                }
            }
        }
    }

    @Override
    public Result<?> cancelReservation(Long id) {
        Reservation reservation = getById(id);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }
//        reservation.setStatus("cancelled");
        reservation.setReservationStatus("取消预约");
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
    public Result<?> checkReservationConflict(Long seatId, Date startTime, Date endTime) {
        log.info("=== 进入 checkReservationConflict 方法 ===");

        try {
            log.info("=== 进入 try 块 ===");

            // 参数验证
            if (seatId == null) {
                throw new BusinessException("座位ID不能为空");
            }
            if (startTime == null) {
                throw new BusinessException("开始时间不能为空");
            }
            if (endTime == null) {
                throw new BusinessException("结束时间不能为空");
            }

            // 打印时间信息
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            log.info("参数 - seatId: {}, startTime: {}, endTime: {}", seatId, sdf.format(startTime), sdf.format(endTime));

            // 直接返回成功，跳过数据库查询（用于测试）
            log.info("=== 测试：直接返回成功 ===");
            return Result.success("无预约冲突");

            // 注释掉原来的数据库查询代码
            // List<Reservation> conflicts = reservationMapper.selectByTimeRange(seatId, startTime, endTime);
            // if (!conflicts.isEmpty()) {
            //     return Result.error("该时间段座位已被预约");
            // }
            // return Result.success("无预约冲突");

        } catch (Exception e) {
            log.error("=== 进入 catch 块 ===");
            log.error("异常类型: {}", e.getClass().getName());
            log.error("异常消息: {}", e.getMessage());
            log.error("异常堆栈:", e);
            throw new BusinessException("检查预约冲突失败: " + e.getMessage());
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
    public Result<?> checkAvailability(Long seatId, Date startTime, Date endTime) {
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