package com.studyroom.service.impl;

import com.studyroom.mapper.ReservationMapper;
import com.studyroom.mapper.RoomMapper;
import com.studyroom.service.DashboardService;
import com.studyroom.service.ReservationService;
import com.studyroom.service.RoomService;
import com.studyroom.service.SeatService;
import com.studyroom.service.UserService;
import com.studyroom.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private RoomService roomService;

    @Autowired
    private SeatService seatService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private ReservationMapper reservationMapper;
    
    @Autowired
    private RoomMapper roomMapper;

    @Override
    public Result<?> getDashboardStatistics() {
        try {
            log.info("=== 获取仪表盘统计数据 ===");

            // 1. 获取总座位数
            int totalSeats = getTotalSeats();
            log.info("总座位数: {}", totalSeats);

            // 2. 获取当前占用数
            int currentOccupancy = getCurrentOccupancy();
            log.info("当前占用数: {}", currentOccupancy);

            // 3. 计算使用率
            double usageRate = totalSeats > 0 ? Math.round((double) currentOccupancy / totalSeats * 100) : 0;
            log.info("使用率: {}%", usageRate);

            // 4. 获取今日预约数
            int todayReservations = getTodayReservations();
            log.info("今日预约数: {}", todayReservations);

            // 5. 计算昨日预约变化率
            int yesterdayReservations = getYesterdayReservations();
            double reservationChangeRate = yesterdayReservations > 0 ? 
                    Math.round(((double) todayReservations - yesterdayReservations) / yesterdayReservations * 100) : 0;
            log.info("昨日预约数: {}, 变化率: {}%", yesterdayReservations, reservationChangeRate);

            // 6. 计算爽约率
            double noShowRate = calculateNoShowRate();
            log.info("爽约率: {}%", noShowRate);

            // 7. 获取高峰时段
            String peakTime = getPeakTime();
            log.info("高峰时段: {}", peakTime);

            // 构建返回数据
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalSeats", totalSeats);
            statistics.put("currentOccupancy", currentOccupancy);
            statistics.put("usageRate", usageRate);
            statistics.put("todayReservations", todayReservations);
            statistics.put("yesterdayReservationChange", reservationChangeRate);
            statistics.put("noShowRate", noShowRate);
            statistics.put("peakTime", peakTime);

            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取仪表盘统计数据失败", e);
            return Result.error("获取仪表盘统计数据失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> getUsageTrend() {
        try {
            log.info("=== 获取预约趋势数据 ===");

            // 计算近7天的开始日期和结束日期
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -6); // 7天前
            java.util.Date startDate = calendar.getTime();
            java.util.Date endDate = new java.util.Date(); // 今天

            log.info("开始日期: {}, 结束日期: {}", startDate, endDate);

            // 从数据库获取近7天的预约数据
            List<Map<String, Object>> reservationData = reservationMapper.countReservationsByDateRange(startDate, endDate);
            log.info("数据库返回的预约数据: {}", reservationData);

            // 构建完整的近7天数据，包括没有预约的日期
            List<Map<String, Object>> trendData = new ArrayList<>();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd");
            java.text.SimpleDateFormat dbSdf = new java.text.SimpleDateFormat("yyyy-MM-dd");

            // 创建日期到预约数的映射
            java.util.Map<String, Integer> reservationMap = new java.util.HashMap<>();
            for (Map<String, Object> item : reservationData) {
                String dateStr = item.get("date").toString();
                Integer count = Integer.parseInt(item.get("count").toString());
                reservationMap.put(dateStr, count);
            }

            // 生成近7天的日期数据
            for (int i = 0; i < 7; i++) {
                java.util.Calendar currentCalendar = java.util.Calendar.getInstance();
                currentCalendar.add(java.util.Calendar.DAY_OF_YEAR, -6 + i);
                java.util.Date currentDate = currentCalendar.getTime();
                String dateKey = dbSdf.format(currentDate);
                String dateLabel = sdf.format(currentDate);
                
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("date", dateLabel);
                dataPoint.put("count", reservationMap.getOrDefault(dateKey, 0));
                trendData.add(dataPoint);
            }

            log.info("生成的预约趋势数据: {}", trendData);
            return Result.success(trendData);
        } catch (Exception e) {
            log.error("获取预约趋势数据失败", e);
            return Result.error("获取预约趋势数据失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> getRoomRanking() {
        try {
            log.info("=== 获取自习室热度排行 ===");

            // 从数据库获取自习室热度排行数据
            List<Map<String, Object>> rankingData = roomMapper.selectRoomRanking();
            log.info("自习室热度排行数据: {}", rankingData);

            // 如果没有数据，返回空列表
            if (rankingData == null || rankingData.isEmpty()) {
                return Result.success(new ArrayList<>());
            }

            return Result.success(rankingData);
        } catch (Exception e) {
            log.error("获取自习室热度排行失败", e);
            return Result.error("获取自习室热度排行失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> getUserCollegeDistribution() {
        try {
            log.info("=== 获取用户学院分布 ===");

            // 从数据库获取用户学院分布数据
            List<Map<String, Object>> collegeData = userService.getUserCollegeDistribution();
            
            // 如果没有数据，返回空列表
            if (collegeData == null || collegeData.isEmpty()) {
                log.info("未获取到用户学院分布数据");
                return Result.success(new ArrayList<>());
            }

            log.info("用户学院分布数据: {}", collegeData);
            return Result.success(collegeData);
        } catch (Exception e) {
            log.error("获取用户学院分布失败", e);
            return Result.error("获取用户学院分布失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> getHeatmapData() {
        try {
            log.info("=== 获取24小时热力图数据 ===");

            // 从数据库获取自习室24小时热力图数据
            List<Map<String, Object>> heatmapRawData = roomMapper.selectRoomHeatmapData();
            log.info("热力图原始数据: {}", heatmapRawData);

            // 提取所有自习室名称
            java.util.Set<String> roomSet = new java.util.HashSet<>();
            // 直接从数据库获取所有自习室，确保即使没有预约记录也能显示
            Result<?> roomResult = roomService.getRoomList(null, null, null, null, null);
            if (roomResult.isSuccess() && roomResult.getData() instanceof List) {
                List<?> allRooms = (List<?>) roomResult.getData();
                for (Object room : allRooms) {
                    if (room instanceof com.studyroom.entity.Room) {
                        com.studyroom.entity.Room roomEntity = (com.studyroom.entity.Room) room;
                        roomSet.add(roomEntity.getName());
                    }
                }
            }

            List<String> rooms = new java.util.ArrayList<>(roomSet);

            // 如果没有自习室数据，使用默认自习室
            if (rooms.isEmpty()) {
                rooms = List.of("图书馆A区", "教学楼B301", "图书馆C区", "实验楼D202", "教学楼A105");
            }

            // 构建热力图数据
            List<List<Object>> heatmapData = new ArrayList<>();

            // 创建房间索引映射
            java.util.Map<String, Integer> roomIndexMap = new java.util.HashMap<>();
            for (int i = 0; i < rooms.size(); i++) {
                roomIndexMap.put(rooms.get(i), i);
            }

            // 构建小时到房间到预约数的映射
            java.util.Map<Integer, java.util.Map<String, Integer>> hourRoomCountMap = new java.util.HashMap<>();
            for (Map<String, Object> item : heatmapRawData) {
                try {
                    String roomName = item.get("room_name").toString();
                    Object hourObj = item.get("hour");
                    if (hourObj != null) {
                        int hour = Integer.parseInt(hourObj.toString());
                        Object countObj = item.get("count");
                        int count = countObj != null ? Integer.parseInt(countObj.toString()) : 0;

                        hourRoomCountMap.computeIfAbsent(hour, k -> new java.util.HashMap<>())
                                .put(roomName, count);
                    }
                } catch (Exception e) {
                    log.warn("处理热力图数据时出现异常: {}", e.getMessage());
                    continue;
                }
            }

            // 生成热力图数据
            for (int roomIndex = 0; roomIndex < rooms.size(); roomIndex++) {
                String roomName = rooms.get(roomIndex);
                for (int hour = 0; hour < 24; hour++) {
                    java.util.Map<String, Integer> roomCountMap = hourRoomCountMap.get(hour);
                    int count = roomCountMap != null ? roomCountMap.getOrDefault(roomName, 0) : 0;
                    // 将预约数转换为占用率（假设每个自习室有100个座位）
                    int occupancyRate = Math.min(100, count * 10); // 简化计算，实际应该根据座位数计算
                    heatmapData.add(List.of(hour, roomIndex, occupancyRate));
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("rooms", rooms);
            result.put("data", heatmapData);

            log.info("24小时热力图数据: {}", result);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取24小时热力图数据失败", e);
            return Result.error("获取24小时热力图数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取总座位数
     * @return 总座位数
     */
    private int getTotalSeats() {
        try {
            // 1. 获取所有房间
            Result<?> roomResult = roomService.getRoomList(null, null, null, null, null);
            if (roomResult.isSuccess() && roomResult.getData() instanceof List) {
                List<?> rooms = (List<?>) roomResult.getData();
                int totalSeats = 0;
                
                // 2. 遍历每个房间，获取其座位数
                for (Object room : rooms) {
                    if (room instanceof com.studyroom.entity.Room) {
                        // 正确处理Room实体对象
                        com.studyroom.entity.Room roomEntity = (com.studyroom.entity.Room) room;
                        Long roomId = roomEntity.getId();
                        Result<?> seatResult = seatService.getSeatsByRoomId(roomId);
                        if (seatResult.isSuccess() && seatResult.getData() instanceof List) {
                            totalSeats += ((List<?>) seatResult.getData()).size();
                        }
                    }
                }
                
                log.info("总座位数: {}", totalSeats);
                return totalSeats;
            }
            // 暂时返回模拟数据
            return 100;
        } catch (Exception e) {
            log.error("获取总座位数失败", e);
            return 100;
        }
    }

    /**
     * 获取当前占用数
     * @return 当前占用数
     */
    private int getCurrentOccupancy() {
        try {
            Result<?> result = reservationService.getReservationList(null, "active", null, null);
            if (result.isSuccess() && result.getData() instanceof List) {
                return ((List<?>) result.getData()).size();
            }
            // 暂时返回模拟数据
            return 67;
        } catch (Exception e) {
            log.error("获取当前占用数失败", e);
            return 67;
        }
    }

    /**
     * 获取今日预约数
     * @return 今日预约数
     */
    private int getTodayReservations() {
        try {
            // 从数据库获取今日预约数
            int count = reservationMapper.countTodayReservations();
            log.info("今日预约数: {}", count);
            return count;
        } catch (Exception e) {
            log.error("获取今日预约数失败", e);
            return 0;
        }
    }

    /**
     * 获取昨日预约数
     * @return 昨日预约数
     */
    private int getYesterdayReservations() {
        try {
            // 从数据库获取昨日预约数
            int count = reservationMapper.countYesterdayReservations();
            log.info("昨日预约数: {}", count);
            return count;
        } catch (Exception e) {
            log.error("获取昨日预约数失败", e);
            return 0;
        }
    }

    /**
     * 计算爽约率
     * @return 爽约率
     */
    private double calculateNoShowRate() {
        try {
            // 从数据库获取爽约次数和总预约次数
            int noShowCount = reservationMapper.countNoShowReservations();
            int totalCount = reservationMapper.countTotalReservations();
            
            // 计算爽约率
            double noShowRate = totalCount > 0 ? Math.round((double) noShowCount / totalCount * 1000) / 10.0 : 0;
            log.info("爽约次数: {}, 总预约次数: {}, 爽约率: {}%", noShowCount, totalCount, noShowRate);
            return noShowRate;
        } catch (Exception e) {
            log.error("计算爽约率失败", e);
            return 0;
        }
    }

    /**
     * 获取高峰时段
     * @return 高峰时段
     */
    private String getPeakTime() {
        try {
            // 这里应该根据历史数据计算高峰时段
            // 暂时返回固定值
            return "19:00-21:00";
        } catch (Exception e) {
            log.error("获取高峰时段失败", e);
            return "19:00-21:00";
        }
    }
}
