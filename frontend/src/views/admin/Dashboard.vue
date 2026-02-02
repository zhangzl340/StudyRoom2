<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoomStore } from '../../stores/room'
import { useReservationStore } from '../../stores/reservation'
import { useAuthStore } from '../../stores/auth'

const roomStore = useRoomStore()
const reservationStore = useReservationStore()
const authStore = useAuthStore()

// 统计数据
const stats = ref({
  roomCount: 0,
  seatCount: 0,
  reservationCount: 0,
  userCount: 0,
  violationCount: 0
})

// 加载状态
const loading = ref(true)

// 获取统计数据
const getStats = async () => {
  try {
    // 获取自习室数量
    await roomStore.getRooms()
    stats.value.roomCount = roomStore.rooms.length
    
    // 计算座位数量
    let totalSeats = 0
    for (const room of roomStore.rooms) {
      await roomStore.getSeatsByRoomId(room.id)
      totalSeats += roomStore.seats.length
    }
    stats.value.seatCount = totalSeats
    
    // 获取预约数量
    await reservationStore.getReservations()
    stats.value.reservationCount = reservationStore.reservations.length
    
    // 计算违约数量
    const violationCount = reservationStore.reservations.filter(item => item.status === '违约').length
    stats.value.violationCount = violationCount
    
    // 暂时设置用户数量为100（实际应该从用户服务获取）
    stats.value.userCount = 100
  } catch (error) {
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  getStats()
})
</script>

<template>
  <div class="admin-dashboard">
    <el-card v-loading="loading" shadow="hover" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span>系统概览</span>
          <el-button type="primary" size="small" @click="getStats">刷新数据</el-button>
        </div>
      </template>
      
      <!-- 统计卡片 -->
      <div class="stats-grid">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <h3 class="stat-value">{{ stats.roomCount }}</h3>
              <p class="stat-label">自习室数量</p>
            </div>
            <div class="stat-icon room-icon">
              <el-icon size="32">
                <Building />
              </el-icon>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <h3 class="stat-value">{{ stats.seatCount }}</h3>
              <p class="stat-label">座位总数</p>
            </div>
            <div class="stat-icon seat-icon">
              <el-icon size="32">
                <Suitcase />
              </el-icon>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <h3 class="stat-value">{{ stats.reservationCount }}</h3>
              <p class="stat-label">预约总数</p>
            </div>
            <div class="stat-icon reservation-icon">
              <el-icon size="32">
                <Calendar />
              </el-icon>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <h3 class="stat-value">{{ stats.userCount }}</h3>
              <p class="stat-label">用户总数</p>
            </div>
            <div class="stat-icon user-icon">
              <el-icon size="32">
                <User />
              </el-icon>
            </div>
          </div>
        </el-card>
        
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-info">
              <h3 class="stat-value">{{ stats.violationCount }}</h3>
              <p class="stat-label">违约总数</p>
            </div>
            <div class="stat-icon violation-icon">
              <el-icon size="32">
                <Warning />
              </el-icon>
            </div>
          </div>
        </el-card>
      </div>
      
      <!-- 系统信息 -->
      <el-card class="system-info-card" shadow="hover" style="margin-top: 20px;">
        <template #header>
          <span>系统信息</span>
        </template>
        <div class="system-info">
          <p><span class="info-label">系统版本：</span> v1.0.0</p>
          <p><span class="info-label">运行状态：</span> <el-tag type="success">正常运行</el-tag></p>
          <p><span class="info-label">当前管理员：</span> {{ authStore.user?.username || '管理员' }}</p>
          <p><span class="info-label">最后登录时间：</span> {{ new Date().toLocaleString() }}</p>
        </div>
      </el-card>
      
      <!-- 快捷操作 -->
      <el-card class="quick-actions-card" shadow="hover" style="margin-top: 20px;">
        <template #header>
          <span>快捷操作</span>
        </template>
        <div class="quick-actions">
          <el-button type="primary" @click="window.location.href = '/admin/rooms'">
            <el-icon>
              <Building />
            </el-icon>
            <span>自习室管理</span>
          </el-button>
          <el-button type="success" @click="window.location.href = '/admin/seats'">
            <el-icon>
              <Suitcase />
            </el-icon>
            <span>座位管理</span>
          </el-button>
          <el-button type="warning" @click="window.location.href = '/admin/reservations'">
            <el-icon>
              <Calendar />
            </el-icon>
            <span>预约管理</span>
          </el-button>
          <el-button type="danger" @click="window.location.href = '/admin/users'">
            <el-icon>
              <User />
            </el-icon>
            <span>用户管理</span>
          </el-button>
        </div>
      </el-card>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.admin-dashboard {
  .dashboard-card {
    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
    margin-bottom: 20px;
  }

  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: #303133;
          margin: 0 0 8px 0;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
          margin: 0;
        }
      }

      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;

        &.room-icon {
          background-color: rgba(64, 158, 255, 0.1);
          color: #409EFF;
        }

        &.seat-icon {
          background-color: rgba(103, 194, 58, 0.1);
          color: #67C23A;
        }

        &.reservation-icon {
          background-color: rgba(230, 162, 60, 0.1);
          color: #E6A23C;
        }

        &.user-icon {
          background-color: rgba(144, 147, 153, 0.1);
          color: #909399;
        }

        &.violation-icon {
          background-color: rgba(245, 108, 108, 0.1);
          color: #F56C6C;
        }
      }
    }
  }

  .system-info-card {
    .system-info {
      p {
        margin: 8px 0;
        font-size: 14px;

        .info-label {
          color: #909399;
          margin-right: 8px;
        }
      }
    }
  }

  .quick-actions-card {
    .quick-actions {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 12px;

      .el-button {
        justify-content: center;
        gap: 8px;
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .quick-actions {
    grid-template-columns: repeat(2, 1fr) !important;
  }
}
</style>