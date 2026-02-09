package com.studyroom.controller;

import com.studyroom.service.DashboardService;
import com.studyroom.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "仪表盘模块", description = "仪表盘数据统计")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/statistics")
    public Result<?> getDashboardStatistics() {
        return dashboardService.getDashboardStatistics();
    }

    @Operation(summary = "获取使用率趋势数据")
    @GetMapping("/trend")
    public Result<?> getUsageTrend() {
        return dashboardService.getUsageTrend();
    }

    @Operation(summary = "获取自习室热度排行")
    @GetMapping("/room/ranking")
    public Result<?> getRoomRanking() {
        return dashboardService.getRoomRanking();
    }

    @Operation(summary = "获取用户学院分布")
    @GetMapping("/user/college")
    public Result<?> getUserCollegeDistribution() {
        return dashboardService.getUserCollegeDistribution();
    }

    @Operation(summary = "获取24小时热力图数据")
    @GetMapping("/heatmap")
    public Result<?> getHeatmapData() {
        return dashboardService.getHeatmapData();
    }
}
