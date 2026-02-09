<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, Check, Close, Refresh } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useReservationStore } from '../../stores/reservation'
import { useRoomStore } from '../../stores/room'
import { signInReservation, signOutReservation, leaveReservation, returnFromLeaveReservation } from '../../services/reservation'
import service from '../../services/axios'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const reservationStore = useReservationStore()
const roomStore = useRoomStore()

// 加载状态
const loading = ref(false)
const loading2 = ref(false)

// 预约信息
const reservationInfo = ref(null)
const reservationId = ref(route.query.reservationId || null)

// 座位信息
const seatInfo = ref(null)
const roomInfo = ref(null)
const checkInInfo = ref(null)

// 获取预约详情
const fetchReservationDetail = async () => {
  if (!reservationId.value) {
    ElMessage.error('预约ID不能为空')
    router.push('/student')
    return
  }
  
  loading.value = true
  try {
    // 先获取所有预约，然后筛选出当前预约
    await reservationStore.fetchReservations()
    const reservation = reservationStore.reservations.find(res => res.id === parseInt(reservationId.value))
    
    if (reservation) {
      reservationInfo.value = reservation
      
      // 获取所有自习室和座位信息
      await roomStore.getRooms()
      
      // 存储所有座位信息的临时数组
      const allSeats = []
      
      // 遍历所有自习室，获取座位信息
      for (const room of roomStore.rooms) {
        try {
          await roomStore.fetchSeats(room.id)
          // 将当前房间的座位信息添加到临时数组
          allSeats.push(...roomStore.seats.map(seat => ({
            ...seat,
            roomId: room.id
          })))
        } catch (error) {
          console.error(`获取自习室 ${room.id} 的座位信息失败:`, error)
        }
      }
      
      // 查找对应的座位信息
      const seat = allSeats.find(s => s.id === parseInt(reservation.seatId))
      if (seat) {
        // 查找对应的自习室信息
        const foundRoom = roomStore.rooms.find(r => r.id === parseInt(seat.roomId))
        if (foundRoom) {
          roomInfo.value = foundRoom
          reservation.roomId = foundRoom.id
        }
      }
      
      // 获取签到记录信息
      await fetchCheckInInfo(reservation.id)
      
      console.log('预约信息:', reservation)
      console.log('自习室信息:', roomInfo.value)
      console.log('签到记录信息:', checkInInfo.value)
    } else {
      ElMessage.error('预约信息不存在')
      router.push('/student')
    }
  } catch (error) {
    ElMessage.error('获取预约信息失败')
  } finally {
    loading.value = false
  }
}

// 签到
const handleSignIn = async () => {
  if (!reservationInfo.value) return
  
  loading2.value = true
  try {
    const response = await signInReservation(reservationInfo.value.id)
    if (response.code === 200) {
      ElMessage.success('签到成功')
      // 更新预约状态
      reservationInfo.value.reservationStatus = '使用中'
      reservationInfo.value.signInTime = new Date().toISOString().replace('T', ' ').split('.')[0]
      // 刷新预约列表
      await reservationStore.fetchReservations()
      await fetchCheckInInfo(reservationInfo.value.id)
    } else {
      ElMessage.error('签到失败：' + response.message)
    }
  } catch (error) {
    ElMessage.error('签到失败：' + error.message)
  } finally {
    loading2.value = false
  }
}

// 签退
const handleSignOut = async () => {
  if (!reservationInfo.value) return
  
  loading2.value = true
  try {
    // 获取签到id
    if (!checkInInfo.value) {
      ElMessage.error('未找到签到记录')
      return
    }
    
    const response = await signOutReservation(checkInInfo.value.id)
    if (response.code === 200) {
      ElMessage.success('签退成功')
      // 更新预约状态
      reservationInfo.value.reservationStatus = '完成预约'
      reservationInfo.value.signOutTime = new Date().toISOString().replace('T', ' ').split('.')[0]
      // 刷新预约列表
      await reservationStore.fetchReservations()
      // 重新获取签到信息
      await fetchCheckInInfo(reservationInfo.value.id)
    } else {
      ElMessage.error('签退失败：' + response.message)
    }
  } catch (error) {
    ElMessage.error('签退失败：' + error.message)
  } finally {
    loading2.value = false
  }
}

// 暂离
const handleLeave = async () => {
  if (!reservationInfo.value) return
  
  loading2.value = true
  try {
    // 获取签到id
    if (!checkInInfo.value) {
      ElMessage.error('未找到签到记录')
      return
    }
    
    const response = await leaveReservation(checkInInfo.value.id)
    
    if (response.code === 200) {
      ElMessage.success('暂离成功')
      // 更新签到状态
      checkInInfo.value.status = 'left'
      // 重新获取签到信息，确保状态更新
      await fetchCheckInInfo(reservationInfo.value.id)
    } else {
      ElMessage.error('暂离失败：' + response.message)
    }
  } catch (error) {
    ElMessage.error('暂离失败：' + error.message)
  } finally {
    loading2.value = false
  }
}

// 暂离返回
const handleReturn = async () => {
  if (!reservationInfo.value) return
  
  loading2.value = true
  try {
    // 获取签到id
    if (!checkInInfo.value) {
      ElMessage.error('未找到签到记录')
      return
    }
    
    const response = await returnFromLeaveReservation(checkInInfo.value.id)
    
    if (response.code === 200) {
      ElMessage.success('返回成功')
      // 更新签到状态
      checkInInfo.value.status = 'returned'
      // 重新获取签到信息，确保状态更新
      await fetchCheckInInfo(reservationInfo.value.id)
    } else {
      ElMessage.error('返回失败：' + response.message)
    }
  } catch (error) {
    ElMessage.error('返回失败：' + error.message)
  } finally {
    loading2.value = false
  }
}

// 取消预约
const handleCancelReservation = async () => {
  if (!reservationInfo.value) return
  
  try {
    await reservationStore.cancelReservationRequest(reservationInfo.value.id)
    ElMessage.success('取消预约成功')
    router.push('/student')
  } catch (error) {
    ElMessage.error('取消预约失败')
  }
}

// 获取签到记录
const fetchCheckInInfo = async (reservationId) => {
  try {
    const response = await service({
      url: '/checkin/list',
      method: 'get',
      params: {
        userId: authStore.userInfo?.id
      }
    })
    
    if (response.code === 200) {
      const checkIns = response.data
      const checkIn = checkIns.find(ci => ci.reservationId === parseInt(reservationId))
      if (checkIn) {
        checkInInfo.value = checkIn
        console.log('签到记录信息:', checkInInfo.value)
      }
    }
  } catch (error) {
    console.error('获取签到记录失败:', error)
  }
}

// 刷新预约信息
const refreshReservationInfo = async () => {
  await fetchReservationDetail()
}

// 初始化
onMounted(async () => {
  // 先获取用户信息
  if (!authStore.userInfo && authStore.token) {
    await authStore.fetchUserInfo()
  }
  await fetchReservationDetail()
})
</script>

<template>
  <div class="checkin">
    <el-container>
      <el-header>签到</el-header>
      <el-main>
        <div class="checkin-card" v-loading="loading">
          <div v-if="reservationInfo" class="reservation-info">
            <div class="info-header">
              <h3>预约信息</h3>
              <el-button type="primary" size="small" icon="Refresh" @click="refreshReservationInfo">
                刷新
              </el-button>
            </div>
            
            <div class="info-content">
              <div class="info-item">
                <span class="label">自习室：</span>
                <span class="value">{{ roomInfo?.name || reservationInfo.roomId || '未知' }}</span>
              </div>
              <div class="info-item">
                <span class="label">座位号：</span>
                <span class="value">{{ reservationInfo.seatId || '未知' }}</span>
              </div>
              <div class="info-item">
                <span class="label">预约状态：</span>
                <span class="value" :class="{
                  'status-reserved': reservationInfo.reservationStatus === '已预约',
                  'status-using': reservationInfo.reservationStatus === '使用中',
                  'status-completed': reservationInfo.reservationStatus === '完成预约',
                  'status-cancelled': reservationInfo.reservationStatus === '取消预约',
                  'status-default': reservationInfo.reservationStatus === '违约中'
                }">{{ reservationInfo.reservationStatus }}</span>
              </div>
              <div class="info-item">
                <span class="label">预约时间：</span>
                <span class="value">{{ reservationInfo.reservationInTime }} - {{ reservationInfo.reservationOutTime }}</span>
              </div>
              <div class="info-item" v-if="reservationInfo.signInTime">
                <span class="label">签到时间：</span>
                <span class="value">{{ reservationInfo.signInTime }}</span>
              </div>
              <div class="info-item" v-if="reservationInfo.signOutTime">
                <span class="label">签退时间：</span>
                <span class="value">{{ reservationInfo.signOutTime }}</span>
              </div>
            </div>
            
            <div class="action-buttons" v-loading="loading2">
              <!-- 已预约状态 -->
              <div v-if="reservationInfo.reservationStatus === '已预约'" class="buttons-group">
                <el-button type="primary" size="large" @click="handleSignIn">
                  <el-icon><Check /></el-icon>
                  立即签到
                </el-button>
                <el-button type="danger" size="large" @click="handleCancelReservation">
                  <el-icon><Close /></el-icon>
                  取消预约
                </el-button>
              </div>
              
                <!-- 使用中状态和暂离状态 -->
              <div v-else-if="reservationInfo.reservationStatus === '使用中'" class="buttons-group">
                <el-button v-if="!checkInInfo || checkInInfo.status !== 'left'" type="info" size="large" @click="handleLeave">
                  暂离
                </el-button>
                <el-button v-else type="primary" size="large" @click="handleReturn">
                  返回
                </el-button>
                <el-button type="warning" size="large" @click="handleSignOut">
                  <el-icon><Close /></el-icon>
                  立即签退
                </el-button>
              </div>
              <!-- 其他状态 -->
              <div v-else class="status-message">
                <p class="message">{{ 
                  reservationInfo.reservationStatus === '完成预约' ? '预约已完成' :
                  reservationInfo.reservationStatus === '取消预约' ? '预约已取消' :
                  reservationInfo.reservationStatus === '违约中' ? '预约已违约' :
                  '预约状态异常'
                }}</p>
                <el-button type="primary" size="large" @click="router.push('/student/dashboard')">
                  返回首页
                </el-button>
              </div>
            </div>
          </div>
          
          <div v-else class="empty-info">
            <el-empty description="未找到预约信息" />
            <el-button type="primary" @click="router.push('/student')">
              返回首页
            </el-button>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<style lang="scss" scoped>
.checkin {
  height: 100%;
  background-color: #f5f7fa;
  padding: 10px;

  .el-container {
    height: 100%;

    .el-header {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 40px;
      color: #555555;
      font-size: 16px;
      background-color: #ffffff;
    }

    .el-main {
      padding: 10px;

      .checkin-card {
        background-color: #ffffff;
        border-radius: 12px;
        padding: 20px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        .info-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 20px;

          h3 {
            font-size: 18px;
            color: #333333;
            margin: 0;
          }
        }

        .info-content {
          background-color: #f9f9f9;
          border-radius: 8px;
          padding: 15px;
          margin-bottom: 20px;

          .info-item {
            display: flex;
            margin-bottom: 12px;
            align-items: center;

            &:last-child {
              margin-bottom: 0;
            }

            .label {
              width: 80px;
              font-size: 14px;
              color: #606266;
            }

            .value {
              flex: 1;
              font-size: 14px;
              color: #333333;
            }

            .status-reserved {
              color: #409EFF;
              font-weight: 500;
            }

            .status-using {
              color: #67C23A;
              font-weight: 500;
            }

            .status-completed {
              color: #909399;
              font-weight: 500;
            }

            .status-cancelled {
              color: #F56C6C;
              font-weight: 500;
            }

            .status-default {
              color: #E6A23C;
              font-weight: 500;
            }
          }
        }

        .action-buttons {
          .buttons-group {
            display: flex;
            gap: 10px;
            align-items: center;
            justify-content: center;

            .el-button {
              flex: 1;
              min-width: 120px;
            }
          }

          .status-message {
            text-align: center;
            padding: 30px 0;

            .message {
              font-size: 16px;
              color: #606266;
              margin-bottom: 20px;
            }

            .el-button {
              width: 200px;
            }
          }
        }

        .empty-info {
          text-align: center;
          padding: 50px 0;

          .el-button {
            margin-top: 20px;
          }
        }
      }
    }
  }

  // 响应式调整
  @media (max-width: 768px) {
    padding: 5px;

    .el-main {
      padding: 5px;

      .checkin-card {
        padding: 15px;

        .info-header {
          h3 {
            font-size: 16px;
          }
        }

        .info-content {
          padding: 12px;

          .info-item {
            margin-bottom: 10px;

            .label {
              width: 70px;
              font-size: 13px;
            }

            .value {
              font-size: 13px;
            }
          }
        }

        .action-buttons {
          .buttons-group {
            flex-direction: column;

            .el-button {
              width: 100%;
            }
          }
        }
      }
    }
  }
}
</style>