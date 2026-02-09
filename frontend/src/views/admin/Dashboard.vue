<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Calendar, OfficeBuilding, User, DataLine, HotWater, ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { useRoomStore } from '../../stores/room'
import { useReservationStore } from '../../stores/reservation'
import { useAuthStore } from '../../stores/auth'
import { useUserStore } from '../../stores/user'
import * as echarts from 'echarts'

const roomStore = useRoomStore()
const reservationStore = useReservationStore()
const authStore = useAuthStore()
const userStore = useUserStore()

// 统计数据
const stats = ref({
  totalSeats: 0,
  currentOccupancy: 0,
  todayReservations: 0,
  noShowRate: 0,
  usageRate: 0,
  yesterdayReservationChange: 0
})

// 高峰时段
const peakTime = ref('19:00–21:00')

// 图表实例
const trendChart = ref(null)
const heatRankChart = ref(null)
const collegeChart = ref(null)
const heatmapChart = ref(null)

// 加载状态
const loading = ref(true)

// 获取统计数据
const getStats = async () => {
  try {
    // 从后端 API 获取仪表盘统计数据
    const response = await fetch('/api/dashboard/statistics')
    const data = await response.json()
    
    if (data.success) {
      const statsData = data.data
      stats.value.totalSeats = statsData.totalSeats
      stats.value.currentOccupancy = statsData.currentOccupancy
      stats.value.usageRate = statsData.usageRate
      stats.value.todayReservations = statsData.todayReservations
      stats.value.yesterdayReservationChange = statsData.yesterdayReservationChange
      stats.value.noShowRate = statsData.noShowRate
      peakTime.value = statsData.peakTime
    } else {
      throw new Error(data.message || '获取统计数据失败')
    }
    
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
    
    // 备用方案：使用模拟数据
    stats.value.totalSeats = 100
    stats.value.currentOccupancy = 67
    stats.value.usageRate = 67
    stats.value.todayReservations = Math.floor(Math.random() * 100) + 150
    stats.value.yesterdayReservationChange = Math.floor(Math.random() * 20) - 5
    stats.value.noShowRate = 7.3
    peakTime.value = '19:00-21:00'
  } finally {
    loading.value = false
    // 初始化图表
    await nextTick()
    initTrendChart()
    initHeatRankChart()
    initCollegeChart()
    initHeatmapChart()
  }
}

// 导出数据
const exportData = () => {
  ElMessage.success('数据导出成功')
}

// 使用率趋势图（折线+面积填充）
const initTrendChart = async () => {
  const chartDom = document.getElementById('trend-chart')
  if (!chartDom) return
  
  const myChart = echarts.init(chartDom)
  trendChart.value = myChart
  
  try {
    // 从后端 API 获取趋势数据
    const response = await fetch('/api/dashboard/trend')
    const data = await response.json()
    
    let dates = ['12/29', '12/30', '12/31', '1/1', '1/2', '1/3', '1/4']
    let usageRates = [65, 72, 58, 45, 35, 42, 68]
    
    if (data.success && data.data) {
      const trendData = data.data
      dates = trendData.map(item => item.date)
      usageRates = trendData.map(item => item.usageRate)
    }
    
    const option = {
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: dates,
        axisLine: { lineStyle: { color: '#e0e0e0' } },
        axisLabel: { color: '#666' }
      },
      yAxis: {
        type: 'value',
        name: '使用率(%)',
        nameTextStyle: { color: '#666' },
        axisLine: { lineStyle: { color: '#e0e0e0' } },
        axisLabel: { color: '#666' },
        splitLine: { lineStyle: { color: '#f0f0f0' } }
      },
      series: [{
        data: usageRates,
        type: 'line',
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        lineStyle: { color: '#409eff', width: 3 },
        itemStyle: { color: '#409eff', borderRadius: 4 },
        symbol: 'circle',
        symbolSize: 6,
        smooth: true
      }]
    }
    
    myChart.setOption(option)
  } catch (error) {
    console.error('获取趋势数据失败:', error)
    // 使用默认数据
    const option = {
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: ['12/29', '12/30', '12/31', '1/1', '1/2', '1/3', '1/4'],
        axisLine: { lineStyle: { color: '#e0e0e0' } },
        axisLabel: { color: '#666' }
      },
      yAxis: {
        type: 'value',
        name: '使用率(%)',
        nameTextStyle: { color: '#666' },
        axisLine: { lineStyle: { color: '#e0e0e0' } },
        axisLabel: { color: '#666' },
        splitLine: { lineStyle: { color: '#f0f0f0' } }
      },
      series: [{
        data: [65, 72, 58, 45, 35, 42, 68],
        type: 'line',
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        lineStyle: { color: '#409eff', width: 3 },
        itemStyle: { color: '#409eff', borderRadius: 4 },
        symbol: 'circle',
        symbolSize: 6,
        smooth: true
      }]
    }
    myChart.setOption(option)
  }
  
  window.addEventListener('resize', () => myChart.resize())
}

// 自习室热度排行（横向柱状图）
const initHeatRankChart = async () => {
  const chartDom = document.getElementById('heat-rank-chart')
  if (!chartDom) return
  
  const myChart = echarts.init(chartDom)
  heatRankChart.value = myChart
  
  try {
    // 从后端 API 获取自习室热度排行数据
    const response = await fetch('/api/dashboard/room/ranking')
    const data = await response.json()
    
    let roomData = [
      { name: '图书馆A区', value: 95 },
      { name: '教学楼B301', value: 82 },
      { name: '图书馆C区', value: 70 },
      { name: '实验楼D202', value: 61 },
      { name: '教学楼A105', value: 40 }
    ]
    
    if (data.success && data.data) {
      roomData = data.data
    }
    
    const option = {
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '10%', right: '15%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'value',
        axisLine: { lineStyle: { color: '#e0e0e0' } },
        axisLabel: { color: '#666' },
        splitLine: { lineStyle: { color: '#f0f0f0' } }
      },
      yAxis: {
        type: 'category',
        data: roomData.map(item => item.name),
        axisLine: { lineStyle: { color: '#e0e0e0' } },
        axisLabel: { color: '#666' }
      },
      series: [{
        data: roomData.map(item => item.value),
        type: 'bar',
        barWidth: 20,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
            { offset: 0, color: '#409eff' },
            { offset: 1, color: '#87ceeb' }
          ])
        },
        label: {
          show: true,
          position: 'right',
          formatter: '{c} 人',
          color: '#666'
        }
      }]
    }
    
    myChart.setOption(option)
  } catch (error) {
    console.error('获取自习室热度排行失败:', error)
    // 使用默认数据
    const roomData = [
      { name: '图书馆A区', value: 95 },
      { name: '教学楼B301', value: 82 },
      { name: '图书馆C区', value: 70 },
      { name: '实验楼D202', value: 61 },
      { name: '教学楼A105', value: 40 }
    ]
    
    const option = {
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '10%', right: '15%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'value',
        axisLine: { lineStyle: { color: '#e0e0e0' } },
        axisLabel: { color: '#666' },
        splitLine: { lineStyle: { color: '#f0f0f0' } }
      },
      yAxis: {
        type: 'category',
        data: roomData.map(item => item.name),
        axisLine: { lineStyle: { color: '#e0e0e0' } },
        axisLabel: { color: '#666' }
      },
      series: [{
        data: roomData.map(item => item.value),
        type: 'bar',
        barWidth: 20,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
            { offset: 0, color: '#409eff' },
            { offset: 1, color: '#87ceeb' }
          ])
        },
        label: {
          show: true,
          position: 'right',
          formatter: '{c} 人',
          color: '#666'
        }
      }]
    }
    myChart.setOption(option)
  }
  
  window.addEventListener('resize', () => myChart.resize())
}

// 用户学院分布（环形图）
const initCollegeChart = async () => {
  const chartDom = document.getElementById('college-chart')
  if (!chartDom) return
  
  const myChart = echarts.init(chartDom)
  collegeChart.value = myChart
  
  try {
    // 从后端 API 获取用户学院分布数据
    const response = await fetch('/api/dashboard/user/college')
    const data = await response.json()
    
    let collegeData = [
      { value: 52, name: '数理学院' },
      { value: 45, name: '外国语学院' },
      { value: 40, name: '环境与资源学院' },
      { value: 36, name: '信息与控制工程学院' },
      { value: 31, name: '航空航天学院' },
      { value: 27, name: '法学院' },
      { value: 25, name: '计算机科学与技术学院' },
      { value: 24, name: '文学与艺术学院' },
      { value: 23, name: '生命科学与农林学院' },
      { value: 22, name: '经济管理学院' },
      { value: 19, name: '土木工程与建筑学院' },
      { value: 16, name: '医学院' },
      { value: 15, name: '制造科学与工程学院' },
      { value: 14, name: '马克思主义学院' },
      { value: 13, name: '国防科学学院' },
      { value: 11, name: '应用技术学院' },
      { value: 10, name: '体育与健康学院' },
      { value: 9, name: '继续教育学院' }
    ]
    
    if (data.success && data.data) {
      collegeData = data.data
    }
    
    const option = {
      tooltip: { trigger: 'item', formatter: '{b}: {c} 人 ({d}%)' },
      legend: {
        orient: 'vertical',
        right: '0%',
        top: 'center',
        textStyle: { color: '#666', fontSize: 11 }
      },
      series: [{
        name: '学院分布',
        type: 'pie',
        radius: ['30%', '70%'],
        center: ['26%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 16, fontWeight: 600, color: '#1d2129' }
        },
        labelLine: { show: false },
        data: collegeData,
        color: [
          '#409eff', '#67c23a', '#f59a23', '#f56c6c', '#909399', '#e6a23c', '#722ed1',
          '#f7ba1e', '#ff8042', '#00c48c', '#36a2eb', '#ff9f7f', '#00d8a6', '#ff6b6b',
          '#feca57', '#ff9ff3', '#54a0ff', '#5f27cd'
        ]
      }]
    }
    
    myChart.setOption(option)
  } catch (error) {
    console.error('获取用户学院分布失败:', error)
    // 使用默认数据
    const collegeData = [
      { value: 52, name: '数理学院' },
      { value: 45, name: '外国语学院' },
      { value: 40, name: '环境与资源学院' },
      { value: 36, name: '信息与控制工程学院' },
      { value: 31, name: '航空航天学院' },
      { value: 27, name: '法学院' },
      { value: 25, name: '计算机科学与技术学院' },
      { value: 24, name: '文学与艺术学院' },
      { value: 23, name: '生命科学与农林学院' },
      { value: 22, name: '经济管理学院' },
      { value: 19, name: '土木工程与建筑学院' },
      { value: 16, name: '医学院' },
      { value: 15, name: '制造科学与工程学院' },
      { value: 14, name: '马克思主义学院' },
      { value: 13, name: '国防科学学院' },
      { value: 11, name: '应用技术学院' },
      { value: 10, name: '体育与健康学院' },
      { value: 9, name: '继续教育学院' }
    ]
    
    const option = {
      tooltip: { trigger: 'item', formatter: '{b}: {c} 人 ({d}%)' },
      legend: {
        orient: 'vertical',
        right: '0%',
        top: 'center',
        textStyle: { color: '#666', fontSize: 11 }
      },
      series: [{
        name: '学院分布',
        type: 'pie',
        radius: ['30%', '70%'],
        center: ['26%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 16, fontWeight: 600, color: '#1d2129' }
        },
        labelLine: { show: false },
        data: collegeData,
        color: [
          '#409eff', '#67c23a', '#f59a23', '#f56c6c', '#909399', '#e6a23c', '#722ed1',
          '#f7ba1e', '#ff8042', '#00c48c', '#36a2eb', '#ff9f7f', '#00d8a6', '#ff6b6b',
          '#feca57', '#ff9ff3', '#54a0ff', '#5f27cd'
        ]
      }]
    }
    myChart.setOption(option)
  }
  
  window.addEventListener('resize', () => myChart.resize())
}

// 24小时热力图
const initHeatmapChart = async () => {
  const chartDom = document.getElementById('heatmap-chart')
  if (!chartDom) return
  
  const myChart = echarts.init(chartDom)
  heatmapChart.value = myChart
  
  try {
    // 从后端 API 获取热力图数据
    const response = await fetch('/api/dashboard/heatmap')
    const data = await response.json()
    
    const hours = Array.from({ length: 24 }, (_, i) => i)
    let rooms = ['图书馆A区', '教学楼B301', '图书馆C区', '实验楼D202', '教学楼A105']
    let heatmapData = []
    
    if (data.success && data.data) {
      const heatmapResponse = data.data
      rooms = heatmapResponse.rooms || rooms
      heatmapData = heatmapResponse.data || []
    } else {
      // 生成默认热力图数据
      rooms.forEach((_, roomIndex) => {
        hours.forEach((hour, hourIndex) => {
          let occupancyRate
          if (hour >= 7 && hour <= 23) {
            const baseRate = [80, 75, 70, 65, 60][roomIndex] // 不同自习室的基础热度
            const peakFactor = (hour >= 18 && hour <= 22) ? 1.2 : 1
            occupancyRate = Math.min(100, Math.round(baseRate * peakFactor * (0.9 + Math.random() * 0.2)))
          } else {
            occupancyRate = Math.round(Math.random() * 10)
          }
          heatmapData.push([hourIndex, roomIndex, occupancyRate])
        })
      })
    }
    
    const option = {
      tooltip: {
        position: 'top',
        formatter: function(params) {
          const hour = params.data[0]
          const roomName = rooms[params.data[1]]
          const rate = params.data[2]
          return `${roomName}<br/>${hour}:00 - ${hour+1}:00<br/>占用率: ${rate}%`
        }
      },
      grid: {
        height: '50%',
        top: '10%'
      },
      xAxis: {
        type: 'category',
        data: hours.map(h => `${h}:00`),
        splitArea: { show: true },
        axisLabel: { interval: 1, rotate: 45 }
      },
      yAxis: {
        type: 'category',
        data: rooms,
        splitArea: { show: true }
      },
      visualMap: {
        min: 0,
        max: 100,
        calculable: true,
        orient: 'horizontal',
        left: 'center',
        bottom: '15%',
        textStyle: { color: '#666' }
      },
      series: [{
        name: '占用率',
        type: 'heatmap',
        data: heatmapData,
        label: { show: false },
        emphasis: {
          itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0, 0, 0, 0.5)' }
        }
      }]
    }
    
    myChart.setOption(option)
  } catch (error) {
    console.error('获取热力图数据失败:', error)
    // 使用默认数据
    const hours = Array.from({ length: 24 }, (_, i) => i)
    const rooms = ['图书馆A区', '教学楼B301', '图书馆C区', '实验楼D202', '教学楼A105']
    const data = []
    
    rooms.forEach((_, roomIndex) => {
      hours.forEach((hour, hourIndex) => {
        let occupancyRate
        if (hour >= 7 && hour <= 23) {
          const baseRate = [80, 75, 70, 65, 60][roomIndex] // 不同自习室的基础热度
          const peakFactor = (hour >= 18 && hour <= 22) ? 1.2 : 1
          occupancyRate = Math.min(100, Math.round(baseRate * peakFactor * (0.9 + Math.random() * 0.2)))
        } else {
          occupancyRate = Math.round(Math.random() * 10)
        }
        data.push([hourIndex, roomIndex, occupancyRate])
      })
    })
    
    const option = {
      tooltip: {
        position: 'top',
        formatter: function(params) {
          const hour = params.data[0]
          const roomName = rooms[params.data[1]]
          const rate = params.data[2]
          return `${roomName}<br/>${hour}:00 - ${hour+1}:00<br/>占用率: ${rate}%`
        }
      },
      grid: {
        height: '50%',
        top: '10%'
      },
      xAxis: {
        type: 'category',
        data: hours.map(h => `${h}:00`),
        splitArea: { show: true },
        axisLabel: { interval: 1, rotate: 45 }
      },
      yAxis: {
        type: 'category',
        data: rooms,
        splitArea: { show: true }
      },
      visualMap: {
        min: 0,
        max: 100,
        calculable: true,
        orient: 'horizontal',
        left: 'center',
        bottom: '15%',
        textStyle: { color: '#666' }
      },
      series: [{
        name: '占用率',
        type: 'heatmap',
        data: data,
        label: { show: false },
        emphasis: {
          itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0, 0, 0, 0.5)' }
        }
      }]
    }
    myChart.setOption(option)
  }
  
  window.addEventListener('resize', () => myChart.resize())
}

// 初始化
onMounted(async () => {
  await getStats()
})

// 清理图表实例
const cleanupCharts = () => {
  if (trendChart.value) {
    trendChart.value.dispose()
    trendChart.value = null
  }
  if (heatRankChart.value) {
    heatRankChart.value.dispose()
    heatRankChart.value = null
  }
  if (collegeChart.value) {
    collegeChart.value.dispose()
    collegeChart.value = null
  }
  if (heatmapChart.value) {
    heatmapChart.value.dispose()
    heatmapChart.value = null
  }
}
</script>

<template>
  <div class="admin-dashboard">
    <!-- 头部标题与操作区 -->
    <div class="header">
      <h1 class="dashboard-title">
        <DataLine class="icon-medium" />
        高校自习室管理后台 · 数据看板
      </h1>
      <div class="header-actions">
        <el-button-group>
          <el-button type="primary" size="medium" plain>
            <Calendar class="icon-small" />
            今日
          </el-button>
          <el-button type="primary" size="medium" plain>
            <Calendar class="icon-small" />
            本周
          </el-button>
          <el-button type="primary" size="medium" @click="exportData">
            <Download class="icon-small" />
            导出
          </el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 核心数据统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <div class="card stat-card">
          <span class="stat-label">总座位数</span>
          <span class="stat-value highlight-green">{{ stats.totalSeats }}</span>
          <span class="stat-rate">(使用率 {{ stats.usageRate }}%)</span>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="card stat-card">
          <span class="stat-label">当前占用</span>
          <span class="stat-value highlight-green">{{ stats.currentOccupancy }}</span>
          <span class="stat-rate">实时更新</span>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="card stat-card">
          <span class="stat-label">今日预约</span>
          <span class="stat-value highlight-green">{{ stats.todayReservations }}</span>
          <span class="stat-rate">
            <span v-if="stats.yesterdayReservationChange > 0" class="highlight-green">
              <ArrowUp class="change-icon" />
              {{ stats.yesterdayReservationChange }}%
            </span>
            <span v-else-if="stats.yesterdayReservationChange < 0" class="highlight-red">
              <ArrowDown class="change-icon" />
              {{ Math.abs(stats.yesterdayReservationChange) }}%
            </span>
            <span v-else>
              无变化
            </span>
          </span>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="card stat-card">
          <span class="stat-label">爽约率</span>
          <span class="stat-value highlight-red">{{ stats.noShowRate }}%</span>
          <span class="stat-rate">较昨日 -0.5%</span>
        </div>
      </el-col>
    </el-row>

    <!-- 高峰时段提示 -->
    <div class="card peak-time-card">
      <div class="peak-time">
        <HotWater class="peak-icon" />
        <span>高峰时段：{{ peakTime }}</span>
      </div>
    </div>

    <!-- 使用率趋势图 -->
    <div class="card">
      <h3 class="card-title">
        <DataLine class="icon-small" />
        使用率趋势图（近7天）
      </h3>
      <div id="trend-chart" class="chart-container"></div>
    </div>

    <!-- 自习室热度排行 + 用户学院分布 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <div class="card">
          <h3 class="card-title">
            <OfficeBuilding class="icon-small" />
            自习室热度排行 (当前在室人数)
          </h3>
          <div id="heat-rank-chart" class="small-chart-container"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card">
          <h3 class="card-title">
            <User class="icon-small" />
            用户学院分布 (当前在室人数)
          </h3>
          <div id="college-chart" class="small-chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 24小时热力图 -->
    <div class="card">
      <h3 class="card-title">
        <HotWater class="icon-small" />
        24小时使用热力图
      </h3>
      <div id="heatmap-chart" class="heatmap-container"></div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.admin-dashboard {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  background-color: #f5f7fa;
  min-height: 100vh;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .dashboard-title {
      font-size: 24px;
      color: #1d2129;
      font-weight: 600;
      display: flex;
      align-items: center;
    }

    .header-actions {
      .el-button {
        display: flex;
        align-items: center;
      }
    }
  }

  .card {
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    padding: 20px;
    margin-bottom: 20px;

    .card-title {
      font-size: 16px;
      color: #1d2129;
      margin-bottom: 16px;
      font-weight: 500;
      display: flex;
      align-items: center;
    }
  }

  .stat-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    text-align: center;

    .stat-label {
      font-size: 14px;
      color: #666;
      margin-bottom: 8px;
    }

    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: #1d2129;
      margin: 8px 0;
    }

    .stat-rate {
      font-size: 12px;
      color: #888;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .highlight-red {
    color: #f56c6c;
  }

  .highlight-green {
    color: #409eff;
  }

  .peak-time-card {
    .peak-time {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      color: #1d2129;
      font-weight: 500;
    }
  }

  .chart-container {
    width: 100%;
    height: 300px;
    margin-top: 16px;
  }

  .small-chart-container {
    width: 100%;
    height: 250px;
    margin-top: 16px;
  }

  .heatmap-container {
    width: 100%;
    height: 200px;
    margin-top: 16px;
  }

  /* 图标样式 - 统一管理 */
  .icon-small {
    font-size: 16px;
    margin-right: 8px;
    width: 1em;
    height: 1em;
  }
  
  .icon-medium {
    font-size: 20px;
    margin-right: 8px;
    width: 1em;
    height: 1em;
  }
  
  .change-icon {
    font-size: 12px;
    margin-right: 2px;
    width: 1em;
    height: 1em;
  }
  
  .peak-icon {
    font-size: 16px;
    color: #f56c6c;
    width: 1em;
    height: 1em;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-dashboard {
    padding: 10px;

    .header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;

      .dashboard-title {
        font-size: 20px;
      }
    }

    .el-row {
      .el-col {
        width: 100%;
        max-width: 100%;
        
        &:not(:last-child) {
          margin-bottom: 16px;
        }
      }
    }

    .chart-container {
      height: 250px;
    }

    .small-chart-container {
      height: 200px;
    }

    .heatmap-container {
      height: 180px;
    }
  }
}
</style>