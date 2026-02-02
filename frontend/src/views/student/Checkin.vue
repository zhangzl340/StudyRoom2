<template>
  <div class="checkin-container">
    <h2>签到签退</h2>
    
    <!-- 待签到预约 -->
    <div class="pending-reservations">
      <h3>待签到预约</h3>
      <el-card v-if="pendingReservations.length > 0" class="reservation-card">
        <div v-for="reservation in pendingReservations" :key="reservation.id" class="reservation-item">
          <div class="reservation-info">
            <div class="info-row">
              <span class="label">自习室：</span>
              <span class="value">{{ reservation.room.name }}</span>
            </div>
            <div class="info-row">
              <span class="label">座位：</span>
              <span class="value">{{ reservation.seat.seatNumber }}</span>
            </div>
            <div class="info-row">
              <span class="label">开始时间：</span>
              <span class="value">{{ formatDateTime(reservation.startTime) }}</span>
            </div>
            <div class="info-row">
              <span class="label">结束时间：</span>
              <span class="value">{{ formatDateTime(reservation.endTime) }}</span>
            </div>
          </div>
          
          <div class="checkin-actions">
            <el-button type="primary" @click="startCheckin(reservation.id)">
              开始签到
            </el-button>
          </div>
        </div>
      </el-card>
      <el-empty v-else description="暂无待签到预约" />
    </div>
    
    <!-- 签到区域 -->
    <div v-if="checkinMode" class="checkin-area">
      <h3>{{ isSigningIn ? '签到' : '签退' }}</h3>
      
      <el-card class="checkin-card">
        <!-- 二维码扫描 -->
        <div class="scan-section">
          <h4>扫描二维码{{ isSigningIn ? '签到' : '签退' }}</h4>
          <div class="qrcode-scanner" ref="scannerRef">
            <qrcode-stream
              @decode="onDecode"
              @init="onInit"
              style="width: 100%"
            >
              <div class="scanner-overlay">
                <div class="scanner-frame"></div>
              </div>
            </qrcode-stream>
          </div>
          <p class="scan-tip">请将摄像头对准自习室的{{ isSigningIn ? '签到' : '签退' }}二维码</p>
        </div>
        
        <!-- 手动签到 -->
        <div class="manual-section">
          <h4>手动{{ isSigningIn ? '签到' : '签退' }}</h4>
          <el-form :model="manualCheckinForm" label-width="80px">
            <el-form-item label="预约ID">
              <el-input v-model="manualCheckinForm.reservationId" placeholder="请输入预约ID" />
            </el-form-item>
            <el-form-item label="验证码">
              <el-input v-model="manualCheckinForm.verificationCode" placeholder="请输入自习室验证码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="manualCheckin">
                {{ isSigningIn ? '手动签到' : '手动签退' }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
      
      <el-button @click="cancelCheckin">取消</el-button>
    </div>
    
    <!-- 签到历史 -->
    <div class="checkin-history">
      <h3>签到历史</h3>
      <el-table
        v-loading="loading"
        :data="checkinHistory"
        style="width: 100%"
        stripe
      >
        <el-table-column prop="id" label="记录ID" width="100" />
        <el-table-column label="预约信息" width="300">
          <template #default="scope">
            <div>
              <div>{{ scope.row.reservation.room.name }} - 座位 {{ scope.row.reservation.seat.seatNumber }}</div>
              <div class="text-sm text-gray-500">{{ formatDateTime(scope.row.reservation.startTime) }} - {{ formatDateTime(scope.row.reservation.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="签到时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.checkinTime) }}
          </template>
        </el-table-column>
        <el-table-column label="签退时间" width="180">
          <template #default="scope">
            {{ scope.row.checkoutTime ? formatDateTime(scope.row.checkoutTime) : '未签退' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag
              :type="scope.row.checkoutTime ? 'success' : 'warning'"
              size="small"
            >
              {{ scope.row.checkoutTime ? '已完成' : '进行中' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 签到成功提示 -->
    <el-dialog
      v-model="successDialogVisible"
      title="操作成功"
      width="400px"
    >
      <div class="success-content">
        <el-icon class="success-icon"><CircleCheck /></el-icon>
        <p>{{ successMessage }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheck } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { QrcodeStream } from 'vue-qrcode-reader'
import { useReservationStore } from '@/stores/reservation'

const route = useRoute()
const reservationStore = useReservationStore()

// 状态
const checkinMode = ref(false)
const isSigningIn = ref(true)
const currentReservationId = ref<number | null>(null)
const scannerRef = ref<HTMLElement | null>(null)
const loading = ref(false)
const successDialogVisible = ref(false)
const successMessage = ref('')

// 表单
const manualCheckinForm = ref({
  reservationId: '',
  verificationCode: ''
})

// 数据
const pendingReservations = ref<any[]>([])
const checkinHistory = ref<any[]>([])

// 计算属性
// const canCheckin = computed(() => {
//   return pendingReservations.value.length > 0
// })

// 方法
function formatDateTime(dateTime: string) {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm')
}

async function fetchPendingReservations() {
  // 这里应该调用API获取待签到的预约
  // 暂时使用模拟数据
  pendingReservations.value = reservationStore.upcomingReservations.filter(
    (res: any) => res.status === 'active' && dayjs(res.startTime).isAfter(dayjs().subtract(1, 'hour'))
  )
}

async function fetchCheckinHistory() {
  loading.value = true
  try {
    // 这里应该调用API获取签到历史
    // 暂时使用模拟数据
    checkinHistory.value = [
      {
        id: 1,
        reservation: {
          id: 1,
          room: { name: '自习室A' },
          seat: { seatNumber: 'A1' },
          startTime: dayjs().subtract(2, 'hour').format(),
          endTime: dayjs().add(1, 'hour').format()
        },
        checkinTime: dayjs().subtract(2, 'hour').format(),
        checkoutTime: null
      },
      {
        id: 2,
        reservation: {
          id: 2,
          room: { name: '自习室B' },
          seat: { seatNumber: 'B2' },
          startTime: dayjs().subtract(1, 'day').format(),
          endTime: dayjs().subtract(1, 'day').add(3, 'hour').format()
        },
        checkinTime: dayjs().subtract(1, 'day').format(),
        checkoutTime: dayjs().subtract(1, 'day').add(3, 'hour').format()
      }
    ]
  } catch (error) {
    ElMessage.error('获取签到历史失败')
  } finally {
    loading.value = false
  }
}

function startCheckin(reservationId: number) {
  currentReservationId.value = reservationId
  isSigningIn.value = true
  checkinMode.value = true
}

function cancelCheckin() {
  checkinMode.value = false
  currentReservationId.value = null
  manualCheckinForm.value = {
    reservationId: '',
    verificationCode: ''
  }
}

function onDecode(result: string) {
  // 解析二维码结果
  console.log('二维码内容:', result)
  
  // 这里应该验证二维码内容是否有效
  // 然后调用签到/签退API
  if (currentReservationId.value) {
    performCheckin()
  }
}

function onInit(promise: Promise<any>) {
  promise
    .then((result) => {
      console.log('摄像头初始化成功:', result)
    })
    .catch((error) => {
      console.error('摄像头初始化失败:', error)
      ElMessage.warning('摄像头初始化失败，请检查权限设置')
    })
}

async function manualCheckin() {
  if (!manualCheckinForm.value.reservationId || !manualCheckinForm.value.verificationCode) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  await performCheckin()
}

async function performCheckin() {
  loading.value = true
  try {
    // 这里应该调用实际的签到/签退API
    // 暂时模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    successMessage.value = isSigningIn.value ? '签到成功！' : '签退成功！'
    successDialogVisible.value = true
    
    // 重置状态
    checkinMode.value = false
    currentReservationId.value = null
    manualCheckinForm.value = {
      reservationId: '',
      verificationCode: ''
    }
    
    // 刷新数据
    await fetchPendingReservations()
    await fetchCheckinHistory()
  } catch (error) {
    ElMessage.error(isSigningIn.value ? '签到失败' : '签退失败')
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(async () => {
  // 检查URL参数是否有预约ID
  const reservationId = route.query.reservationId
  if (reservationId) {
    startCheckin(parseInt(reservationId as string))
  }
  
  await fetchPendingReservations()
  await fetchCheckinHistory()
})

onUnmounted(() => {
  // 清理摄像头资源
  if (scannerRef.value) {
    // 这里可以添加清理逻辑
  }
})
</script>

<style scoped>
.checkin-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

h2 {
  margin-bottom: 30px;
  color: #303133;
}

h3 {
  margin-bottom: 20px;
  color: #409eff;
}

.pending-reservations,
.checkin-area,
.checkin-history {
  margin-bottom: 40px;
}

.reservation-card {
  margin-bottom: 20px;
}

.reservation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
}

.reservation-item:last-child {
  border-bottom: none;
}

.reservation-info {
  flex: 1;
}

.info-row {
  margin-bottom: 8px;
}

.label {
  font-weight: 500;
  color: #606266;
  margin-right: 10px;
}

.value {
  color: #303133;
}

.checkin-actions {
  margin-left: 20px;
}

.checkin-card {
  padding: 20px;
}

.scan-section {
  margin-bottom: 30px;
}

.scan-section h4,
.manual-section h4 {
  margin-bottom: 15px;
  color: #606266;
}

.qrcode-scanner {
  width: 300px;
  height: 300px;
  margin: 0 auto 20px;
  position: relative;
}

.scanner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.scanner-frame {
  width: 200px;
  height: 200px;
  border: 2px solid #409eff;
  position: relative;
}

.scanner-frame::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: #409eff;
  animation: scan 2s linear infinite;
}

@keyframes scan {
  0% {
    top: 0;
  }
  100% {
    top: 100%;
  }
}

.scan-tip {
  text-align: center;
  color: #909399;
  margin-top: 10px;
}

.manual-section {
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.success-content {
  text-align: center;
  padding: 20px;
}

.success-icon {
  font-size: 48px;
  color: #67c23a;
  margin-bottom: 20px;
}

.success-content p {
  font-size: 16px;
  color: #303133;
}
</style>