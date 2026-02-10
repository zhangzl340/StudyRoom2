package com.studyroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studyroom.entity.Room;

import java.util.List;

public interface RoomMapper extends BaseMapper<Room> {

    List<Room> selectAvailableRooms();

    Room selectByCode(String code);

    List<Room> selectByBuilding(String building);

    List<Room> selectByFloor(Integer floor);
    
    java.util.List<java.util.Map<String, Object>> selectRoomRanking();
    
    java.util.List<java.util.Map<String, Object>> selectRoomHeatmapData();
}