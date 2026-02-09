<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Delete, Refresh, Download } from '@element-plus/icons-vue'
import { useReservationStore } from '../../stores/reservation'
import { useRoomStore } from '../../stores/room'
import * as reservationService from '../../services/reservation'

const reservationStore = useReservationStore()
const roomStore = useRoomStore()

// 加载状态
const loading = ref(true)

// 搜索参数
const queryParams = ref({
  userId: '',
  status: '',
  reservationStatus: '',
  pageNum: 1,
  pageSize: 10
})

// 分页参数
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 预约列表
const reservationList = ref([])

// 计算当前页数据
const currentPageData = computed(() => {
  const start = (pagination.value.currentPage - 1) * pagination.value.pageSize
  const end = start + pagination.value.pageSize
  return reservationList.value.slice(start, end)
})

// 自习室列表
const rooms = ref([])

// 座位列表
const seats = ref([])

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('编辑预约')

// 表单数据
const form = ref({
  id: '',
  userId: '',
  seatId: '',
  status: '',
  reservationStatus: '',
  reservationInTime: '',
  reservationOutTime: '',
  signInTime: '',
  signOutTime: '',
  remark: ''
})

// 表单验证规则
const rules = {
  status: [
    { required: true, message: '请选择状态', trigger: 'blur' }
  ],
  reservationStatus: [
    { required: true, message: '请选择预约状态', trigger: 'blur' }
  ],
  reservationInTime: [
    { required: true, message: '请选择预约开始时间', trigger: 'blur' }
  ],
  reservationOutTime: [
    { required: true, message: '请选择预约结束时间', trigger: 'blur' }
  ]
}

// 表单引用
const formRef = ref(null)

// 复选框选中状态
const multipleSelection = ref([])
const single = ref(true)
const multiple = ref(true)

// 获取自习室列表
const getRooms = async () => {
  try {
    await roomStore.getRooms()
    rooms.value = roomStore.rooms
  } catch (error) {
    ElMessage.error('获取自习室列表失败')
  }
}

// 获取预约列表
const getReservations = async () => {
  loading.value = true
  try {
    // 构造后端API参数
    const params = {
      page: queryParams.value.pageNum || 1,
      size: queryParams.value.pageSize || 10
    }
    if(queryParams.value.userId){
      params.userId = parseInt(queryParams.value.userId)
    }
    if(queryParams.value.status){
      params.status = queryParams.value.status
    }
    if(queryParams.value.reservationStatus){
      params.reservationStatus = queryParams.value.reservationStatus
    }
    // 直接调用后端API
    const response = await reservationService.getReservationList(params)
    
    if (response.code === 200) {
      let filteredReservations = response.data
      
      // 前端额外筛选
      if (queryParams.value.userName) {
        // 只根据userId筛选，因为后端没有userName字段
        filteredReservations = filteredReservations.filter(item => 
          item.userId?.toString().includes(queryParams.value.userName)
        )
      }
      if (queryParams.value.reservationStatus) {
        filteredReservations = filteredReservations.filter(item => item.reservationStatus === queryParams.value.reservationStatus)
      }
      
      reservationList.value = filteredReservations
      pagination.value.total = filteredReservations.length
    } else {
      ElMessage.error('获取预约列表失败：' + response.message)
    }
  } catch (error) {
    ElMessage.error('获取预约列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getReservations()
}

// 重置搜索
const handleReset = () => {
  queryParams.value = {
    userName: '',
    status: '',
    reservationStatus: '',
    pageNum: 1,
    pageSize: 10
  }
  getReservations()
}

// 分页变化
const handleSizeChange = (size) => {
  queryParams.value.pageSize = size
  getReservations()
}

const handleCurrentChange = (current) => {
  queryParams.value.pageNum = current
  getReservations()
}

// 复选框选中变化
const handleSelectionChange = (val) => {
  multipleSelection.value = val
  single.value = val.length !== 1
  multiple.value = !val.length
}

// 打开编辑对话框
const openEditDialog = (reservation) => {
  dialogTitle.value = '编辑预约'
  form.value = {
    id: reservation.id,
    userId: reservation.userId,
    seatId: reservation.seatId,
    status: reservation.status,
    reservationStatus: reservation.reservationStatus,
    reservationInTime: reservation.reservationInTime,
    reservationOutTime: reservation.reservationOutTime,
    signInTime: reservation.signInTime,
    signOutTime: reservation.signOutTime,
    remark: reservation.remark
  }
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (formRef.value) {
    formRef.value.validate(async (valid) => {
      if (valid) {
        try {
          // 构造与后端实体类一致的参数
          const reservationData = {
            userId: form.value.userId,
            seatId: form.value.seatId,
            status: form.value.status,
            reservationStatus: form.value.reservationStatus,
            reservationInTime: form.value.reservationInTime,
            reservationOutTime: form.value.reservationOutTime,
            signInTime: form.value.signInTime,
            signOutTime: form.value.signOutTime,
            remark: form.value.remark
          }
          
          // 调用后端API更新预约
          const response = await reservationService.updateReservation(form.value.id, reservationData)
          
          if (response.code === 200) {
            ElMessage.success('编辑成功')
            dialogVisible.value = false
            getReservations()
          } else {
            ElMessage.error('操作失败：' + response.message)
          }
        } catch (error) {
          ElMessage.error('操作失败：' + error.message)
        }
      }
    })
  }
}

// 删除预约
const handleDelete = (reservation) => {
  const ids = reservation ? [reservation.id] : multipleSelection.value.map(item => item.id)
  ElMessageBox.confirm(`确定要删除选中的${ids.length}条预约记录吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 逐个删除预约（后端暂不支持批量删除）
      for (const id of ids) {
        // 这里可以调用后端API删除预约
        // 由于后端没有提供删除API，我们可以通过更新预约状态为取消来实现
        const response = await reservationService.updateReservation(id, { 
          status: 'violation', 
          reservationStatus: '取消预约' 
        })
        
        if (response.code !== 200) {
          throw new Error(response.message || '删除失败')
        }
      }
      ElMessage.success('删除成功')
      getReservations()
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    }
  }).catch(() => {
    // 取消删除
  })
}

// 导出预约
const handleExport = async () => {
  try {
    // 调用后端API导出预约
    // 由于后端没有提供导出API，这里暂时模拟导出功能
    // 实际项目中，应该调用后端的导出API，然后下载文件
    const response = await fetch('/api/reservation/export', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    if (response.ok) {
      // 处理文件下载
      const blob = await response.blob()
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `reservation_${new Date().getTime()}.xlsx`
      document.body.appendChild(a)
      a.click()
      window.URL.revokeObjectURL(url)
      document.body.removeChild(a)
      ElMessage.success('导出成功')
    } else {
      ElMessage.error('导出失败')
    }
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 刷新列表
const refreshList = () => {
  getReservations()
}

// 获取自习室名称
const getRoomName = (roomId) => {
  const room = rooms.value.find(r => r.id === roomId)
  return room ? room.name : '未知'
}

// 获取座位编号
const getSeatNumber = (seatId) => {
  const seat = seats.value.find(s => s.id === seatId)
  return seat ? seat.seatNumber : '未知'
}

// 初始化
onMounted(async () => {
  await getRooms()
  await getReservations()
})
</script>

<template>
  <div class="admin-reservation-list">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>预约管理</span>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true">
          <el-form-item label="用户ID/姓名" prop="userName">
            <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
              <el-option label="正常" value="pending" />
              <el-option label="违约" value="violation" />
            </el-select>
          </el-form-item>
          <el-form-item label="预约状态" prop="reservationStatus">
            <el-select v-model="queryParams.reservationStatus" placeholder="请选择预约状态" clearable>
              <el-option label="已预约" value="已预约" />
              <el-option label="使用中" value="使用中" />
              <el-option label="违约中" value="违约中" />
              <el-option label="取消预约" value="取消预约" />
              <el-option label="完成预约" value="完成预约" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 操作栏 -->
      <div class="action-bar">
        <el-button type="danger" plain :icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>
        <el-button type="warning" plain :icon="Download" @click="handleExport">导出</el-button>
        <el-button :icon="Refresh" @click="refreshList">刷新</el-button>
      </div>
      
      <!-- 预约列表 -->
      <el-table
        v-loading="loading"
        :data="currentPageData"
        style="width: 100%"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="seatId" label="座位ID" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag type="success" v-if="scope.row.status === 'pending'">正常</el-tag>
            <el-tag type="danger" v-else>违约</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reservationStatus" label="预约状态" width="120">
          <template #default="scope">
            <el-tag type="warning" v-if="scope.row.reservationStatus === '违约中'">违约中</el-tag>
            <el-tag v-else>{{ scope.row.reservationStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预约时间" width="280">
          <template #default="scope">
            <el-tag type="info">{{ scope.row.reservationInTime }} ~ {{ scope.row.reservationOutTime }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="签到签退时间" width="280">
          <template #default="scope">
            <el-tag type="info">{{ scope.row.signInTime || '-' }} ~ {{ scope.row.signOutTime || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
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
    
    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="auto">
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" clearable>
            <el-option label="正常" value="pending" />
            <el-option label="违约" value="violation" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约状态" prop="reservationStatus">
          <el-select v-model="form.reservationStatus" placeholder="请选择预约状态" clearable>
            <el-option label="已预约" value="已预约" />
            <el-option label="使用中" value="使用中" />
            <el-option label="违约中" value="违约中" />
            <el-option label="取消预约" value="取消预约" />
            <el-option label="完成预约" value="完成预约" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约开始时间" prop="reservationInTime">
          <el-date-picker clearable v-model="form.reservationInTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择预约开始时间" />
        </el-form-item>
        <el-form-item label="预约结束时间" prop="reservationOutTime">
          <el-date-picker clearable v-model="form.reservationOutTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择预约结束时间" />
        </el-form-item>
        <el-form-item label="签到时间" prop="signInTime">
          <el-date-picker clearable v-model="form.signInTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择签到时间" />
        </el-form-item>
        <el-form-item label="签退时间" prop="signOutTime">
          <el-date-picker clearable v-model="form.signOutTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择签退时间" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="4" maxlength="100" show-word-limit placeholder="请输入备注" />
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
.admin-reservation-list {
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .search-bar {
    margin-bottom: 20px;
  }

  .action-bar {
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
    .el-form {
      display: flex;
      flex-direction: column;
      align-items: stretch;
      gap: 10px;
    }
  }

  .action-bar {
    flex-direction: column;
    align-items: stretch;
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