package com.studyroom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studyroom.entity.Reservation;
import com.studyroom.utils.Result;

import java.util.List;

public interface ReservationService extends IService<Reservation> {

    Result<?> createReservation(Reservation reservation);

    Result<?> cancelReservation(Long id);

    Result<?> getReservationList(Long userId, String status, Integer page, Integer size);

    Result<?> getReservationDetail(Long id);

    Result<?> updateReservation(Long id, Reservation reservation);

    Result<?> checkReservationConflict(Long roomId, Long seatId, String startTime, String endTime);

    Result<?> calculateReservationFee(Long id);

    Result<?> getUpcomingReservations(Long userId);

    Result<?> getTodayReservations(Long userId);

    Result<?> checkAvailability(Long roomId, Long seatId, String startTime, String endTime);

    Result<?> getReservationStatistics();

    Result<?> confirmReservation(Long id);

    Result<?> updateReservationStatus(Long id, String status);
}