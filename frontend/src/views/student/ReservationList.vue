<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Location, Clock, Search } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useRoomStore } from '../../stores/room'
import { useReservationStore } from '../../stores/reservation'

const router = useRouter()
const authStore = useAuthStore()
const roomStore = useRoomStore()
const reservationStore = useReservationStore()

// 加载状态
const loading = ref(false)      // 自习室列表加载
const loading2 = ref(false)     // 座位列表加载

// 自习室相关
const roomList = ref([])        // 自习室列表
const searchKeyword = ref('')   // 自习室搜索
const resetBtnShow = ref(false) // 重置按钮显示

// 座位选择相关
const roomShow = ref(false)     // 自习室详情显示
const roomData = ref({})        // 自习室数据
const seatList = ref([])        // 自习室座位
const selectedSeat = ref(null)  // 选中的座位

// 预约数据
const reservationData = ref({
  userId: null,
  seatId: null,
  status: null,
  reservationStatus: null,
  reservationInTime: null,
  reservationOutTime: null,
})

// 获取自习室列表
const getRoomList = async () => {
  loading.value = true
  try {
    await roomStore.getRooms(searchKeyword.value)
    roomList.value = roomStore.rooms
  } catch (error) {
    ElMessage.error('获取自习室列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索自习室
const handleSearch = () => {
  if (!searchKeyword.value) {
    ElMessage.error('请输入自习室名称')
    return
  }
  resetBtnShow.value = true
  getRoomList()
}

// 重置搜索
const handleReset = () => {
  searchKeyword.value = ''
  resetBtnShow.value = false
  getRoomList()
}

// 选择自习室
const selectRoom = async (room) => {
  // 自习室关闭
  if (room.status === '关闭') {
    ElMessage.error('自习室已关闭，请选择其他自习室')
    return
  }
  
  // 自习室未到开放时间
  const now = new Date()
  const currentTime = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
  if (room.openTime > currentTime || room.closeTime < currentTime) {
    ElMessage.error('自习室未到开放时间')
    return
  }
  
  loading2.value = true
  roomShow.value = true
  selectedSeat.value = null
  roomData.value = room
  
  // 重置预约数据
  reservationData.value = {
    userId: null,
    seatId: null,
    status: null,
    reservationStatus: null,
    reservationInTime: null,
    reservationOutTime: null,
  }
  
  try {
    // 获取自习室座位
    await roomStore.getSeatsByRoomId(room.id)
    seatList.value = roomStore.seats
    
    // 检查当前用户是否已有预约
    await reservationStore.getReservations()
    const userReservations = reservationStore.reservations.filter(res => 
      res.userId === authStore.user?.id && 
      (res.reservationStatus === '已预约' || res.reservationStatus === '使用中')
    )
    
    if (userReservations.length > 0) {
      selectedSeat.value = userReservations[0].seatId
    }
  } catch (error) {
    ElMessage.error('获取座位信息失败')
  } finally {
    loading2.value = false
  }
}

// 根据自习室布局生成座位网格
const seatGrid = computed(() => {
  if (!roomData.value || !roomData.value.rowsCount || !roomData.value.colsCount) {
    return []
  }
  
  const rows = roomData.value.rowsCount // 行数
  const cols = roomData.value.colsCount // 列数
  
  // 创建一个空座位网格，用于填充
  const grid = []
  
  // 生成所有位置（包括空位）
  for (let rowNum = 1; rowNum <= rows; rowNum++) {
    for (let colNum = 1; colNum <= cols; colNum++) {
      // 默认创建空位
      grid.push({
        roomId: roomData.value.id,
        rowNum,
        colNum,
        status: null,
      })
    }
  }
  
  // 用实际座位数据替换空位
  seatList.value.forEach(seat => {
    // 座位编号 = (行号 - 1) × 列数 + 列号
    const index = (seat.rowNum - 1) * cols + seat.colNum - 1
    if (index >= 0 && index < grid.length) {
      grid[index] = seat
    }
  })
  
  return grid
})

// 获取座位状态样式
const getSeatClass = (seat) => {
  let baseClass = ''
  if (seat.status === 'available') baseClass = 'seat-normal';
  if (seat.status === 'fault') baseClass = 'seat-fault';
  if (seat.status === null) baseClass = 'seat-empty';
  if (seat.status === 'reserved') baseClass = 'seat-reserved';
  if (seat.status === 'using') baseClass = 'seat-using';
  // 添加选中状态样式
  if (selectedSeat.value === seat.id) {
    baseClass += ' seat-selected'
  }
  return baseClass
}

// 根据自习室布局生成座位网格样式
const gridStyle = computed(() => {
  if (!roomData.value || !roomData.value.colsCount) return {}
  return {
    gridTemplateColumns: `repeat(${roomData.value.colsCount}, 1fr)`
  }
})

// 自动适配座位样式大小
const seatSize = computed(() => {
  if (!roomData.value || !roomData.value.colsCount) return {}
  if (roomData.value.colsCount === 7) {
    return {
      width: '30px',
      height: '30px',
    }
  }
  if (roomData.value.colsCount === 8) {
    return {
      width: '26px',
      height: '26px',
    }
  }
  if (roomData.value.colsCount === 9) {
    return {
      width: '22px',
      height: '22px',
    }
  }
  if (roomData.value.colsCount >= 10) {
    return {
      width: '20px',
      height: '20px',
    }
  }
  return {
    width: '40px',
    height: '40px',
  }
})

// 选择座位
const selectSeat = (seat) => {
  // 检查座位状态
  if (seat.status === 'using') {
    ElMessage.error('该座位正在使用中，不可预约')
    return
  }
  if (seat.status === 'reserved') {
    ElMessage.error('该座位已预约，请勿重复预约')
    return
  }
  if (seat.status === 'fault') {
    ElMessage.error('该座位已故障，不可预约')
    return
  }
  
  // 检查用户是否已有预约
  const userReservations = reservationStore.reservations.filter(res => 
    res.userId === authStore.user?.id && 
    (res.reservationStatus === '已预约' || res.reservationStatus === '使用中')
  )
  
  if (userReservations.length > 0) {
    if (selectedSeat.value === seat.id) {
      ElMessage.success('你已预约此座位')
      return
    }
    ElMessage.error('你已预约，不可重复预约')
    return
  }
  
  // 选择座位并设置预约数据
  if (seat.status === 'available') {
    selectedSeat.value = seat.id
    const now = new Date()
    const nowIn = new Date()
    const nowOut = new Date()
    nowIn.setHours(now.getHours() + 1)
    nowOut.setHours(now.getHours() + 3)
    
    reservationData.value = {
      userId: authStore.user?.id,
      seatId: seat.id,
      status: '正常',
      reservationStatus: '已预约',
      // 预约时间（获取当前时间）
      reservationInTime: nowIn.toISOString().replace('T', ' ').split('.')[0],
      reservationOutTime: nowOut.toISOString().replace('T', ' ').split('.')[0],
    }
  }
}

// 提交预约
const handleReservation = async () => {
  if (!reservationData.value.userId) {
    ElMessage.error('请先登录')
    return
  }
  
  if (!reservationData.value.seatId) {
    ElMessage.error('请选择座位')
    return
  }
  
  try {
    // 检查用户是否被禁约
    const userReservations = reservationStore.reservations.filter(res => 
      res.userId === authStore.user?.id && 
      res.reservationStatus === '违约中'
    )
    
    if (userReservations.length >= 3) {
      ElMessage.error('您已被禁约，无法预约！')
      return
    }
    
    // 提交预约
    await reservationStore.createReservation(reservationData.value)
    roomShow.value = false
    ElMessage.success('预约成功')
    // 刷新预约列表
    await reservationStore.getReservations()
  } catch (error) {
    ElMessage.error('预约失败：' + error.message)
  }
}

// 初始化数据
const initData = async () => {
  await getRoomList()
  await reservationStore.getReservations()
}

// 获取自习室列表
const getRoomList = async () => {
  loading.value = true
  try {
    await roomStore.getRooms(searchKeyword.value)
    roomList.value = roomStore.rooms
  } catch (error) {
    ElMessage.error('获取自习室列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索自习室
const handleSearch = () => {
  if (!searchKeyword.value) {
    ElMessage.error('请输入自习室名称')
    return
  }
  resetBtnShow.value = true
  getRoomList()
}

// 重置搜索
const handleReset = () => {
  searchKeyword.value = ''
  resetBtnShow.value = false
  getRoomList()
}

// 初始化数据
initData()
</script>

<template>
  <div class="reservation-list">
    <el-container>
      <el-header>预约</el-header>
      <el-main>
        <!-- 搜索栏 -->
        <el-input v-model="searchKeyword" placeholder="搜索自习室" clearable>
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
        <el-button v-if="resetBtnShow" @click="handleReset" style="width: 100%;">重置</el-button>

        <!-- 自习室列表 -->
        <div class="room-list" v-loading="loading">
          <div class="room-card" v-for="item in roomList" :key="item.id" @click="selectRoom(item)">
            <div class="room-image">
              <img :src="item.image || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20study%20room%20interior&image_size=landscape_16_9'" alt="自习室图片">
            </div>
            <div class="left">
              <h4>{{ item.name }}</h4>
              <p>
                <el-icon>
                  <Location />
                </el-icon>
                {{ item.location }}
              </p>
              <p>
                <el-icon>
                  <Clock />
                </el-icon>
                {{ item.openTime }} ~ {{ item.closeTime }}
              </p>
            </div>
            <div class="right">
              <p>
                <el-tag type="success" v-if="item.status === 'available'">状态：正常运营</el-tag>
                <el-tag type="danger" v-else>状态：暂未开放</el-tag>
              </p>
              <p><el-tag>布局：{{ item.rowsCount }}行 × {{ item.colsCount }}列</el-tag></p>
            </div>
          </div>
          <el-empty v-if="roomList.length === 0" description="没有搜索到此自习室！" />
        </div>
      </el-main>

      <!-- 选择座位对话框 -->
      <el-dialog v-model="roomShow" title="选择座位" width="98%">
        <div class="room-info">
          <h4>{{ roomData.name }}</h4>
          <p>
            <el-icon>
              <Location />
            </el-icon>
            {{ roomData.location }}
          </p>
          <p>
            <el-icon>
              <Clock />
            </el-icon>
            {{ roomData.openTime }} ~ {{ roomData.closeTime }}
          </p>
        </div>
        
        <!-- 座位图例 -->
        <div class="legend">
          <div class="legend-item">
            <div class="legend-color" style="background-color: #409EFF;"></div>
            <span>空闲</span>
          </div>
          <div class="legend-item">
            <div class="legend-color" style="background-color: #E6A23C;"></div>
            <span>已预约</span>
          </div>
          <div class="legend-item">
            <div class="legend-color" style="background-color: #909399;"></div>
            <span>使用中</span>
          </div>
          <div class="legend-item">
            <div class="legend-color" style="background-color: #F56C6C;"></div>
            <span>故障</span>
          </div>
        </div>
        
        <!-- 座位地图 -->
        <div class="seat-map-container" v-loading="loading2">
          <div class="seat-map" :style="gridStyle">
            <div v-for="seat in seatGrid" :key="seat.id" class="seat" :class="getSeatClass(seat)" :style="seatSize"
              @click="selectSeat(seat)">
              {{ seat.seatNumber }}
              <span v-if="seat.id === selectedSeat">已选择</span>
              <span v-else-if="seat.status === 'available'">空闲</span>
              <span v-else-if="seat.status === 'fault'">故障</span>
              <span v-else-if="seat.status === 'reserved'">已预约</span>
              <span v-else-if="seat.status === 'using'">使用中</span>
            </div>
          </div>
        </div>
        
        <template #footer>
          <el-button @click="roomShow = false">取消</el-button>
          <el-button type="primary" @click="handleReservation" :disabled="!reservationData.userId || !reservationData.seatId">预约</el-button>
        </template>
      </el-dialog>
    </el-container>
  </div>
</template>

<style lang="scss" scoped>
.reservation-list {
  height: 100%;

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
      padding: 5px;

      .room-list {
        margin: 10px 0;
        display: flex;
        flex-direction: column;
        gap: 10px;

        .room-card {
          padding: 10px;
          background-color: #ffffff;
          border-radius: 8px;
          box-shadow: 0 2px 6px #00000020;
          cursor: pointer;
          transition: transform 0.2s;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px #00000020;
          }

          .room-image {
            width: 100%;
            height: 150px;
            border-radius: 8px;
            overflow: hidden;

            img {
              width: 100%;
              height: 100%;
              object-fit: cover;
            }
          }

          .left {
            float: left;
            width: 50%;

            h4 {
              padding: 4px 0;
              color: #555555;
              font-size: 16px;
            }

            p {
              display: flex;
              align-items: center;
              gap: 2px;
              padding: 4px 0;
              color: #555555;
              font-size: 12px;
            }
          }

          .right {
            float: right;
            text-align: right;
            width: 50%;

            .el-tag {
              margin: 4px 0;
            }
          }
        }
      }
    }

    .room-info {
      padding: 10px;
      background-color: #f8f9fa;
      border-radius: 8px;

      h4 {
        font-size: 16px;
        text-align: center;
      }

      p {
        display: flex;
        align-items: center;
        gap: 2px;
        padding: 4px 0;
        color: #555555;
        font-size: 12px;
      }
    }

    // 座位图例
    .legend {
      display: flex;
      justify-content: space-around;
      margin: 20px auto;
      max-width: 400px;

      .legend-item {
        display: flex;
        align-items: center;
        gap: 5px;
        font-size: 0.9rem;
      }

      .legend-color {
        width: 16px;
        height: 16px;
        border-radius: 4px;
      }
    }

    // 座位地图
    .seat-map-container {
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 20px auto;
      max-width: 600px;
      max-height: 600px;
      overflow: auto;

      .seat-map {
        display: grid;
        gap: 10px;
        padding: 15px;
        background: #f7fafc;
        border-radius: 8px;
        box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.05);
        justify-content: center;
      }

      .seat {
        display: flex;
        align-items: center;
        justify-content: center;
        flex-direction: column;
        width: 40px;
        height: 40px;
        font-size: 0.8rem;
        font-weight: bold;
        border-radius: 6px;
        cursor: pointer;
        transition: all 0.2s;
        box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);

        span {
          font-size: 0.6rem;
          font-weight: normal;
        }
      }

      .seat-normal:hover {
        transform: scale(1.1);
      }

      .seat-normal {
        background: #409EFF99;
        color: #FFFFFF;
        border: 2px solid #409EFF;
      }

      .seat-reserved {
        background: #E6A23C99;
        color: #FFFFFF;
        border: 2px solid #E6A23C;
        box-shadow: none;
      }

      .seat-using {
        background: #90939999;
        color: #FFFFFF;
        border: 2px solid #909399;
        box-shadow: none;
      }

      .seat-fault {
        background: #F56C6C99;
        color: #FFFFFF;
        border: 2px solid #F56C6C;
        box-shadow: none;
      }

      .seat-empty {
        border: 1px dashed #E9E9EB;
        box-shadow: none;
      }

      .seat-selected {
        background: #67C23A99;
        color: #FFFFFF;
        border: 2px solid #67C23A;
      }
    }
  }
}
</style>