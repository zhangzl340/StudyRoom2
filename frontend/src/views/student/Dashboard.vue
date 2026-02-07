<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowRight, Clock, Check, Close } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'
import { useRouter } from 'vue-router'
import { getActiveAnnouncements } from '../../services/announcement'
import { useReservationStore } from '../../stores/reservation'
import { useRoomStore } from '../../stores/room'

const authStore = useAuthStore()
const router = useRouter()
const reservationStore = useReservationStore()

const roomStore = useRoomStore()

// 待签到预约
const pendingCheckinReservations = ref([])
const loadingReservations = ref(false)

// 违约情况
const defaultStatus = ref('正常') // 违约状态
const defaultNumber = ref(0) // 违约次数
const startTime = ref('')    // 禁约开始时间
const endTime = ref('')      // 禁约结束时间
const remainingTime = ref('')// 禁约剩余时间
const banTime = ref(7)       // 禁约时间/天 
const remark = ref('')       // 备注
const cancelBtn = ref(false) // 禁约按钮

// 通知公告列表
const announcements = ref([])
const loading = ref(false)
const expandedAnnouncementId = ref(null) // 当前展开的公告ID

// 获取活跃的公告
const fetchActiveAnnouncements = async () => {
  loading.value = true
  try {
    const response = await getActiveAnnouncements()
    if (response.code === 200) {
      announcements.value = response.data
    }
  } catch (error) {
    ElMessage.error('获取公告失败')
  } finally {
    loading.value = false
  }
}

// 获取待签到的预约
const fetchPendingCheckinReservations = async () => {
  loadingReservations.value = true
  try {
    await reservationStore.fetchReservations()
    await roomStore.fetchRooms()
    
    // 获取所有座位信息
    const allSeats = []
    for (const room of roomStore.rooms) {
      await roomStore.fetchSeats(room.id)
      allSeats.push(...roomStore.seats)
    }
    
    pendingCheckinReservations.value = reservationStore.reservations.filter(res => 
      res.userId === authStore.userInfo?.id && 
      res.reservationStatus === '已预约'
    ).map(res => {
      // 查找对应的座位信息
      const seat = allSeats.find(s => s.id === parseInt(res.seatId))
      if (seat) {
        res.roomId = seat.roomId
      }
      return res
    })
  } catch (error) {
    ElMessage.error('获取待签到预约失败')
  } finally {
    loadingReservations.value = false
  }
}

// 跳转到签到页面
const goToCheckin = (reservationId) => {
  router.push(`/student/checkin?reservationId=${reservationId}`)
}

// 取消预约
const cancelReservation = async (reservationId) => {
  try {
    await reservationStore.cancelReservationRequest(reservationId)
    ElMessage.success('预约已取消')
    await fetchPendingCheckinReservations()
  } catch (error) {
    ElMessage.error('取消预约失败')
  }
}

// 根据公告类型获取颜色
const getAnnouncementTypeColor = (type) => {
  switch (type) {
    case 'system':
      return '#409EFF' // 系统通知 - 蓝色
    case 'notice':
      return '#67C23A' // 一般通知 - 绿色
    case 'warning':
      return '#E6A23C' // 警告通知 - 橙色
    case 'urgent':
      return '#F56C6C' // 紧急通知 - 红色
    default:
      return '#909399' // 默认 - 灰色
  }
}

// 切换公告展开/收起状态
const toggleAnnouncement = (announcementId) => {
  if (expandedAnnouncementId.value === announcementId) {
    expandedAnnouncementId.value = null
  } else {
    expandedAnnouncementId.value = announcementId
  }
}

// 根据roomId获取自习室名称
const getRoomName = (roomId) => {
  const room = roomStore.rooms.find(r => r.id === parseInt(roomId))
  console.log("room = " + roomId)
  return room?.name || `自习室 ${roomId}`
}

// 模拟获取用户信息
const getUserInfo = () => {
  // 这里可以调用后端API获取用户信息
  if (!authStore.userInfo && authStore.token) {
    authStore.fetchUserInfo()
  }
}

// 模拟获取预约状态
const getReservationStatus = () => {
  // 这里可以调用后端API获取预约状态
  // 暂时使用模拟数据
  defaultNumber.value = 0
  defaultStatus.value = '正常'
}

// 计算并更新剩余时间
const updateRemainingTime = () => {
  if (endTime.value) {
    const now = new Date()
    const end = new Date(endTime.value)
    const diff = end.getTime() - now.getTime()
    if (diff > 0) {
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
      const seconds = Math.floor((diff % (1000 * 60)) / 1000)
      remainingTime.value = `${days}天${hours}小时${minutes}分${seconds}秒`
    } else {
      remainingTime.value = '0天0小时0分0秒'
    }
    // 是否可以解除禁约
    if (defaultStatus.value == '禁约') {
      cancelBtn.value = new Date() > new Date(endTime.value)
    }
  }
}

// 解除禁约
const cancelBan = () => {
  if (new Date() > new Date(endTime.value)) {
    // 这里可以调用后端API解除禁约
    defaultStatus.value = '正常'
    cancelBtn.value = false
    ElMessage.success('预约状态已恢复正常')
  }
}

// 启动定时器更新剩余时间
let timer = null
onMounted(() => {
  getUserInfo()
  getReservationStatus()
  fetchActiveAnnouncements()
  fetchPendingCheckinReservations()
  timer = setInterval(() => { updateRemainingTime() }, 1000)
})

// 清理定时器
onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<template>
  <div class="dashboard">
    <!-- 我的信息 -->
    <div class="my-info">
      <div class="info">
        <el-avatar :size="60" :src="authStore.userInfo?.avatar || ''" class="user-avatar">
          {{ authStore.userInfo?.username?.charAt(0) || '用' }}
        </el-avatar>
        <div class="name">
          <p class="greeting">你好，{{ authStore.userInfo?.username || '用户' }}</p>
          <p class="student-id">学号：{{ authStore.userInfo?.username || '' }}</p>
        </div>
      </div>
      <div class="status">
        <div class="status-item">
          <div class="status-value">{{ defaultNumber }}</div>
          <div class="status-label">违约次数</div>
        </div>
        <div class="status-item">
          <div class="status-value" :class="{ 'status-normal': defaultStatus === '正常', 'status-banned': defaultStatus === '禁约' }">
            {{ defaultStatus }}
          </div>
          <div class="status-label">预约状态</div>
        </div>
      </div>
    </div>
    
    <!-- 轮播图 -->
    <div class="carousel">
      <el-carousel :interval="3000" type="card" height="180px">
        <el-carousel-item>
          <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20study%20room%20with%20comfortable%20seats%20and%20good%20lighting&image_size=landscape_16_9" alt="自习室环境" class="carousel-img">
        </el-carousel-item>
        <el-carousel-item>
          <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=students%20studying%20in%20a%20modern%20library&image_size=landscape_16_9" alt="学习氛围" class="carousel-img">
        </el-carousel-item>
        <el-carousel-item>
          <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=smart%20campus%20study%20room%20reservation%20system&image_size=landscape_16_9" alt="智能预约系统" class="carousel-img">
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 待签到预约 -->
    <div class="pending-checkin" v-if="pendingCheckinReservations.length > 0">
      <h3 class="section-title">
        <el-icon><Clock /></el-icon>
        待签到预约
      </h3>
      <div class="checkin-list" v-loading="loadingReservations">
        <div v-for="reservation in pendingCheckinReservations" :key="reservation.id" class="checkin-item">
          <div class="checkin-info">
            <div class="checkin-room">{{ getRoomName(reservation.roomId) }}</div>
            <div class="checkin-time">预约时间：{{ reservation.reservationInTime }} - {{ reservation.reservationOutTime }}</div>
          </div>
          <div class="checkin-actions">
            <el-button type="primary" size="small" @click="goToCheckin(reservation.id)">
              <el-icon><Check /></el-icon>
              签到
            </el-button>
            <el-button type="danger" size="small" @click="cancelReservation(reservation.id)">
              <el-icon><Close /></el-icon>
              取消
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 通知公告 -->
    <div class="notice">
      <h3 class="notice-title">通知公告</h3>
      <div class="notice-list" v-loading="loading">
        <div v-for="announcement in announcements" :key="announcement.id" class="notice-item" @click="toggleAnnouncement(announcement.id)">
          <div class="notice-content">
            <div class="notice-header">
              <div class="notice-title" :style="{ color: getAnnouncementTypeColor(announcement.type) }">{{ announcement.title }}</div>
              <div class="notice-time">{{ announcement.publishTime || announcement.createdAt }}</div>
            </div>
            <div class="notice-body" v-if="expandedAnnouncementId === announcement.id">
              <div class="notice-content-text">{{ announcement.content }}</div>
            </div>
          </div>
          <el-icon class="notice-arrow" :class="{ 'rotated': expandedAnnouncementId === announcement.id }"><ArrowRight /></el-icon>
        </div>
        <div v-if="!loading && announcements.length === 0" class="empty-notice">
          <el-empty description="暂无通知公告" />
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.dashboard {
  padding: 10px;
  background-color: #f5f7fa;
  min-height: 100%;

  // 我的信息卡片
  .my-info {
    padding: 20px;
    color: #ffffff;
    background: linear-gradient(135deg, #409EFF, #36cfc9);
    border-radius: 12px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    margin-bottom: 15px;

    .info {
      display: flex;
      align-items: center;
      gap: 15px;
      margin-bottom: 20px;

      .user-avatar {
        border: 2px solid rgba(255, 255, 255, 0.5);
      }

      .name {
        .greeting {
          font-size: 18px;
          font-weight: bold;
          margin: 0 0 5px 0;
        }

        .student-id {
          font-size: 14px;
          opacity: 0.9;
          margin: 0;
        }
      }
    }

    .status {
      display: flex;
      justify-content: space-around;
      padding: 0 10px;

      .status-item {
        display: flex;
        flex-direction: column;
        align-items: center;

        .status-value {
          font-size: 20px;
          font-weight: bold;
          margin-bottom: 5px;
        }

        .status-normal {
          color: #67C23A;
        }

        .status-banned {
          color: #F56C6C;
        }

        .status-label {
          font-size: 12px;
          opacity: 0.9;
        }
      }

      .empty-notice {
        padding: 40px 0;
        text-align: center;
      }
    }
  }

  // 轮播图
  .carousel {
    margin-bottom: 15px;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);

    .carousel-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 12px;
    }
  }

  // 待签到预约
  .pending-checkin {
    margin-bottom: 15px;
    background-color: #ffffff;
    border-radius: 12px;
    padding: 15px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .section-title {
      font-size: 16px;
      font-weight: bold;
      color: #333333;
      margin: 0 0 15px 0;
      display: flex;
      align-items: center;
      gap: 8px;
      position: relative;
      padding-left: 10px;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 4px;
        height: 16px;
        background-color: #409EFF;
        border-radius: 2px;
      }
    }

    .checkin-list {
      .checkin-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px;
        border-bottom: 1px solid #f0f0f0;
        background-color: #f9f9f9;
        border-radius: 8px;
        margin-bottom: 10px;

        &:last-child {
          margin-bottom: 0;
        }

        .checkin-info {
          flex: 1;

          .checkin-room {
            font-size: 14px;
            font-weight: 500;
            color: #333333;
            margin-bottom: 5px;
          }

          .checkin-time {
            font-size: 12px;
            color: #606266;
          }
        }

        .checkin-actions {
          display: flex;
          gap: 8px;

          .el-button {
            padding: 6px 12px;
            font-size: 12px;
          }
        }
      }
    }
  }

  // 通知公告
  .notice {
    background-color: #ffffff;
    border-radius: 12px;
    padding: 15px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .notice-title {
      font-size: 16px;
      font-weight: bold;
      color: #333333;
      margin: 0 0 15px 0;
      position: relative;
      padding-left: 10px;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 4px;
        height: 16px;
        background-color: #409EFF;
        border-radius: 2px;
      }
    }

    .notice-list {
      .notice-item {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          padding: 12px 0;
          border-bottom: 1px solid #f0f0f0;
          cursor: pointer;
          transition: all 0.3s ease;

          &:last-child {
            border-bottom: none;
          }

          &:hover {
            background-color: #f5f7fa;
            padding-left: 8px;
          }

          .notice-content {
            flex: 1;

            .notice-header {
              display: flex;
              justify-content: space-between;
              align-items: flex-start;
              margin-bottom: 5px;
            }

            .notice-title {
              font-size: 14px;
              font-weight: 500;
              margin: 0;
              padding: 0;
              flex: 1;
              margin-right: 10px;

              &::before {
                display: none;
              }
            }

            .notice-time {
              font-size: 12px;
              color: #909399;
              white-space: nowrap;
            }

            .notice-body {
              margin-top: 8px;
              padding: 10px;
              background-color: #f9f9f9;
              border-radius: 6px;
              border-left: 3px solid #e0e0e0;

              .notice-content-text {
                font-size: 13px;
                line-height: 1.5;
                color: #606266;
                word-break: break-word;
              }
            }
          }

          .notice-arrow {
            font-size: 16px;
            color: #909399;
            transition: transform 0.3s ease;
            margin-top: 2px;

            &.rotated {
              transform: rotate(90deg);
            }
          }
        }
    }
  }

  // 响应式调整
  @media (max-width: 768px) {
    padding: 10px;

    .my-info {
      padding: 15px;

      .info {
        gap: 10px;

        .user-avatar {
          :deep(.el-avatar) {
            width: 50px !important;
            height: 50px !important;
            font-size: 20px !important;
          }
        }

        .name {
          .greeting {
            font-size: 16px;
          }

          .student-id {
            font-size: 12px;
          }
        }
      }

      .status {
        .status-item {
          .status-value {
            font-size: 18px;
          }

          .status-label {
            font-size: 11px;
          }
        }
      }
    }

    .carousel {
      height: 150px;

      :deep(.el-carousel) {
        height: 150px !important;
      }
    }

    .notice {
      padding: 12px;

      .notice-title {
        font-size: 14px;
      }

      .notice-list {
        .notice-item {
          padding: 10px 0;

          .notice-content {
            .notice-title {
              font-size: 13px;
            }

            .notice-time {
              font-size: 11px;
            }
          }
        }
      }
    }
  }
}
</style>