package com.studyroom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studyroom.entity.Seat;
import com.studyroom.utils.Result;

import java.util.List;

public interface SeatService extends IService<Seat> {

    Result<?> createSeats(Long roomId, List<Seat> seats);

    Result<?> updateSeatStatus(Long id, String status);

    Result<?> getSeatsByRoomId(Long roomId);

    Result<?> getAvailableSeatsByRoomId(Long roomId);

    Result<?> updateSeatLayout(Long roomId, List<Seat> seats);

    Result<?> importSeats(Long roomId, List<Seat> seats);

    Result<?> checkSeatAvailability(Long seatId, String startTime, String endTime);
}
