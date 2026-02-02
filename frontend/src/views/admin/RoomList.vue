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

// 自习室列表
const rooms = ref([])

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('添加自习室')

// 表单数据
const form = ref({
  id: '',
  name: '',
  location: '',
  rowsCount: 5,
  colsCount: 8,
  openTime: '08:00',
  closeTime: '22:00',
  status: 'available',
  image: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入自习室名称', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入自习室位置', trigger: 'blur' }
  ],
  rowsCount: [
    { required: true, message: '请输入行数', trigger: 'blur' },
    { type: 'number', message: '请输入数字', trigger: 'blur' }
  ],
  colsCount: [
    { required: true, message: '请输入列数', trigger: 'blur' },
    { type: 'number', message: '请输入数字', trigger: 'blur' }
  ],
  openTime: [
    { required: true, message: '请输入开放时间', trigger: 'blur' }
  ],
  closeTime: [
    { required: true, message: '请输入关闭时间', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'blur' }
  ]
}

// 表单引用
const formRef = ref(null)

// 获取自习室列表
const getRooms = async () => {
  loading.value = true
  try {
    await roomStore.getRooms(searchKeyword.value)
    rooms.value = roomStore.rooms
    pagination.value.total = roomStore.rooms.length
  } catch (error) {
    ElMessage.error('获取自习室列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.currentPage = 1
  getRooms()
}

// 重置搜索
const handleReset = () => {
  searchKeyword.value = ''
  pagination.value.currentPage = 1
  getRooms()
}

// 分页变化
const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  getRooms()
}

const handleCurrentChange = (current) => {
  pagination.value.currentPage = current
  getRooms()
}

// 打开添加对话框
const openAddDialog = () => {
  dialogTitle.value = '添加自习室'
  form.value = {
    id: '',
    name: '',
    location: '',
    rowsCount: 5,
    colsCount: 8,
    openTime: '08:00',
    closeTime: '22:00',
    status: 'available',
    image: ''
  }
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (room) => {
  dialogTitle.value = '编辑自习室'
  form.value = {
    id: room.id,
    name: room.name,
    location: room.location,
    rowsCount: room.rowsCount,
    colsCount: room.colsCount,
    openTime: room.openTime,
    closeTime: room.closeTime,
    status: room.status,
    image: room.image
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
            await roomStore.updateRoom(form.value)
            ElMessage.success('编辑成功')
          } else {
            // 添加
            await roomStore.createRoom(form.value)
            ElMessage.success('添加成功')
          }
          dialogVisible.value = false
          getRooms()
        } catch (error) {
          ElMessage.error('操作失败')
        }
      }
    })
  }
}

// 删除自习室
const deleteRoom = (room) => {
  ElMessageBox.confirm('确定要删除这个自习室吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await roomStore.deleteRoom(room.id)
      ElMessage.success('删除成功')
      getRooms()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 刷新列表
const refreshList = () => {
  getRooms()
}

// 初始化
onMounted(() => {
  getRooms()
})
</script>

<template>
  <div class="admin-room-list">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>自习室管理</span>
          <el-button type="primary" :icon="Plus" @click="openAddDialog">添加自习室</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索自习室名称或位置"
          :prefix-icon="Search"
          style="width: 300px"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch"></el-button>
            <el-button :icon="Refresh" @click="handleReset"></el-button>
          </template>
        </el-input>
        <el-button :icon="Refresh" @click="refreshList">刷新</el-button>
      </div>
      
      <!-- 自习室列表 -->
      <el-table
        v-loading="loading"
        :data="rooms"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="自习室名称" />
        <el-table-column prop="location" label="位置" />
        <el-table-column label="布局" width="120">
          <template #default="scope">
            {{ scope.row.rowsCount }}行 × {{ scope.row.colsCount }}列
          </template>
        </el-table-column>
        <el-table-column label="开放时间" width="180">
          <template #default="scope">
            {{ scope.row.openTime }} ~ {{ scope.row.closeTime }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag type="success" v-if="scope.row.status === 'available'">可用</el-tag>
            <el-tag type="danger" v-else>不可用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" :icon="Edit" @click="openEditDialog(scope.row)"></el-button>
            <el-button type="danger" size="small" :icon="Delete" @click="deleteRoom(scope.row)"></el-button>
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
        <el-form-item label="自习室名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入自习室名称" />
        </el-form-item>
        <el-form-item label="自习室位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入自习室位置" />
        </el-form-item>
        <el-form-item label="行数" prop="rowsCount">
          <el-input-number v-model="form.rowsCount" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="列数" prop="colsCount">
          <el-input-number v-model="form.colsCount" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="开放时间" prop="openTime">
          <el-time-picker v-model="form.openTime" format="HH:mm" placeholder="选择开放时间" />
        </el-form-item>
        <el-form-item label="关闭时间" prop="closeTime">
          <el-time-picker v-model="form.closeTime" format="HH:mm" placeholder="选择关闭时间" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="选择状态">
            <el-option label="可用" value="available" />
            <el-option label="不可用" value="unavailable" />
          </el-select>
        </el-form-item>
        <el-form-item label="自习室图片">
          <el-input v-model="form.image" placeholder="请输入图片URL" />
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
.admin-room-list {
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