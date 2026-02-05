package com.studyroom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.entity.Room;
import com.studyroom.entity.Seat;
import com.studyroom.exception.BusinessException;
import com.studyroom.mapper.RoomMapper;
import com.studyroom.mapper.SeatMapper;
import com.studyroom.service.SeatService;
import com.studyroom.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class SeatServiceImpl extends ServiceImpl<SeatMapper, Seat> implements SeatService {

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Override
    @Transactional
    public Result<?> createSeats(Long roomId, List<Seat> seats) {
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("自习室不存在");
        }

        for (Seat seat : seats) {
            seat.setRoomId(roomId);
            seat.setStatus("available");
            seatMapper.insert(seat);
        }

        return Result.success("座位创建成功");
    }

    @Override
    public Result<?> updateSeatStatus(Long id, String status) {
        Seat seat = seatMapper.selectById(id);
        if (seat == null) {
            throw new BusinessException("座位不存在");
        }

        seat.setStatus(status);
        if (seatMapper.updateById(seat) > 0) {
            return Result.success("座位状态更新成功");
        } else {
            throw new BusinessException("座位状态更新失败");
        }
    }

    @Override
    public Result<?> getSeatsByRoomId(Long roomId) {
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("自习室不存在");
        }

        return Result.success(seatMapper.selectByRoomId(roomId));
    }

    @Override
    public Result<?> getAvailableSeatsByRoomId(Long roomId) {
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("自习室不存在");
        }

        return Result.success(seatMapper.selectAvailableSeatsByRoomId(roomId));
    }

    @Override
    @Transactional
    public Result<?> updateSeatLayout(Long roomId, List<Seat> seats) {
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("自习室不存在");
        }

        for (Seat seat : seats) {
            seat.setRoomId(roomId);
            seatMapper.updateById(seat);
        }

        return Result.success("座位布局更新成功");
    }

    @Override
    @Transactional
    public Result<?> importSeats(Long roomId, List<Seat> seats) {
        Room room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("自习室不存在");
        }

        for (Seat seat : seats) {
            seat.setRoomId(roomId);
            seat.setStatus("available");
            seatMapper.insert(seat);
        }

        return Result.success("座位导入成功");
    }

    @Override
    public Result<?> checkSeatAvailability(Long seatId, String startTime, String endTime) {
        // 这里应该实现座位可用性检查，暂时跳过
        return Result.success("座位可用");
    }
}
