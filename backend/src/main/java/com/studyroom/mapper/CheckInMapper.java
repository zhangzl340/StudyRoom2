package com.studyroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.entity.CheckIn;

import java.util.List;

public interface CheckInMapper extends BaseMapper<CheckIn> {

    List<CheckIn> selectByUserId(Long userId);

    List<CheckIn> selectByReservationId(Long reservationId);

    List<CheckIn> selectByStatus(String status);

    CheckIn selectCurrentByUserId(Long userId);

    List<CheckIn> selectByDateRange(String startDate, String endDate);
}