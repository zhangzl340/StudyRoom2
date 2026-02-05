package com.studyroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.entity.Reservation;

import java.util.Date;
import java.util.List;

public interface ReservationMapper extends BaseMapper<Reservation> {

    List<Reservation> selectByUserId(Long userId);

    List<Reservation> selectBySeatId(Long seatId);

    List<Reservation> selectByStatus(String status);

    List<Reservation> selectByReservationStatus(String reservationStatus);

    List<Reservation> selectByTimeRange(Long seatId, Date reservationInTime, Date reservationOutTime);

    List<Reservation> selectUpcomingByUserId(Long userId);

    List<Reservation> selectTodayByUserId(Long userId);
}
