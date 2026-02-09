package com.studyroom.service.impl;

import com.studyroom.mapper.ReservationMapper;
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
            log.info("=== 获取使用率趋势数据 ===");

            // 生成近7天的使用率数据
            List<Map<String, Object>> trendData = new ArrayList<>();
            String[] dates = {"12/29", "12/30", "12/31", "1/1", "1/2", "1/3", "1/4"};
            int[] usageRates = {65, 72, 58, 45, 35, 42, 68};

            for (int i = 0; i < dates.length; i++) {
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("date", dates[i]);
                dataPoint.put("usageRate", usageRates[i]);
                trendData.add(dataPoint);
            }

            return Result.success(trendData);
        } catch (Exception e) {
            log.error("获取使用率趋势数据失败", e);
            return Result.error("获取使用率趋势数据失败: " + e.getMessage());
        }
    }

    @Override
    public Result<?> getRoomRanking() {
        try {
            log.info("=== 获取自习室热度排行 ===");

            // 生成自习室热度数据
            List<Map<String, Object>> rankingData = new ArrayList<>();
            rankingData.add(Map.of("name", "图书馆A区", "value", 95));
            rankingData.add(Map.of("name", "教学楼B301", "value", 82));
            rankingData.add(Map.of("name", "图书馆C区", "value", 70));
            rankingData.add(Map.of("name", "实验楼D202", "value", 61));
            rankingData.add(Map.of("name", "教学楼A105", "value", 40));

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

            // 生成24小时热力图数据
            List<String> rooms = List.of("图书馆A区", "教学楼B301", "图书馆C区", "实验楼D202", "教学楼A105");
            List<List<Object>> heatmapData = new ArrayList<>();

            for (int roomIndex = 0; roomIndex < rooms.size(); roomIndex++) {
                for (int hour = 0; hour < 24; hour++) {
                    int occupancyRate;
                    if (hour >= 7 && hour <= 23) {
                        // 不同自习室的基础热度
                        int[] baseRates = {80, 75, 70, 65, 60};
                        int baseRate = baseRates[roomIndex];
                        // 高峰时段（18:00-22:00）热度提升
                        double peakFactor = (hour >= 18 && hour <= 22) ? 1.2 : 1;
                        // 随机波动
                        occupancyRate = Math.min(100, (int) Math.round(baseRate * peakFactor * (0.9 + Math.random() * 0.2)));
                    } else {
                        // 非营业时间，热度较低
                        occupancyRate = (int) Math.round(Math.random() * 10);
                    }
                    heatmapData.add(List.of(hour, roomIndex, occupancyRate));
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("rooms", rooms);
            result.put("data", heatmapData);

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
