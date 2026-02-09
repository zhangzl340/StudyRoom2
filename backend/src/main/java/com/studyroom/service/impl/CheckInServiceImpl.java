package com.studyroom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.entity.CheckIn;
import com.studyroom.entity.Reservation;
import com.studyroom.entity.Violation;
import com.studyroom.exception.BusinessException;
import com.studyroom.mapper.CheckInMapper;
import com.studyroom.mapper.ReservationMapper;
import com.studyroom.mapper.ViolationMapper;
import com.studyroom.service.CheckInService;
import com.studyroom.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class CheckInServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements CheckInService {

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private ViolationMapper violationMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Override
    public Result<?> checkIn(Long reservationId, Long userId, String method) {
        // 检查是否已经有签到记录
        CheckIn existingCheckIn = checkInMapper.selectByReservationId(reservationId).stream().findFirst().orElse(null);
        if (existingCheckIn != null) {
            throw new BusinessException("该预约已经签到");
        }

        // 更新预约表状态为使用中
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }
        reservation.setReservationStatus("使用中");
        reservation.setSignInTime(new Date());
        reservationMapper.updateById(reservation);

        CheckIn checkIn = new CheckIn();
        checkIn.setReservationId(reservationId);
        checkIn.setUserId(userId);
        checkIn.setCheckInTime(new Date());
        checkIn.setCheckInMethod(method);
        checkIn.setStatus("checked_in");

        if (save(checkIn)) {
            return Result.success("签到成功");
        } else {
            throw new BusinessException("签到失败");
        }
    }

    @Override
    public Result<?> checkOut(Long id) {
        CheckIn checkIn = getById(id);
        if (checkIn == null) {
            throw new BusinessException("签到记录不存在");
        }

        // 更新预约表状态为完成预约
        Long reservationId = checkIn.getReservationId();
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation != null) {
            reservation.setReservationStatus("完成预约");
            reservation.setSignOutTime(new Date());
            reservationMapper.updateById(reservation);
        }

        checkIn.setCheckOutTime(new Date());
        checkIn.setStatus("checked_out");
        if (updateById(checkIn)) {
            return Result.success("签出成功");
        } else {
            throw new BusinessException("签出失败");
        }
    }

    @Override
    public Result<?> leave(Long id) {
        CheckIn checkIn = getById(id);
        if (checkIn == null) {
            throw new BusinessException("签到记录不存在");
        }

        checkIn.setStatus("left");
        if (updateById(checkIn)) {
            return Result.success("暂离成功");
        } else {
            throw new BusinessException("暂离失败");
        }
    }

    @Override
    public Result<?> returnFromLeave(Long id) {
        CheckIn checkIn = getById(id);
        if (checkIn == null) {
            throw new BusinessException("签到记录不存在");
        }

        checkIn.setStatus("returned");
        if (updateById(checkIn)) {
            return Result.success("返回成功");
        } else {
            throw new BusinessException("返回失败");
        }
    }

    @Override
    public Result<?> getCheckInDetail(Long id) {
        CheckIn checkIn = getById(id);
        if (checkIn == null) {
            throw new BusinessException("签到记录不存在");
        }
        return Result.success(checkIn);
    }

    @Override
    public Result<?> getCheckInList(Long userId, String status, Integer page, Integer size) {
        // 这里应该实现分页查询，暂时跳过
        List<CheckIn> checkIns;
        if (userId != null) {
            checkIns = checkInMapper.selectByUserId(userId);
        } else if (status != null) {
            checkIns = checkInMapper.selectByStatus(status);
        } else {
            checkIns = list();
        }
        return Result.success(checkIns);
    }

    @Override
    public Result<?> getCurrentCheckIn(Long userId) {
        return Result.success(checkInMapper.selectCurrentByUserId(userId));
    }

    @Override
    public Result<?> generateCheckInQrcode(Long reservationId) {
        // 生成二维码内容
        String qrcodeContent = "CHECKIN:" + reservationId + ":" + UUID.randomUUID().toString();
        return Result.success(qrcodeContent);
    }

    @Override
    public Result<?> verifyCheckInQrcode(String qrcodeContent) {
        // 验证二维码内容
        if (qrcodeContent.startsWith("CHECKIN:")) {
            String[] parts = qrcodeContent.split(":");
            if (parts.length == 3) {
                Long reservationId = Long.parseLong(parts[1]);
                return Result.success(reservationId);
            }
        }
        return Result.error("无效的二维码");
    }

    @Override
    public Result<?> getCheckInStatistics(String startDate, String endDate) {
        // 这里应该实现统计逻辑，暂时跳过
        return Result.success("统计成功");
    }

    @Override
    public Result<?> createViolation(Violation violation) {
        violation.setStatus("processed");
        if (violationMapper.insert(violation) > 0) {
            return Result.success("违规记录创建成功");
        } else {
            throw new BusinessException("违规记录创建失败");
        }
    }

    @Override
    public Result<?> getViolationList(Long userId, String type, Integer page, Integer size) {
        // 这里应该实现分页查询，暂时跳过
        List<Violation> violations;
        if (userId != null) {
            violations = violationMapper.selectByUserId(userId);
        } else if (type != null) {
            violations = violationMapper.selectByType(type);
        } else {
            violations = violationMapper.selectList(null);
        }
        return Result.success(violations);
    }

    @Override
    public Result<?> getViolationDetail(Long id) {
        Violation violation = violationMapper.selectById(id);
        if (violation == null) {
            throw new BusinessException("违规记录不存在");
        }
        return Result.success(violation);
    }

    @Override
    public Result<?> updateViolationStatus(Long id, String status) {
        Violation violation = violationMapper.selectById(id);
        if (violation == null) {
            throw new BusinessException("违规记录不存在");
        }

        violation.setStatus(status);
        if (violationMapper.updateById(violation) > 0) {
            return Result.success("违规状态更新成功");
        } else {
            throw new BusinessException("违规状态更新失败");
        }
    }
}