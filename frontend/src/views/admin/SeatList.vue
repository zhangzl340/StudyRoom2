<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoomStore } from '../../stores/room'
import * as roomService from '../../services/room'

const roomStore = useRoomStore()

// 加载状态
const loading = ref(true)

// 自习室列表
const rooms = ref([])

// 当前选中的自习室
const roomData = ref({})

// 座位列表
const seatList = ref([])

// 总座位数
const total = ref(0)

// 对话框状态
const open = ref(false)
const title = ref('添加自习室座位')

// 表单数据
const form = ref({
  id: '',
  roomId: '',
  seatNum: '',
  rowNum: 1,
  colNum: 1,
  status: 'available'
})

// 表单验证规则
const rules = {
  seatNum: [
    { required: true, message: '请输入座位号', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'blur' }
  ]
}

// 表单引用
const formRef = ref(null)

// 获取自习室列表
const getRooms = async () => {
  try {
    await roomStore.getRooms()
    rooms.value = roomStore.rooms
    if (rooms.value.length > 0) {
      roomData.value = rooms.value[0]
      await getSeats()
    }
  } catch (error) {
    ElMessage.error('获取自习室列表失败')
  }
}

// 获取座位列表
const getSeats = async () => {
  loading.value = true
  try {
    if (roomData.value.id) {
      await roomStore.fetchSeats(roomData.value.id)
      seatList.value = roomStore.seats
      total.value = seatList.value.length
    }
  } catch (error) {
    ElMessage.error('获取座位列表失败')
  } finally {
    loading.value = false
  }
}

// 自习室选择
const roomSelect = async (row) => {
  roomData.value = row
  await getSeats()
}

// 表格行样式
const tableRowClassName = ({ row }) => {
  if (row.id === roomData.value.id) return 'on-row'
}
// 座位状态常量（可以移到单独的文件中维护）
const SEAT_STATUS = {
  AVAILABLE: 'available',    // 正常/可用
  OCCUPIED: 'occupied',      // 使用中
  FAULT: 'fault'             // 故障
}
// 获取座位状态样式
const getSeatClass = (seat) => {
  if (seat.status === SEAT_STATUS.AVAILABLE) return 'seat-available'
  if (seat.status === SEAT_STATUS.OCCUPIED) return 'seat-occupied'
  if (seat.status === SEAT_STATUS.FAULT) return 'seat-fault'
  if (seat.status === null) return 'seat-empty'
}

// 设置座位
const setSeat = async (seat) => {
  resetForm()
  if (seat.status === null) {
    open.value = true
    title.value = "添加自习室座位"
    form.value.roomId = seat.roomId
    form.value.rowNum = seat.rowNum
    form.value.colNum = seat.colNum
  } 
  // else if (seat.status === SEAT_STATUS.OCCUPIED) {
  //   // 如果座位正在使用中，不允许编辑，只能查看
  //   ElMessage.warning('该座位正在使用中，无法编辑')
  //   return
  // }
  else {
    // 这里可以调用后端API获取座位详情
    form.value = {
      id: seat.id,
      roomId: seat.roomId,
      seatNum: seat.seatNumber || seat.seatNum,
      rowNum: seat.rowNum,
      colNum: seat.colNum,
      status: seat.status
    }
    open.value = true
    title.value = "修改自习室座位"
  }
}

// 重置表单
const resetForm = () => {
  form.value = {
    id: '',
    roomId: '',
    seatNum: '',
    rowNum: 1,
    colNum: 1,
    status: 'available'
  }
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 提交表单
const submitForm = async () => {
  if (formRef.value) {
    formRef.value.validate(async (valid) => {
      if (valid) {
        try {
          // if (form.value.id) {
          //   const currentSeat = seatList.value.find(s => s.id === form.value.id)
          //   if (currentSeat?.status === SEAT_STATUS.OCCUPIED) {
          //     ElMessage.error('该座位正在使用中，无法修改')
          //     return
          //   }
          // }
          if (form.value.id) {
            // 编辑座位
            await roomService.updateSeatStatus(form.value.id, form.value.status)
            ElMessage.success('修改成功')
          } else {
            // 添加座位
            const newSeat = {
              seatNum: form.value.seatNum,
              roomId:roomData.value.id,
              rowNum: form.value.rowNum,
              colNum: form.value.colNum,
              status: form.value.status
            }
            await roomService.createSeats(form.value.roomId, [newSeat])
            ElMessage.success('新增成功')
          }
          open.value = false
          await getSeats()
        } catch (error) {
          ElMessage.error('操作失败')
        }
      }
    })
  }
}

// 删除座位
const handleDelete = async (row) => {
  const ids = row.id || form.value.id
  const seatToDelete = seatList.value.find(s => s.id === ids)
    // 检查座位是否在使用中
  if (seatToDelete?.status === SEAT_STATUS.OCCUPIED) {
    ElMessage.warning('该座位正在使用中，无法删除')
    return
  }
  ElMessageBox.confirm('是否确认删除自习室座位编号为"' + ids + '"的数据项？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 这里可以调用后端API删除座位
      ElMessage.success('删除成功')
      open.value = false
      await getSeats()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  if (time.length > 5) {
    return time.substring(0, 5)
  }
  return time
}

// 根据自习室布局生成座位网格
const seatGrid = computed(() => {
  if (!roomData.value) return []
  const rows = roomData.value.rowsCount || 0
  const cols = roomData.value.colsCount || 0
  const grid = []
  
  // 生成所有位置（包括空位）
  for (let rowNum = 1; rowNum <= rows; rowNum++) {
    for (let colNum = 1; colNum <= cols; colNum++) {
      grid.push({
        roomId: roomData.value.id,
        rowNum,
        colNum,
        status: null
      })
    }
  }
  
  // 用实际座位数据替换空位
  seatList.value.forEach(seat => {
    const index = (seat.rowNum - 1) * cols + seat.colNum - 1
    if (index >= 0 && index < grid.length) {
      grid[index] = seat
    }
  })
  
  return grid
})

// 座位网格样式
const gridStyle = computed(() => {
  if (!roomData.value) return {}
  return {
    gridTemplateColumns: `repeat(${roomData.value.colsCount || 1}, 1fr)`
  }
})

// 初始化
onMounted(async () => {
  await getRooms()
})
</script>

<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 左侧自习室信息 -->
      <el-col :span="8">
        <el-card class="box-card">
          <h2 class="title">自习室信息</h2>
          <div class="room-info" v-if="roomData">
            <div class="info-item">
              <span class="info-label">自习室:</span>
              <span class="info-value">{{ roomData.name }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">位置:</span>
              <span class="info-value">{{ roomData.location }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">布局:</span>
              <span class="info-value">{{ roomData.rowsCount }}行 × {{ roomData.colsCount }}列</span>
            </div>
            <div class="info-item">
              <span class="info-label">座位数:</span>
              <span class="info-value">{{ total }}个</span>
            </div>
            <div class="info-item">
              <span class="info-label">开放时间:</span>
              <span class="info-value">{{ formatTime(roomData.openTime) }} - {{ formatTime(roomData.closeTime) }}</span>
            </div>
          </div>
          <el-divider></el-divider>
          <el-table class="room-list" :data="rooms" @row-click="roomSelect" :show-header="false"
            :row-class-name="tableRowClassName" height="170">
            <el-table-column prop="name" />
            <el-table-column prop="status" width="80">
              <template #default="scope">
                <el-tag v-if="scope.row.status === 'open'" size="small" type="success">可用</el-tag>
                <el-tag v-else-if="scope.row.status === 'closed'" size="small" type="danger">关闭</el-tag>
                <el-tag v-else size="small" type="warning">维护</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-divider></el-divider>
        </el-card>
      </el-col>
      
      <!-- 右侧座位分布图 -->
      <el-col :span="16">
        <el-card class="box-card">
          <h2 class="title">座位分布图</h2>
          <div class="legend">
            <div class="legend-item">
              <div class="legend-color" style="background-color: #67C23A;"></div>
              <span>可用</span>
            </div>
            <div class="legend-item">
              <div class="legend-color" style="background-color: #E6A23C;"></div>
              <span>使用中</span>
            </div>
            <div class="legend-item">
              <div class="legend-color" style="background-color: #F56C6C;"></div>
              <span>故障</span>
            </div>
            <div class="legend-item">
              <div class="legend-color" style="border: 2px dashed #E9E9EB;"></div>
              <span>空位</span>
            </div>
          </div>
          <div class="seat-map-container" v-loading="loading">
            <div class="seat-map" :style="gridStyle">
              <div v-for="seat in seatGrid" :key="`${seat.rowNum}-${seat.colNum}`" class="seat" :class="getSeatClass(seat)"
                @click="setSeat(seat)">
                {{ seat.seatNumber || seat.seatNum || '' }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 添加或修改自习室座位对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="位置">
          <el-tag>{{ roomData.name }}</el-tag>
          <span> - </span>
          <el-tag type="success">第 {{ form.rowNum }} 行 - 第 {{ form.colNum }} 个座位</el-tag>
        </el-form-item>
        <el-form-item label="座位号" prop="seatNum">
          <el-input v-model="form.seatNum" placeholder="请输入座位号" />
        </el-form-item>
       <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status" 
                         >
            <el-radio :label="SEAT_STATUS.AVAILABLE">可用</el-radio>
            <el-radio :label="SEAT_STATUS.OCCUPIED">使用中</el-radio>
            <el-radio :label="SEAT_STATUS.FAULT">故障</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="danger" v-if="form.id" @click="handleDelete(form)">删 除 座 位</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.box-card {
  min-height: 600px;
}

.title {
  margin: 10px 0 20px;
  color: #409EFF;
  font-size: 20px;
  font-weight: bold;
  text-align: center;
}

.room-info {
  margin-bottom: 25px;

  .info-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 10px;
    padding: 10px 15px;
    font-size: 14px;
    background: #f8fafc;
    border-radius: 8px;
    transition: background 0.3s;
  }

  .info-item:hover {
    background: #f0f5ff;
  }

  .info-label {
    font-weight: bold;
    color: #4a5568;
  }

  .info-value {
    color: #2d3748;
    font-weight: 500;
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
    width: 45px;
    height: 45px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 6px;
    font-weight: bold;
    cursor: pointer;
    transition: all 0.2s;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
    font-size: 0.8rem;
  }

  .seat:hover {
    transform: scale(1.1);
  }

    // 可用状态（绿色）
  .seat-available {
    background: #67C23A99;
    color: #FFFFFF;
    border: 2px solid #67C23A;
  }

  // 使用中状态（橙色）
  .seat-occupied {
    background: #E6A23C99;
    color: #FFFFFF;
    border: 2px solid #E6A23C;
    cursor: not-allowed;
    
    &:hover {
      transform: none;
    }
  }

  // 故障状态（红色）
  .seat-fault {
    background: #F56C6C99;
    color: #FFFFFF;
    border: 2px solid #F56C6C;
  }

  .seat-empty {
    border: 2px dashed #E9E9EB;
    box-shadow: none;
  }


  @media (max-width: 1200px) {
    .seat {
      width: 40px;
      height: 40px;
    }
  }

  @media (max-width: 1080px) {
    .seat {
      width: 30px;
      height: 30px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .el-col {
    flex: 100%;
    max-width: 100%;
  }

  .box-card {
    margin-bottom: 20px;
  }
}
</style>

<style>
.el-table .on-row {
  color: #ffffff;
  font-weight: bold;
  background: #409EFF;
}
</style>