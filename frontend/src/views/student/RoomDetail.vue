<template>
  <div class="room-detail-container">
    <!-- 自习室基本信息 -->
    <div class="room-info-card">
      <div class="room-info-header">
        <h2 class="room-name">{{ roomInfo.name }}</h2>
        <el-tag :type="roomInfo.status === 'available' ? 'success' : 'danger'">
          {{ roomInfo.status === 'available' ? '可预约' : '已满' }}
        </el-tag>
      </div>
      
      <div class="room-info-details">
        <div class="room-detail-item">
          <el-icon class="detail-icon"><OfficeBuilding /></el-icon>
          <span>{{ roomInfo.building }} {{ roomInfo.floor }}楼</span>
        </div>
        <div class="room-detail-item">
          <el-icon class="detail-icon"><Timer /></el-icon>
          <span>08:00-22:00</span>
        </div>
        <div class="room-detail-item">
          <el-icon class="detail-icon"><Suitcase /></el-icon>
          <span>余位：{{ roomInfo.availableSeats }}/{{ roomInfo.totalSeats }}</span>
        </div>
      </div>
    </div>
    
    <!-- 座位布局 -->
    <div class="seat-layout-section">
      <div class="section-header">
        <h3>选择座位</h3>
        <div class="seat-legend">
          <div class="legend-item">
            <div class="legend-color available"></div>
            <span>空闲</span>
          </div>
          <div class="legend-item">
            <div class="legend-color reserved"></div>
            <span>已预约</span>
          </div>
          <div class="legend-item">
            <div class="legend-color occupied"></div>
            <span>使用中</span>
          </div>
        </div>
      </div>
      
      <div class="seat-grid">
        <div 
          v-for="seat in seats" 
          :key="seat.id"
          class="seat-item"
          :class="{
            'seat-available': seat.status === 'available',
            'seat-reserved': seat.status === 'reserved',
            'seat-occupied': seat.status === 'occupied',
            'seat-selected': selectedSeat?.id === seat.id
          }"
          @click="selectSeat(seat)"
        >
          <div class="seat-number">{{ seat.seatNumber }}</div>
        </div>
      </div>
    </div>
    
    <!-- 预约信息 -->
    <div v-if="selectedSeat" class="reservation-section">
      <div class="section-header">
        <h3>预约信息</h3>
      </div>
      
      <div class="reservation-form">
        <div class="form-item">
          <label>座位号：</label>
          <span class="form-value">{{ selectedSeat.seatNumber }}</span>
        </div>
        
        <div class="form-item">
          <label>预约时间：</label>
          <div class="time-selector">
            <el-date-picker
              v-model="reservationForm.startTime"
              type="datetime"
              placeholder="开始时间"
              format="MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              :min-date="new Date()"
              class="time-picker"
            />
            <span class="time-separator">至</span>
            <el-date-picker
              v-model="reservationForm.endTime"
              type="datetime"
              placeholder="结束时间"
              format="MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              :min-date="reservationForm.startTime || new Date()"
              class="time-picker"
            />
          </div>
        </div>
        
        <div class="form-item">
          <label>预计费用：</label>
          <span class="price-value">¥{{ estimatedPrice }}/小时</span>
        </div>
        
        <el-button 
          type="primary" 
          class="reserve-button"
          @click="submitReservation"
          :disabled="!selectedSeat || selectedSeat.status !== 'available' || !reservationForm.startTime || !reservationForm.endTime"
        >
          确认预约
        </el-button>
      </div>
    </div>
    
    <!-- 成功提示 -->
    <el-message
      v-if="successMessage"
      :message="successMessage"
      type="success"
      :duration="3000"
      @close="successMessage = ''"
    />
    
    <!-- 错误提示 -->
    <el-message
      v-if="errorMessage"
      :message="errorMessage"
      type="error"
      :duration="3000"
      @close="errorMessage = ''"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useRoomStore } from '../../stores/room'
import { OfficeBuilding, Timer, Suitcase } from '@element-plus/icons-vue'

// 路由
const route = useRoute()
const router = useRouter()

// Store
const roomStore = useRoomStore()

// 状态
const loading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')
const selectedSeat = ref<any>(null)

// 自习室信息
const roomInfo = reactive({
  id: 0,
  name: '',
  building: '',
  floor: 0,
  totalSeats: 0,
  availableSeats: 0,
  status: 'available',
  description: ''
})

// 座位数据
const seats = ref([
  { id: 1, seatNumber: 'A1', status: 'available' },
  { id: 2, seatNumber: 'A2', status: 'available' },
  { id: 3, seatNumber: 'A3', status: 'available' },
  { id: 4, seatNumber: 'A4', status: 'available' },
  { id: 5, seatNumber: 'A5', status: 'available' },
  { id: 6, seatNumber: 'B1', status: 'reserved' },
  { id: 7, seatNumber: 'B2', status: 'available' },
  { id: 8, seatNumber: 'B3', status: 'available' },
  { id: 9, seatNumber: 'B4', status: 'reserved' },
  { id: 10, seatNumber: 'B5', status: 'available' },
  { id: 11, seatNumber: 'C1', status: 'available' },
  { id: 12, seatNumber: 'C2', status: 'occupied' },
  { id: 13, seatNumber: 'C3', status: 'available' },
  { id: 14, seatNumber: 'C4', status: 'available' },
  { id: 15, seatNumber: 'C5', status: 'available' }
])

// 预约表单
const reservationForm = reactive({
  startTime: '',
  endTime: ''
})

// 计算属性
const estimatedPrice = computed(() => {
  // 固定价格4元/小时
  return 4
})

// 方法
const selectSeat = (seat: any) => {
  if (seat.status !== 'available') {
    errorMessage.value = '该座位不可预约'
    return
  }
  selectedSeat.value = seat
}

const submitReservation = async () => {
  if (!selectedSeat.value) {
    errorMessage.value = '请选择座位'
    return
  }
  
  if (!reservationForm.startTime || !reservationForm.endTime) {
    errorMessage.value = '请选择预约时间'
    return
  }
  
  // 这里应该调用预约API
  // 暂时模拟成功
  successMessage.value = '预约成功'
  
  // 更新座位状态
  const seatIndex = seats.value.findIndex(s => s.id === selectedSeat.value.id)
  if (seatIndex !== -1) {
    seats.value[seatIndex].status = 'reserved'
  }
  
  // 清空表单
  selectedSeat.value = null
  reservationForm.startTime = ''
  reservationForm.endTime = ''
  
  // 3秒后跳转到预约列表
  setTimeout(() => {
    router.push('/student/reservations')
  }, 3000)
}

const loadRoomDetail = async () => {
  const roomId = parseInt(route.params.id as string)
  if (isNaN(roomId)) {
    errorMessage.value = '无效的自习室ID'
    router.push('/student/rooms')
    return
  }
  
  loading.value = true
  try {
    // 调用API获取自习室详情
    const response = await roomStore.fetchRoomDetail(roomId) as any
    if (response.code === 200) {
      Object.assign(roomInfo, response.data)
    }
    
    // 调用API获取座位列表
    const seatsResponse = await roomStore.fetchSeats(roomId) as any
    if (seatsResponse.code === 200) {
      seats.value = seatsResponse.data
    }
  } catch (error) {
    console.error('加载自习室详情失败:', error)
    errorMessage.value = '加载自习室详情失败'
  } finally {
    loading.value = false
  }
}

// 页面加载时加载数据
onMounted(() => {
  loadRoomDetail()
})
</script>

<style scoped lang="scss">
.room-detail-container {
  padding: 15px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

// 自习室信息卡片
.room-info-card {
  background-color: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .room-info-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;

    .room-name {
      font-size: 18px;
      font-weight: bold;
      color: #303133;
      margin: 0;
    }
  }

  .room-info-details {
    .room-detail-item {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 10px;
      font-size: 14px;
      color: #606266;

      .detail-icon {
        font-size: 16px;
        color: #909399;
      }
    }
  }
}

// 座位布局部分
.seat-layout-section {
  background-color: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .section-header {
    margin-bottom: 20px;

    h3 {
      font-size: 16px;
      font-weight: bold;
      color: #303133;
      margin: 0 0 15px 0;
    }

    .seat-legend {
      display: flex;
      gap: 20px;
      flex-wrap: wrap;

      .legend-item {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 12px;
        color: #606266;

        .legend-color {
          width: 16px;
          height: 16px;
          border-radius: 4px;

          &.available {
            background-color: #67C23A;
          }

          &.reserved {
            background-color: #E6A23C;
          }

          &.occupied {
            background-color: #F56C6C;
          }
        }
      }
    }
  }

  .seat-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 10px;
    margin-bottom: 15px;

    .seat-item {
      aspect-ratio: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s ease;
      font-size: 14px;
      font-weight: bold;

      &.seat-available {
        background-color: #F0F9EB;
        border: 2px solid #67C23A;
        color: #67C23A;

        &:hover {
          background-color: #E1F5E8;
          transform: scale(1.05);
        }
      }

      &.seat-reserved {
        background-color: #FEF3CD;
        border: 2px solid #E6A23C;
        color: #E6A23C;
        cursor: not-allowed;
      }

      &.seat-occupied {
        background-color: #FEE2E2;
        border: 2px solid #F56C6C;
        color: #F56C6C;
        cursor: not-allowed;
      }

      &.seat-selected {
        background-color: #ECF5FF;
        border: 2px solid #409EFF;
        color: #409EFF;
      }

      .seat-number {
        font-size: 14px;
        font-weight: bold;
      }
    }
  }
}

// 预约信息部分
.reservation-section {
  background-color: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .section-header {
    margin-bottom: 20px;

    h3 {
      font-size: 16px;
      font-weight: bold;
      color: #303133;
      margin: 0;
    }
  }

  .reservation-form {
    .form-item {
      margin-bottom: 20px;

      label {
        display: block;
        font-size: 14px;
        font-weight: 500;
        color: #606266;
        margin-bottom: 8px;
      }

      .form-value {
        font-size: 14px;
        color: #303133;
      }

      .time-selector {
        display: flex;
        align-items: center;
        gap: 10px;
        flex-wrap: wrap;

        .time-picker {
          flex: 1;
          min-width: 120px;
        }

        .time-separator {
          font-size: 14px;
          color: #909399;
        }
      }

      .price-value {
        font-size: 16px;
        font-weight: bold;
        color: #F56C6C;
      }
    }

    .reserve-button {
      width: 100%;
      height: 45px;
      font-size: 16px;
      border-radius: 25px;
      margin-top: 10px;
    }
  }
}

// 响应式调整
@media (max-width: 768px) {
  .room-detail-container {
    padding: 10px;
  }

  .room-info-card {
    padding: 15px;

    .room-info-header {
      .room-name {
        font-size: 16px;
      }
    }

    .room-info-details {
      .room-detail-item {
        font-size: 13px;
      }
    }
  }

  .seat-layout-section {
    padding: 15px;

    .seat-grid {
      grid-template-columns: repeat(5, 1fr);
      gap: 8px;

      .seat-item {
        font-size: 12px;
      }
    }

    .seat-legend {
      gap: 15px;

      .legend-item {
        font-size: 11px;
      }
    }
  }

  .reservation-section {
    padding: 15px;

    .reservation-form {
      .form-item {
        margin-bottom: 15px;

        .time-selector {
          flex-direction: column;
          align-items: stretch;

          .time-picker {
            width: 100%;
          }

          .time-separator {
            text-align: center;
            margin: 5px 0;
          }
        }
      }

      .reserve-button {
        height: 40px;
        font-size: 14px;
      }
    }
  }
}

// 平板适配
@media (min-width: 769px) and (max-width: 1024px) {
  .seat-layout-section {
    .seat-grid {
      grid-template-columns: repeat(5, 1fr);
    }
  }
}
</style>
