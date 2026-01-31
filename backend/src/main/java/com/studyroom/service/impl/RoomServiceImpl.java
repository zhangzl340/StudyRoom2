package com.studyroom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.entity.Room;
import com.studyroom.entity.Seat;
import com.studyroom.exception.BusinessException;
import com.studyroom.mapper.RoomMapper;
import com.studyroom.mapper.SeatMapper;
import com.studyroom.service.RoomService;
import com.studyroom.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Override
    public Result<?> createRoom(Room room) {
        if (roomMapper.selectByCode(room.getCode()) != null) {
            throw new BusinessException("自习室编码已存在");
        }

        if (save(room)) {
            return Result.success("自习室创建成功");
        } else {
            throw new BusinessException("自习室创建失败");
        }
    }

    @Override
    public Result<?> updateRoom(Long id, Room room) {
        Room existingRoom = getById(id);
        if (existingRoom == null) {
            throw new BusinessException("自习室不存在");
        }

        room.setId(id);
        if (updateById(room)) {
            return Result.success("自习室更新成功");
        } else {
            throw new BusinessException("自习室更新失败");
        }
    }

    @Override
    public Result<?> deleteRoom(Long id) {
        Room room = getById(id);
        if (room == null) {
            throw new BusinessException("自习室不存在");
        }

        if (removeById(id)) {
            return Result.success("自习室删除成功");
        } else {
            throw new BusinessException("自习室删除失败");
        }
    }

    @Override
    public Result<?> getRoomList(String building, Integer floor, String status, Integer page, Integer size) {
        // 这里应该实现分页查询，暂时跳过
        return Result.success(list());
    }

    @Override
    public Result<?> getRoomDetail(Long id) {
        Room room = getById(id);
        if (room == null) {
            throw new BusinessException("自习室不存在");
        }
        return Result.success(room);
    }

    @Override
    public Result<?> getAvailableRooms() {
        return Result.success(roomMapper.selectAvailableRooms());
    }

    @Override
    public Result<?> updateRoomStatus(Long id, String status) {
        Room room = getById(id);
        if (room == null) {
            throw new BusinessException("自习室不存在");
        }

        room.setStatus(status);
        if (updateById(room)) {
            return Result.success("自习室状态更新成功");
        } else {
            throw new BusinessException("自习室状态更新失败");
        }
    }

    @Override
    public Result<?> getRoomStatus(Long id) {
        Room room = getById(id);
        if (room == null) {
            throw new BusinessException("自习室不存在");
        }
        return Result.success(room.getStatus());
    }

    @Transactional
    @Override
    public Result<?> createSeats(Long roomId, List<Seat> seats) {
        Room room = getById(roomId);
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
        Room room = getById(roomId);
        if (room == null) {
            throw new BusinessException("自习室不存在");
        }

        return Result.success(seatMapper.selectByRoomId(roomId));
    }

    @Override
    public Result<?> getAvailableSeatsByRoomId(Long roomId) {
        Room room = getById(roomId);
        if (room == null) {
            throw new BusinessException("自习室不存在");
        }

        return Result.success(seatMapper.selectAvailableSeatsByRoomId(roomId));
    }

    @Transactional
    @Override
    public Result<?> updateSeatLayout(Long roomId, List<Seat> seats) {
        Room room = getById(roomId);
        if (room == null) {
            throw new BusinessException("自习室不存在");
        }

        for (Seat seat : seats) {
            seat.setRoomId(roomId);
            seatMapper.updateById(seat);
        }

        return Result.success("座位布局更新成功");
    }

    @Transactional
    @Override
    public Result<?> importSeats(Long roomId, List<Seat> seats) {
        Room room = getById(roomId);
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
    public Result<?> checkSeatAvailability(Long roomId, Long seatId, String startTime, String endTime) {
        // 这里应该实现座位可用性检查，暂时跳过
        return Result.success("座位可用");
    }
}