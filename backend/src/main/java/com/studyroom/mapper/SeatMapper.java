package com.studyroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.entity.Seat;

import java.util.List;

public interface SeatMapper extends BaseMapper<Seat> {

    List<Seat> selectByRoomId(Long roomId);

    List<Seat> selectAvailableSeatsByRoomId(Long roomId);

    Seat selectByRoomIdAndSeatNum(Long roomId, String seatNum);

    List<Seat> selectByStatus(String status);
}
