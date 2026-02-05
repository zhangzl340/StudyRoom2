package com.studyroom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studyroom.entity.Room;
import com.studyroom.entity.Seat;
import com.studyroom.utils.Result;

import java.util.List;

public interface RoomService extends IService<Room> {

    Result<?> createRoom(Room room);

    Result<?> updateRoom(Long id, Room room);

    Result<?> deleteRoom(Long id);

    Result<?> getRoomList(String building, Integer floor, String status, Integer page, Integer size);

    Result<?> getRoomDetail(Long id);

    Result<?> getAvailableRooms();

    Result<?> updateRoomStatus(Long id, String status);

    Result<?> getRoomStatus(Long id);
}