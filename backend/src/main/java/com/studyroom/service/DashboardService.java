package com.studyroom.service;

import com.studyroom.utils.Result;

public interface DashboardService {

    /**
     * 获取仪表盘统计数据
     * @return 统计数据
     */
    Result<?> getDashboardStatistics();

    /**
     * 获取使用率趋势数据
     * @return 趋势数据
     */
    Result<?> getUsageTrend();

    /**
     * 获取自习室热度排行
     * @return 热度排行数据
     */
    Result<?> getRoomRanking();

    /**
     * 获取用户学院分布
     * @return 学院分布数据
     */
    Result<?> getUserCollegeDistribution();

    /**
     * 获取24小时热力图数据
     * @return 热力图数据
     */
    Result<?> getHeatmapData();
}
