package com.studyroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.entity.Violation;

import java.util.List;

public interface ViolationMapper extends BaseMapper<Violation> {

    List<Violation> selectByUserId(Long userId);

    List<Violation> selectByReservationId(Long reservationId);

    List<Violation> selectByType(String type);

    List<Violation> selectByStatus(String status);

    List<Violation> selectByDateRange(String startDate, String endDate);
}