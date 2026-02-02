<template>
  <div class="room-detail-container">
    <el-card class="room-detail-card">
      <template #header>
        <div class="card-header">
          <h2>{{ roomInfo.name }} - 详情</h2>
          <el-button 
            type="primary" 
            @click="showReservationDialog = true"
            :disabled="roomInfo.status !== 'available'"
          >
            预约座位
          </el-button>
        </div>
      </template>
      
      <!-- 自习室基本信息 -->
      <div class="room-info-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="info-item">
              <label>楼栋：</label>
              <span>{{ roomInfo.building }}</span>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <label>楼层：</label>
              <span>{{ roomInfo.floor }}楼</span>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <label>状态：</label>
              <el-tag :type="roomInfo.status === 'available' ? 'success' : 'danger'">
                {{ roomInfo.status === 'available' ? '可用' : '不可用' }}
              </el-tag>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <label>总座位数：</label>
              <span>{{ roomInfo.totalSeats }}</span>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <label>可用座位数：</label>
              <span class="available-seats">{{ roomInfo.availableSeats }}</span>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="info-item">
              <label>使用率：</label>
              <span>{{ usageRate }}%</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <label>描述：</label>
              <span>{{ roomInfo.description || '暂无描述' }}</span>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <!-- 座位布局 -->
      <div class="seat-layout-section">
        <h3>座位布局</h3>
        <div class="layout-container">
          <div class="seat-grid">
            <div 
              v-for="seat in seats" 
              :key="seat.id"
              class="seat-item"
              :class="{
                'seat-available': seat.status === 'available',
                'seat-reserved': seat.status === 'reserved',
                'seat-occupied': seat.status === 'occupied'
              }"
              @click="selectSeat(seat)"
            >
              <div class="seat-number">{{ seat.seatNumber }}</div>
              <div class="seat-status">{{ seatStatusText(seat.status) }}</div>
            </div>
          </div>
        </div>
        
        <!-- 座位图例 -->
        <div class="seat-legend">
          <div class="legend-item">
            <div class="legend-color available"></div>
            <span>可用</span>
          </div>
          <div class="legend-item">
            <div class="legend-color reserved"></div>
            <span>已预约</span>
          </div>
          <div class="legend-item">
            <div class="legend-color occupied"></div>
            <span>已使用</span>
          </div>
        </div>
      </div>
      
      <!-- 座位详情 -->
      <div v-if="selectedSeat" class="selected-seat-section">
        <h3>座位详情</h3>
        <el-form :model="selectedSeat" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="座位号">
                <span>{{ selectedSeat.seatNumber }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="状态">
                <el-tag :type="selectedSeat.status === 'available' ? 'success' : selectedSeat.status === 'reserved' ? 'warning' : 'danger'">
                  {{ seatStatusText(selectedSeat.status) }}
                </el-tag>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="位置">
                <span>{{ selectedSeat.position || '暂无信息' }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="类型">
                <span>{{ selectedSeat.type || '普通座位' }}</span>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="描述">
                <span>{{ selectedSeat.description || '暂无描述' }}</span>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item>
            <el-button 
              type="primary" 
              @click="reserveSelectedSeat"
              :disabled="selectedSeat.status !== 'available'"
            >
              预约此座位
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 统计信息 -->
      <div class="stats-section">
        <h3>使用统计</h3>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ availableSeatsCount }}</div>
                <div class="stat-label">可用座位</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ reservedSeatsCount }}</div>
                <div class="stat-label">已预约座位</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ occupiedSeatsCount }}</div>
                <div class="stat-label">已使用座位</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ usageRate }}%</div>
                <div class="stat-label">使用率</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 预约对话框 -->
      <el-dialog
        v-model="showReservationDialog"
        title="预约座位"
        width="500px"
      >
        <el-form
          ref="reservationFormRef"
          :model="reservationForm"
          :rules="reservationRules"
          label-width="100px"
        >
          <el-form-item label="自习室">
            <span>{{ roomInfo.name }}</span>
          </el-form-item>
          <el-form-item label="座位号" prop="seatId">
            <el-select v-model="reservationForm.seatId" placeholder="请选择座位">
              <el-option 
                v-for="seat in availableSeats" 
                :key="seat.id" 
                :label="seat.seatNumber" 
                :value="seat.id" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
              v-model="reservationForm.startTime"
              type="datetime"
              placeholder="请选择开始时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              :min-date="new Date()"
            />
          </el-form-item>
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
              v-model="reservationForm.endTime"
              type="datetime"
              placeholder="请选择结束时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              :min-date="reservationForm.startTime || new Date()"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="showReservationDialog = false">取消</el-button>
            <el-button type="primary" @click="submitReservation">确认预约</el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
    
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

// 路由
const route = useRoute()
const router = useRouter()

// Store
const roomStore = useRoomStore()

// 状态
const loading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')
const showReservationDialog = ref(false)
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
  { id: 1, seatNumber: 'A1', status: 'available', position: 'A区1号', type: '普通座位' },
  { id: 2, seatNumber: 'A2', status: 'available', position: 'A区2号', type: '普通座位' },
  { id: 3, seatNumber: 'A3', status: 'reserved', position: 'A区3号', type: '普通座位' },
  { id: 4, seatNumber: 'A4', status: 'occupied', position: 'A区4号', type: '普通座位' },
  { id: 5, seatNumber: 'A5', status: 'available', position: 'A区5号', type: '普通座位' },
  { id: 6, seatNumber: 'B1', status: 'available', position: 'B区1号', type: '普通座位' },
  { id: 7, seatNumber: 'B2', status: 'available', position: 'B区2号', type: '普通座位' },
  { id: 8, seatNumber: 'B3', status: 'available', position: 'B区3号', type: '普通座位' },
  { id: 9, seatNumber: 'B4', status: 'reserved', position: 'B区4号', type: '普通座位' },
  { id: 10, seatNumber: 'B5', status: 'available', position: 'B区5号', type: '普通座位' },
  { id: 11, seatNumber: 'C1', status: 'available', position: 'C区1号', type: '普通座位' },
  { id: 12, seatNumber: 'C2', status: 'available', position: 'C区2号', type: '普通座位' },
  { id: 13, seatNumber: 'C3', status: 'available', position: 'C区3号', type: '普通座位' },
  { id: 14, seatNumber: 'C4', status: 'available', position: 'C区4号', type: '普通座位' },
  { id: 15, seatNumber: 'C5', status: 'occupied', position: 'C区5号', type: '普通座位' }
])

// 预约表单
const reservationForm = reactive({
  seatId: '',
  startTime: '',
  endTime: ''
})

// 表单验证规则
const reservationRules = {
  seatId: [
    { required: true, message: '请选择座位', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ]
}

// 计算属性
const availableSeatsCount = computed(() => {
  return seats.value.filter(seat => seat.status === 'available').length
})

const reservedSeatsCount = computed(() => {
  return seats.value.filter(seat => seat.status === 'reserved').length
})

const occupiedSeatsCount = computed(() => {
  return seats.value.filter(seat => seat.status === 'occupied').length
})

const usageRate = computed(() => {
  if (roomInfo.totalSeats === 0) return 0
  const used = reservedSeatsCount.value + occupiedSeatsCount.value
  return Math.round((used / roomInfo.totalSeats) * 100)
})

const availableSeats = computed(() => {
  return seats.value.filter(seat => seat.status === 'available')
})

// 方法
const seatStatusText = (status: string): string => {
  switch (status) {
    case 'available': return '可用'
    case 'reserved': return '已预约'
    case 'occupied': return '已使用'
    default: return '未知'
  }
}

const selectSeat = (seat: any) => {
  selectedSeat.value = seat
}

const reserveSelectedSeat = () => {
  if (!selectedSeat.value) return
  if (selectedSeat.value.status !== 'available') {
    errorMessage.value = '该座位不可预约'
    return
  }
  reservationForm.seatId = selectedSeat.value.id
  showReservationDialog.value = true
}

const submitReservation = async () => {
  // 这里应该调用预约API
  // 暂时模拟成功
  showReservationDialog.value = false
  successMessage.value = '预约成功'
  
  // 更新座位状态
  const seatIndex = seats.value.findIndex(s => s.id === parseInt(reservationForm.seatId))
  if (seatIndex !== -1 && seats.value[seatIndex]) {
    seats.value[seatIndex].status = 'reserved'
  }
  
  // 清空表单
  reservationForm.seatId = ''
  reservationForm.startTime = ''
  reservationForm.endTime = ''
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
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.room-detail-card {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  h2 {
    margin: 0;
    font-size: 20px;
    color: #303133;
  }
}

.room-info-section {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.info-item {
  margin-bottom: 10px;
  
  label {
    font-weight: bold;
    color: #606266;
    margin-right: 10px;
  }
  
  span {
    color: #303133;
  }
  
  .available-seats {
    color: #52c41a;
    font-weight: bold;
  }
}

.seat-layout-section {
  margin-bottom: 30px;
}

.layout-container {
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 20px;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 15px;
}

.seat-item {
  padding: 15px;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
  
  &.seat-available {
    background-color: #f6ffed;
    border-color: #b7eb8f;
    
    &:hover {
      background-color: #d9f7be;
    }
  }
  
  &.seat-reserved {
    background-color: #fff7e6;
    border-color: #ffd591;
    cursor: not-allowed;
  }
  
  &.seat-occupied {
    background-color: #fff2f0;
    border-color: #ffccc7;
    cursor: not-allowed;
  }
}

.seat-number {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
}

.seat-status {
  font-size: 12px;
  color: #606266;
}

.seat-legend {
  display: flex;
  gap: 30px;
  margin-top: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  
  &.available {
    background-color: #f6ffed;
    border: 1px solid #b7eb8f;
  }
  
  &.reserved {
    background-color: #fff7e6;
    border: 1px solid #ffd591;
  }
  
  &.occupied {
    background-color: #fff2f0;
    border: 1px solid #ffccc7;
  }
}

.selected-seat-section {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.stats-section {
  margin-top: 30px;
}

.stat-card {
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-item {
  text-align: center;
  
  .stat-value {
    font-size: 24px;
    font-weight: bold;
    color: #1890ff;
    margin-bottom: 5px;
  }
  
  .stat-label {
    font-size: 14px;
    color: #606266;
  }
}

@media (max-width: 768px) {
  .room-detail-container {
    padding: 10px;
  }
  
  .room-detail-card {
    padding: 10px;
  }
  
  .room-info-section {
    padding: 15px;
  }
  
  .layout-container {
    padding: 15px;
  }
  
  .seat-grid {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
    gap: 10px;
  }
  
  .seat-item {
    padding: 10px;
  }
  
  .seat-legend {
    flex-wrap: wrap;
    gap: 15px;
  }
  
  .selected-seat-section {
    padding: 15px;
  }
  
  .stats-section {
    .el-col {
      margin-bottom: 15px;
    }
  }
}
</style>
