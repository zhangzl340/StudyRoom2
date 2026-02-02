<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Refresh, Filter } from '@element-plus/icons-vue'
import { useRoomStore } from '../../stores/room'

const roomStore = useRoomStore()

// 加载状态
const loading = ref(true)

// 搜索关键词
const searchKeyword = ref('')

// 分页参数
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 座位列表
const seats = ref([])

// 自习室列表
const rooms = ref([])

// 选中的自习室
const selectedRoomId = ref('')

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('添加座位')

// 表单数据
const form = ref({
  id: '',
  roomId: '',
  seatNumber: '',
  rowNum: 1,
  colNum: 1,
  status: 'available'
})

// 表单验证规则
const rules = {
  roomId: [
    { required: true, message: '请选择自习室', trigger: 'blur' }
  ],
  seatNumber: [
    { required: true, message: '请输入座位编号', trigger: 'blur' }
  ],
  rowNum: [
    { required: true, message: '请输入行号', trigger: 'blur' },
    { type: 'number', message: '请输入数字', trigger: 'blur' }
  ],
  colNum: [
    { required: true, message: '请输入列号', trigger: 'blur' },
    { type: 'number', message: '请输入数字', trigger: 'blur' }
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
  } catch (error) {
    ElMessage.error('获取自习室列表失败')
  }
}

// 获取座位列表
const getSeats = async () => {
  loading.value = true
  try {
    if (selectedRoomId.value) {
      await roomStore.getSeatsByRoomId(selectedRoomId.value)
      seats.value = roomStore.seats
    } else {
      // 暂时获取所有自习室的座位
      let allSeats = []
      for (const room of rooms.value) {
        await roomStore.getSeatsByRoomId(room.id)
        allSeats = [...allSeats, ...roomStore.seats]
      }
      seats.value = allSeats
    }
    pagination.value.total = seats.value.length
  } catch (error) {
    ElMessage.error('获取座位列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.currentPage = 1
  getSeats()
}

// 重置搜索
const handleReset = () => {
  searchKeyword.value = ''
  selectedRoomId.value = ''
  pagination.value.currentPage = 1
  getSeats()
}

// 分页变化
const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  getSeats()
}

const handleCurrentChange = (current) => {
  pagination.value.currentPage = current
  getSeats()
}

// 自习室选择变化
const handleRoomChange = () => {
  pagination.value.currentPage = 1
  getSeats()
}

// 打开添加对话框
const openAddDialog = () => {
  dialogTitle.value = '添加座位'
  form.value = {
    id: '',
    roomId: selectedRoomId.value || '',
    seatNumber: '',
    rowNum: 1,
    colNum: 1,
    status: 'available'
  }
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (seat) => {
  dialogTitle.value = '编辑座位'
  form.value = {
    id: seat.id,
    roomId: seat.roomId,
    seatNumber: seat.seatNumber,
    rowNum: seat.rowNum,
    colNum: seat.colNum,
    status: seat.status
  }
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (formRef.value) {
    formRef.value.validate(async (valid) => {
      if (valid) {
        try {
          if (form.value.id) {
            // 编辑
            // 这里可以调用后端API更新座位
            ElMessage.success('编辑成功')
          } else {
            // 添加
            // 这里可以调用后端API创建座位
            ElMessage.success('添加成功')
          }
          dialogVisible.value = false
          getSeats()
        } catch (error) {
          ElMessage.error('操作失败')
        }
      }
    })
  }
}

// 删除座位
const deleteSeat = (seat) => {
  ElMessageBox.confirm('确定要删除这个座位吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 这里可以调用后端API删除座位
      ElMessage.success('删除成功')
      getSeats()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 刷新列表
const refreshList = () => {
  getSeats()
}

// 初始化
onMounted(async () => {
  await getRooms()
  await getSeats()
})
</script>

<template>
  <div class="admin-seat-list">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>座位管理</span>
          <el-button type="primary" :icon="Plus" @click="openAddDialog">添加座位</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-select v-model="selectedRoomId" placeholder="选择自习室" style="width: 200px" @change="handleRoomChange">
          <el-option label="全部自习室" value="" />
          <el-option v-for="room in rooms" :key="room.id" :label="room.name" :value="room.id" />
        </el-select>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索座位编号"
          :prefix-icon="Search"
          style="width: 200px"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch"></el-button>
            <el-button :icon="Refresh" @click="handleReset"></el-button>
          </template>
        </el-input>
        <el-button :icon="Refresh" @click="refreshList">刷新</el-button>
      </div>
      
      <!-- 座位列表 -->
      <el-table
        v-loading="loading"
        :data="seats"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="自习室" width="150">
          <template #default="scope">
            {{ rooms.find(r => r.id === scope.row.roomId)?.name || '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="seatNumber" label="座位编号" width="120" />
        <el-table-column label="位置" width="120">
          <template #default="scope">
            第{{ scope.row.rowNum }}行第{{ scope.row.colNum }}列
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag type="success" v-if="scope.row.status === 'available'">可用</el-tag>
            <el-tag type="danger" v-else-if="scope.row.status === 'fault'">故障</el-tag>
            <el-tag type="warning" v-else-if="scope.row.status === 'reserved'">已预约</el-tag>
            <el-tag type="info" v-else-if="scope.row.status === 'using'">使用中</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" :icon="Edit" @click="openEditDialog(scope.row)"></el-button>
            <el-button type="danger" size="small" :icon="Delete" @click="deleteSeat(scope.row)"></el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="自习室" prop="roomId">
          <el-select v-model="form.roomId" placeholder="选择自习室">
            <el-option v-for="room in rooms" :key="room.id" :label="room.name" :value="room.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="座位编号" prop="seatNumber">
          <el-input v-model="form.seatNumber" placeholder="请输入座位编号" />
        </el-form-item>
        <el-form-item label="行号" prop="rowNum">
          <el-input-number v-model="form.rowNum" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="列号" prop="colNum">
          <el-input-number v-model="form.colNum" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="选择状态">
            <el-option label="可用" value="available" />
            <el-option label="故障" value="fault" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.admin-seat-list {
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .search-bar {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-bar {
    flex-direction: column;
    align-items: stretch;

    .el-select,
    .el-input {
      width: 100% !important;
    }
  }

  .el-table {
    .el-table__header-wrapper,
    .el-table__body-wrapper {
      overflow-x: auto;
    }
  }

  .pagination {
    justify-content: center;
  }
}
</style>