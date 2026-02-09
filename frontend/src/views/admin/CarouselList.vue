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
  Picture,
  Loading
} from '@element-plus/icons-vue'
import service from '../../services/axios'

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

// 轮播图列表
const carousels = ref([])

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('添加轮播图')

// 表单数据
const form = ref({
  id: '',
  name: '',
  path: '',
  status: 'active'
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入轮播图名称', trigger: 'blur' }
  ],
  path: [
    { required: true, message: '请上传轮播图', trigger: 'blur' }
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
    // 创建FormData对象
    const formData = new FormData()
    formData.append('file', file.raw)
    
    // 调用后端API上传图片
    const response = await service({
      url: '/carousel/upload/image',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (response.code === 200 || response.success) {
      // 根据后端返回的数据结构调整
      const uploadedImageUrl = response.data
      
      if (uploadedImageUrl) {
        // 更新表单和预览
        form.value.path = uploadedImageUrl
        // 为预览设置完整的URL
        imageUrl.value = getFullImageUrl(uploadedImageUrl)
        
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
  form.value.path = ''
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
  if (!form.value.path) {
    ElMessage.warning('没有图片URL可复制')
    return
  }
  
  // 使用现代API
  if (navigator.clipboard) {
    navigator.clipboard.writeText(form.value.path)
      .then(() => {
        ElMessage.success('图片URL已复制到剪贴板')
      })
      .catch(() => {
        // 备用方法
        copyToClipboardFallback(form.value.path)
      })
  } else {
    // 备用方法
    copyToClipboardFallback(form.value.path)
  }
}

// 备用复制方法
const copyToClipboardFallback = (text) => {
  const textArea = document.createElement('textarea')
  textArea.value = text
  document.body.appendChild(textArea)
  textArea.select()
  try {
    document.execCommand('copy')
    ElMessage.success('图片URL已复制到剪贴板')
  } catch (err) {
    ElMessage.error('复制失败，请手动复制')
  } finally {
    document.body.removeChild(textArea)
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
    // 根据您的后端地址拼接完整URL，包含/api前缀
    const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
    const fullUrl = `${baseURL}/api${imagePath}`
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
  return carousels.value.slice(start, end)
})

// 获取轮播图列表
const getCarousels = async () => {
  loading.value = true
  try {
    const response = await service({
      url: '/carousel/list',
      method: 'get'
    })
    
    if (response.code === 200 || response.success) {
      carousels.value = response.data
      pagination.value.total = carousels.value.length
    } else {
      ElMessage.error('获取轮播图列表失败')
    }
  } catch (error) {
    console.error('获取轮播图列表失败:', error)
    ElMessage.error('获取轮播图列表失败')
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
    carousels.value = carousels.value.filter(carousel => 
      carousel.name.includes(searchKeyword.value)
    )
    pagination.value.total = carousels.value.length
  } else {
    getCarousels()
  }
}

// 重置搜索
const handleReset = () => {
  searchKeyword.value = ''
  getCarousels()
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
  dialogTitle.value = '添加轮播图'
  form.value = {
    id: '',
    name: '',
    path: '',
    status: 'active'
  }
  imageUrl.value = ''
  dialogVisible.value = true
  
  // 重置表单验证
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 打开编辑对话框
const openEditDialog = (carousel) => {
  dialogTitle.value = '编辑轮播图'
  form.value = {
    id: carousel.id,
    name: carousel.name,
    path: carousel.path || '',
    status: carousel.status
  }
  // 设置图片预览
  if (carousel.path) {
    // 如果图片是完整URL，直接使用；如果是相对路径，可能需要拼接基础URL
    imageUrl.value = getFullImageUrl(carousel.path)
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
    
    // 准备提交数据
    const formData = {
      name: form.value.name,
      path: form.value.path,
      status: form.value.status
    }
    
    if (form.value.id) {
      // 编辑
      const response = await service({
        url: `/carousel/update/${form.value.id}`,
        method: 'put',
        data: formData
      })
      
      if (response.code === 200 || response.success) {
        ElMessage.success('编辑成功')
      } else {
        ElMessage.error('编辑失败：' + (response.message || '未知错误'))
      }
    } else {
      // 添加
      const response = await service({
        url: '/carousel/create',
        method: 'post',
        data: formData
      })
      
      if (response.code === 200 || response.success) {
        ElMessage.success('添加成功')
      } else {
        ElMessage.error('添加失败：' + (response.message || '未知错误'))
      }
    }
    
    dialogVisible.value = false
    getCarousels()
  } catch (error) {
    if (error instanceof Error) {
      ElMessage.error('表单验证失败: ' + error.message)
    } else {
      ElMessage.error('操作失败')
    }
  }
}

// 删除轮播图
const deleteCarousel = (carousel) => {
  ElMessageBox.confirm('确定要删除这个轮播图吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    try {
      const response = await service({
        url: `/carousel/delete/${carousel.id}`,
        method: 'delete'
      })
      
      if (response.code === 200 || response.success) {
        ElMessage.success('删除成功')
        getCarousels()
      } else {
        ElMessage.error('删除失败：' + (response.message || '未知错误'))
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 刷新列表
const refreshList = () => {
  getCarousels()
}

// 初始化
onMounted(() => {
  getCarousels()
})
</script>

<template>
  <div class="admin-carousel-list">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>轮播图管理</span>
          <el-button type="primary" :icon="Plus" @click="openAddDialog">添加轮播图</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索轮播图名称"
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
      
      <!-- 轮播图列表 -->
      <el-table
        v-loading="loading"
        :data="currentPageData"
        style="width: 100%"
        border
        stripe
        empty-text="暂无数据"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="轮播图名称" min-width="150" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'active'" type="success">启用</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="轮播图" width="150" align="center">
          <template #default="scope">
            <div v-if="scope.row.path" class="carousel-image-preview">
              <el-image
                :src="getFullImageUrl(scope.row.path)"
                :preview-src-list="[getFullImageUrl(scope.row.path)]"
                fit="cover"
                style="width: 120px; height: 60px; border-radius: 4px;"
                preview-teleported
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" :icon="Edit" @click="openEditDialog(scope.row)"></el-button>
            <el-button type="danger" size="small" :icon="Delete" @click="deleteCarousel(scope.row)"></el-button>
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
        <el-form-item label="轮播图名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入轮播图名称" />
        </el-form-item>
        <el-form-item label="轮播图" prop="path">
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
              <img :src="imageUrl" alt="轮播图" />
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
          <div v-if="form.path" class="image-url">
            <el-input v-model="form.path" placeholder="图片URL" readonly>
              <template #append>
                <el-button :icon="Picture" @click="copyImageUrl" />
              </template>
            </el-input>
          </div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="选择状态">
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">
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
.admin-carousel-list {
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
