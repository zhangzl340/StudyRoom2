<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Plus, 
  Edit, 
  Delete, 
  Refresh 
} from '@element-plus/icons-vue'
import * as announcementService from '../../services/announcement'

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

// 公告列表
const announcements = ref([])

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('添加公告')

// 表单数据
const form = ref({
  id: '',
  title: '',
  content: '',
  type: 'announcement',
  publishTime: '',
  publisher: 'admin',
  isActive: true
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择公告类型', trigger: 'blur' }
  ],
  publishTime: [
    { required: true, message: '请选择发布时间', trigger: 'blur' }
  ],
  publisher: [
    { required: true, message: '请输入发布人', trigger: 'blur' }
  ]
}

// 表单引用
const formRef = ref(null)

// 计算当前页数据
const currentPageData = computed(() => {
  const start = (pagination.value.currentPage - 1) * pagination.value.pageSize
  const end = start + pagination.value.pageSize
  return announcements.value.slice(start, end)
})

// 获取公告列表
const getAnnouncements = async () => {
  loading.value = true
  try {
    const response = await announcementService.getAnnouncementList()
    if (response.code === 200 || response.success) {
      announcements.value = response.data
      pagination.value.total = announcements.value.length
    } else {
      ElMessage.error('获取公告列表失败')
    }
  } catch (error) {
    console.error('获取公告列表失败:', error)
    ElMessage.error('获取公告列表失败')
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
    announcements.value = announcements.value.filter(announcement => 
      announcement.title.includes(searchKeyword.value) || 
      announcement.content.includes(searchKeyword.value) ||
      announcement.type.includes(searchKeyword.value)
    )
    pagination.value.total = announcements.value.length
  } else {
    getAnnouncements()
  }
}

// 重置搜索
const handleReset = () => {
  searchKeyword.value = ''
  getAnnouncements()
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
  dialogTitle.value = '添加公告'
  form.value = {
    id: '',
    title: '',
    content: '',
    type: 'announcement',
    publishTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
    publisher: 'admin',
    isActive: true
  }
  dialogVisible.value = true
  
  // 重置表单验证
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 打开编辑对话框
const openEditDialog = (announcement) => {
  dialogTitle.value = '编辑公告'
  form.value = {
    id: announcement.id,
    title: announcement.title,
    content: announcement.content,
    type: announcement.type,
    publishTime: announcement.publishTime || new Date().toISOString().slice(0, 19).replace('T', ' '),
    publisher: announcement.publisher || 'admin',
    isActive: announcement.isActive
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
      title: form.value.title,
      content: form.value.content,
      type: form.value.type,
      publishTime: form.value.publishTime,
      publisher: form.value.publisher,
      isActive: form.value.isActive
    }
    
    if (form.value.id) {
      // 编辑
      await announcementService.updateAnnouncement(form.value.id, formData)
      ElMessage.success('编辑成功')
    } else {
      // 添加
      await announcementService.createAnnouncement(formData)
      ElMessage.success('添加成功')
    }
    
    dialogVisible.value = false
    getAnnouncements()
  } catch (error) {
    if (error instanceof Error) {
      ElMessage.error('表单验证失败: ' + error.message)
    } else {
      ElMessage.error('操作失败')
    }
  }
}

// 删除公告
const deleteAnnouncement = (announcement) => {
  ElMessageBox.confirm('确定要删除这个公告吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    try {
      await announcementService.deleteAnnouncement(announcement.id)
      ElMessage.success('删除成功')
      getAnnouncements()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 更新公告状态
const updateAnnouncementStatus = async (announcement, isActive) => {
  try {
    await announcementService.updateAnnouncementStatus(announcement.id, isActive)
    ElMessage.success('状态更新成功')
    getAnnouncements()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

// 刷新列表
const refreshList = () => {
  getAnnouncements()
}

// 初始化
onMounted(() => {
  getAnnouncements()
})
</script>

<template>
  <div class="admin-announcement-list">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>公告管理</span>
          <el-button type="primary" :icon="Plus" @click="openAddDialog">添加公告</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索公告标题、内容或类型"
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
      
      <!-- 公告列表 -->
      <el-table
        v-loading="loading"
        :data="currentPageData"
        style="width: 100%"
        border
        stripe
        empty-text="暂无数据"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="公告标题" min-width="200" />
        <el-table-column prop="type" label="类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.type === 'announcement' ? 'info' : 'warning'">
              {{ scope.row.type === 'announcement' ? '公告' : '推广' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180" align="center">
          <template #default="scope">
            {{ scope.row.publishTime ? scope.row.publishTime.substring(0, 19) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="publisher" label="发布人" width="120" align="center" />
        <el-table-column prop="isActive" label="状态" width="100" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.isActive"
              :active-text="'活跃'"
              :inactive-text="'非活跃'"
              @change="updateAnnouncementStatus(scope.row, scope.row.isActive)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" :icon="Edit" @click="openEditDialog(scope.row)"></el-button>
            <el-button type="danger" size="small" :icon="Delete" @click="deleteAnnouncement(scope.row)"></el-button>
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
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入公告内容" 
          />
        </el-form-item>
        <el-form-item label="公告类型" prop="type">
          <el-select v-model="form.type" placeholder="选择公告类型">
            <el-option label="公告" value="announcement" />
            <el-option label="推广" value="promotion" />
          </el-select>
        </el-form-item>
        <el-form-item label="发布时间" prop="publishTime">
          <el-date-picker 
            v-model="form.publishTime" 
            type="datetime" 
            placeholder="选择发布时间" 
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="发布人" prop="publisher">
          <el-input v-model="form.publisher" placeholder="请输入发布人" />
        </el-form-item>
        <el-form-item label="状态" prop="isActive">
          <el-switch v-model="form.isActive" />
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
.admin-announcement-list {
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
}
</style>
