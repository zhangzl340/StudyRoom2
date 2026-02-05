<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Plus, 
  Edit, 
  Delete, 
  Refresh, 
  Upload, 
  Picture 
} from '@element-plus/icons-vue'
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
  openTime: '08:00:00',
  closeTime: '22:00:00',
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
    { 
      validator: (rule, value, callback) => {
        if (!Number.isInteger(value) || value < 1) {
          callback(new Error('行数必须是正整数'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  colsCount: [
    { required: true, message: '请输入列数', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!Number.isInteger(value) || value < 1) {
          callback(new Error('列数必须是正整数'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
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

// 图片相关状态
const uploadLoading = ref(false)
const imageUrl = ref('')
const previewDialogVisible = ref(false)
const previewImage = ref('')

// 时间格式化函数
const formatTimeToDate = (timeStr) => {
  if (!timeStr) return null
  const today = new Date()
  const [hours, minutes, seconds = '00'] = timeStr.split(':')
  today.setHours(hours, minutes, seconds)
  return today
}

const formatDateToTime = (date) => {
  if (!date) return ''
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  const seconds = date.getSeconds().toString().padStart(2, '0')
  return `${hours}:${minutes}:${seconds}`
}

// 文件上传前的验证
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  
  return true
}
// 图片上传方法
const handleImageUpload = async (file) => {
  if(!beforeImageUpload(file.raw)){
    return false;
  }
  uploadLoading.value = true
  try {
    // 调用 store 中的上传方法
    const response = await roomStore.uploadImage(file.raw)
    
    if (response.code === 200 || response.success) {
      // 根据后端返回的数据结构调整
      // 假设返回数据结构: { code: 200, data: { url: '...' }, message: '上传成功' }
      const uploadedImageUrl = response.data?.url || response.data || response.url
      
      if (uploadedImageUrl) {
        // 更新表单和预览
        form.value.image = uploadedImageUrl
        imageUrl.value = uploadedImageUrl
        
        ElMessage.success(response.message || '图片上传成功')
      } else {
        ElMessage.error('上传成功但未返回图片URL')
      }
    } else {
      ElMessage.error(response.message || '图片上传失败')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败: ' + (error.message || '网络错误'))
  } finally {
    uploadLoading.value = false
  }
  
  return false // 阻止自动上传
}

// 图片删除方法
const handleImageRemove = () => {
  form.value.image = ''
  imageUrl.value = ''
  ElMessage.info('已移除图片')
}

// 图片预览方法
const handleImagePreview = () => {
  if (imageUrl.value) {
    previewImage.value = imageUrl.value
    previewDialogVisible.value = true
  } else {
    ElMessage.warning('请先上传图片')
  }
}
// 复制图片URL到剪贴板
const copyImageUrl = () => {
  if (!form.value.image) {
    ElMessage.warning('没有图片URL可复制')
    return
  }
  
  // 使用现代API
  if (navigator.clipboard) {
    navigator.clipboard.writeText(form.value.image)
      .then(() => {
        ElMessage.success('图片URL已复制到剪贴板')
      })
      .catch(() => {
        // 备用方法
        copyToClipboardFallback(form.value.image)
      })
  } else {
    // 备用方法
    copyToClipboardFallback(form.value.image)
  }
}
// 获取完整图片URL的方法
const getFullImageUrl = (imagePath) => {
  if (!imagePath) return ''
  console.log('原始图片路径:', imagePath)
  // 如果已经是完整的URL，直接返回
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
    return imagePath
  }
  
  // 如果是以 / 开头，表示是绝对路径
  if (imagePath.startsWith('/')) {
    // 根据您的后端地址拼接完整URL
    // 您需要根据实际情况调整 baseURL
    const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
    const fullUrl = `${baseURL}${imagePath}`
    console.log('拼接后的完整URL:', fullUrl)
    return fullUrl
  }
  
  // 其他情况直接返回
  return imagePath
}


// 计算当前页数据
const currentPageData = computed(() => {
  const start = (pagination.value.currentPage - 1) * pagination.value.pageSize
  const end = start + pagination.value.pageSize
  return rooms.value.slice(start, end)
})

// 获取自习室列表
const getRooms = async () => {
  loading.value = true
  try {
    await roomStore.getRooms()
    // rooms.value = roomStore.rooms
    // room.value.image = getFullImageUrl(room.value.image)
    rooms.value = roomStore.rooms.map(room => ({
      ...room,
      fullImageUrl: getFullImageUrl(room.image)
    }))
    pagination.value.total = roomStore.rooms.length
  } catch (error) {
    console.error('获取自习室列表失败:', error)
    ElMessage.error('获取自习室列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.currentPage = 1
  // 这里应该调用带搜索条件的API
  // 暂时前端过滤
  if (searchKeyword.value) {
    rooms.value = roomStore.rooms.filter(room => 
      room.name.includes(searchKeyword.value) || 
      room.location.includes(searchKeyword.value)
    )
    pagination.value.total = rooms.value.length
  } else {
    rooms.value = roomStore.rooms
    pagination.value.total = roomStore.rooms.length
  }
}

// 重置搜索
const handleReset = () => {
  searchKeyword.value = ''
  getRooms()
}

// 分页变化
const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.currentPage = 1
}

const handleCurrentChange = (current) => {
  pagination.value.currentPage = current
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
    openTime: '08:00:00',
    closeTime: '22:00:00',
    status: 'available',
    image: ''
  }
  imageUrl.value = ''
  dialogVisible.value = true
  
  // 重置表单验证
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 打开编辑对话框
const openEditDialog = (room) => {
  dialogTitle.value = '编辑自习室'
  form.value = {
    id: room.id,
    name: room.name,
    location: room.location,
    rowsCount: room.rows_count || room.rowsCount || 5,
    colsCount: room.cols_count || room.colsCount || 8,
    openTime: room.open_time || room.openTime || '08:00:00',
    closeTime: room.close_time || room.closeTime || '22:00:00',
    status: room.status,
    image: room.image || ''
  }
  // 设置图片预览
  if (room.image) {
    // 如果图片是完整URL，直接使用；如果是相对路径，可能需要拼接基础URL
    imageUrl.value = getFullImageUrl(room.image)
  } else {
    imageUrl.value = ''
  }
  
  dialogVisible.value = true
  // 重置表单验证
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 准备提交数据，排除ID字段
    const formData = {
      name: form.value.name,
      location: form.value.location,
      rowsCount: form.value.rowsCount,
      colsCount: form.value.colsCount,
      openTime: form.value.openTime,
      closeTime: form.value.closeTime,
      status: form.value.status,
      image: form.value.image,
      rows_count: form.value.rowsCount,
      cols_count: form.value.colsCount,
      open_time: form.value.openTime,
      close_time: form.value.closeTime
    }
    
    if (form.value.id) {
      // 编辑，包含ID字段
      await roomStore.updateRoom({
        ...formData,
        id: form.value.id
      })
      ElMessage.success('编辑成功')
    } else {
      // 添加，不包含ID字段
      await roomStore.createRoom(formData)
      ElMessage.success('添加成功')
    }
    
    dialogVisible.value = false
    getRooms()
  } catch (error) {
    if (error instanceof Error) {
      ElMessage.error('表单验证失败: ' + error.message)
    } else {
      ElMessage.error('操作失败')
    }
  }
}

// 删除自习室
const deleteRoom = (room) => {
  ElMessageBox.confirm('确定要删除这个自习室吗？删除后座位数据也会被删除！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger'
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
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch"></el-button>
          </template>
        </el-input>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        <el-button :icon="Refresh" @click="refreshList">刷新</el-button>
      </div>
      
      <!-- 自习室列表 -->
      <el-table
        v-loading="loading"
        :data="currentPageData"
        style="width: 100%"
        border
        stripe
        empty-text="暂无数据"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="自习室名称" min-width="150" />
        <el-table-column prop="location" label="位置" min-width="150" />
        <el-table-column label="布局" width="120" align="center">
          <template #default="scope">
            {{ scope.row.rows_count || scope.row.rowsCount }}行 × {{ scope.row.cols_count || scope.row.colsCount }}列
          </template>
        </el-table-column>
        <el-table-column label="开放时间" width="180" align="center">
          <template #default="scope">
            {{ (scope.row.open_time || scope.row.openTime)?.substring(0, 5) }} ~ {{ (scope.row.close_time || scope.row.closeTime)?.substring(0, 5) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'available' || scope.row.status === 'open'" type="success">可用</el-tag>
            <el-tag v-else type="danger">不可用</el-tag>
          </template>
        </el-table-column>
          <el-table-column label="图片" width="100" align="center">
          <template #default="scope">
            <div v-if="scope.row.image" class="room-image-preview">
              <el-image
                :src="getFullImageUrl(scope.row.image)"
                :preview-src-list="[getFullImageUrl(scope.row.image)]"
                fit="cover"
                style="width: 60px; height: 40px; border-radius: 4px;"
                preview-teleported
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="image-tooltip" @click="previewImage = getFullImageUrl(scope.row.image); previewDialogVisible = true">
                <el-icon size="16"><Picture /></el-icon>
                <span>预览</span>
              </div>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
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
          :page-sizes="[5, 10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="自习室名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入自习室名称" />
        </el-form-item>
        <el-form-item label="自习室位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入自习室位置，如：主楼301" />
        </el-form-item>
        <el-form-item label="行数" prop="rowsCount">
          <el-input-number 
            v-model="form.rowsCount" 
            :min="1" 
            :max="20" 
            :step="1"
            controls-position="right"
          />
          <span class="form-tip">座位布局的行数</span>
        </el-form-item>
        <el-form-item label="列数" prop="colsCount">
          <el-input-number 
            v-model="form.colsCount" 
            :min="1" 
            :max="20" 
            :step="1"
            controls-position="right"
          />
          <span class="form-tip">座位布局的列数</span>
        </el-form-item>
        <el-form-item label="开放时间" prop="openTime">
          <el-time-select
            v-model="form.openTime"
            placeholder="选择开放时间"
            :start="'00:00'"
            :step="'00:30'"
            :end="'23:30'"
            format="HH:mm"
          />
        </el-form-item>
        <el-form-item label="关闭时间" prop="closeTime">
          <el-time-select
            v-model="form.closeTime"
            placeholder="选择关闭时间"
            :start="'00:00'"
            :step="'00:30'"
            :end="'23:30'"
            format="HH:mm"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="选择状态">
            <el-option label="可用" value="available" />
            <el-option label="不可用" value="unavailable" />
            <el-option label="维护中" value="maintenance" />
          </el-select>
        </el-form-item>
        <el-form-item label="自习室图片">
          <el-upload
            class="image-uploader"
            :show-file-list="false"
            :on-change="handleImageUpload"
            :before-upload="() => false"
            :loading="uploadLoading"
            accept="image/*"
            :disabled="uploadLoading"
          >
            <div v-if="imageUrl" class="image-preview">
              <img :src="imageUrl" alt="自习室图片" />
              <div class="image-overlay">
                <el-icon><Picture /></el-icon>
                <span>点击更换</span>
              </div>
              <div v-if="uploadLoading" class="uploading-overlay">
                <el-icon class="loading-icon">
                  <Loading />
                </el-icon>
                <span>上传中...</span>
              </div>
            </div>
            <el-button
              v-else 
              type="primary" 
              :icon="Upload" 
              :loading="uploadLoading"
              :disabled="uploadLoading">
              {{ uploadLoading ? '上传中...' : '上传图片' }}
            </el-button>
          </el-upload>
          <div v-if="imageUrl" class="image-actions">
            <el-button size="small" type="info" @click="handleImagePreview">
              预览
            </el-button>
            <el-button size="small" type="danger" @click="handleImageRemove">
              删除
            </el-button>
            <el-button size="small" type="success" @click="copyImageUrl" :disabled="uploadLoading">
              复制URL
            </el-button>
          </div>
          <div class="upload-tip">
            支持 JPG、PNG 格式，大小不超过 2MB
          </div>
          <div v-if="form.image" class="image-url">
            <el-input v-model="form.image" placeholder="图片URL" readonly>
              <template #append>
                <el-button :icon="Picture" @click="copyImageUrl" />
              </template>
            </el-input>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="roomStore.loading">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewDialogVisible" title="图片预览" width="600px" :close-on-click-modal="false">
      <div class="preview-container">
        <img v-if="previewImage" :src="previewImage" alt="预览" />
        <div v-else class="no-image">暂无图片</div>
      </div>
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
    
    .el-input {
      flex: 1;
      max-width: 400px;
    }
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .form-tip {
    margin-left: 10px;
    color: #909399;
    font-size: 12px;
  }
  
  .upload-tip {
    margin-top: 8px;
    color: #909399;
    font-size: 12px;
  }
}

/* 图片上传样式 */
.image-uploader {
  .el-button {
    width: 150px;
  }
  
  .image-preview {
    position: relative;
    width: 150px;
    height: 100px;
    border: 1px dashed #dcdfe6;
    border-radius: 4px;
    overflow: hidden;
    cursor: pointer;
    &:hover {
      .image-overlay {
        opacity: 1;
      }
    }
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .image-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      color: white;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s;
      
      
      .el-icon {
        font-size: 24px;
        margin-bottom: 5px;
      }
      
      span {
        font-size: 12px;
      }
    }
  }
  .image-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      color: white;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s;
      
      .el-icon {
        font-size: 24px;
        margin-bottom: 5px;
      }
      
      span {
        font-size: 12px;
      }
    }
  .uploading-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(255, 255, 255, 0.9);
      color: #666;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      
      .loading-icon {
        font-size: 24px;
        margin-bottom: 8px;
        animation: rotating 2s linear infinite;
      }
      
      span {
        font-size: 12px;
      }
    }
  
}
@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
.image-actions {
  margin-top: 10px;
  display: flex;
  gap: 10px;
    .el-button {
    padding: 6px 8px;
    font-size: 12px;
  }
}

.image-url {
  margin-top: 10px;
  
  .el-input {
    input {
      cursor: default;
      font-size: 12px;
      color: #666;
    }
  }
}
.upload-tip {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}
.preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  
  img {
    max-width: 100%;
    max-height: 500px;
    object-fit: contain;
  }
  
  .no-image {
    color: #909399;
    font-size: 14px;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-bar {
    flex-direction: column;
    align-items: stretch;
    
    .el-input {
      max-width: 100% !important;
      margin-bottom: 10px;
    }
  }
  
  .el-dialog {
    width: 90% !important;
    max-width: 400px;
  }
  
  .image-uploader {
    .el-button,
    .image-preview {
      width: 100%;
    }
  }
  .image-actions {
    flex-wrap: wrap;
  }
}
</style>