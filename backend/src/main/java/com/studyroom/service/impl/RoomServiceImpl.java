package com.studyroom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.entity.Room;
import com.studyroom.exception.BusinessException;
import com.studyroom.mapper.RoomMapper;
import com.studyroom.service.RoomService;
import com.studyroom.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    @Autowired
    private RoomMapper roomMapper;

    @Override
    public Result<?> createRoom(Room room) {
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
}