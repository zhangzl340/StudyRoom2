<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox, roleTypes } from 'element-plus'
import { Search, Plus, Edit, Delete, Refresh, Download, Upload, User } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

// 加载状态
const loading = ref(true)

// 搜索参数
const queryParams = ref({
  userName: '',
  phonenumber: '',
  status: '',
  pageNum: 1,
  pageSize: 10
})

// 分页参数
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 用户列表
const userList = ref([])

// 计算当前页数据
const currentPageData = computed(() => {
  const start = (pagination.value.currentPage - 1) * pagination.value.pageSize
  const end = start + pagination.value.pageSize
  return userList.value.slice(start, end)
})

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('添加用户')

// 表单数据
const form = ref({
  id: '',
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  gender: '',
  status: 'active',
  role:'student'
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在2-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'blur' }
  ],
  role:[
    { required: true, message: '请选择角色', trigger: 'blur' }
  ]
}

// 角色选项(中英文映射)
const roleOptions= [
  {value:'student',label:'学生'},
  {value:'admin',label:'管理员'},
  {value:'system_admin',label:'系统管理员'}
]

// 表单引用
const formRef = ref(null)

// 复选框选中状态
const multipleSelection = ref([])
const single = ref(true)
const multiple = ref(true)

// 日期范围
const dateRange = ref([])

// 获取用户列表
const getUsers = async () => {
  loading.value = true
  try {
    // 调用后端API获取用户列表
    await userStore.getUsers()
    userList.value = userStore.users
    pagination.value.total = userStore.users.length
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getUsers()
}

// 重置搜索
const handleReset = () => {
  queryParams.value = {
    userName: '',
    phonenumber: '',
    status: '',
    pageNum: 1,
    pageSize: 10
  }
  dateRange.value = []
  getUsers()
}

// 分页变化
const handleSizeChange = (size) => {
  queryParams.value.pageSize = size
  getUsers()
}

const handleCurrentChange = (current) => {
  queryParams.value.pageNum = current
  getUsers()
}

// 复选框选中变化
const handleSelectionChange = (val) => {
  multipleSelection.value = val
  single.value = val.length !== 1
  multiple.value = !val.length
}

// 打开添加对话框
const openAddDialog = () => {
  dialogTitle.value = '添加用户'
  form.value = {
    id: '',
    username: '',
    password: '',
    realName: '',
    email: '',
    phone: '',
    gender: '',
    status: 'active',
    role:'student' // 默认角色
  }
  dialogVisible.value = true
  
  // 重置表单验证
  nextTick(() => {
    if (formRef.value) {
      formRef.value.clearValidate()
    }
  })
}

// 打开编辑对话框
const openEditDialog = (user) => {
  dialogTitle.value = '编辑用户'
  form.value = {
    id: user.id,
    username: user.username,
    password: '', // 编辑时不修改密码
    realName: user.real_name || user.realName || '',
    email: user.email || '',
    phone: user.phone || '',
    gender: user.gender || '',
    status: user.status || 'active',
    role:user.role || 'student'   //角色字段
  }
  dialogVisible.value = true
  
  // 重置表单验证
  nextTick(() => {
    if (formRef.value) {
      formRef.value.clearValidate()
    }
  })
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 准备提交数据
    const userData = {
      username: form.value.username,
      realName: form.value.realName,
      email: form.value.email,
      phone: form.value.phone,
      gender: form.value.gender,
      status: form.value.status,
      role: form.value.role
    }
    
    // 如果是添加用户，需要密码；编辑用户不需要密码
    if (!form.value.id && form.value.password) {
      userData.password = form.value.password
    }
    
    if (form.value.id) {
      // 编辑用户
      await userStore.updateUser(form.value.id, userData)
      ElMessage.success('编辑成功')
    } else {
      // 添加用户
      await userStore.createUser(userData)
      ElMessage.success('添加成功')
    }
    
    dialogVisible.value = false
    getUsers()
  } catch (error) {
    if (error instanceof Error) {
      ElMessage.error('表单验证失败: ' + error.message)
    } else {
      ElMessage.error('操作失败')
    }
  }
}

// 删除用户
const handleDelete = (user) => {
  const ids = user ? [user.id] : multipleSelection.value.map(item => item.id)
  if (ids.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }
  
  ElMessageBox.confirm(`确定要删除选中的${ids.length}个用户吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    try {
      for (const id of ids) {
        await userStore.deleteUser(id)
      }
      ElMessage.success('删除成功')
      getUsers()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 导出用户
const handleExport = () => {
  // 这里可以调用后端API导出用户
  ElMessage.success('导出成功')
}

// 导入用户
const handleImport = () => {
  // 这里可以调用后端API导入用户
  ElMessage.success('导入成功')
}

// 刷新列表
const refreshList = () => {
  getUsers()
}

// 初始化
onMounted(async () => {
  await getUsers()
})


// 角色标签类型映射
const getRoleTagType = (role) => {
  const typeMap = {
    'admin': 'danger',       // 管理员 - 红色
    'system_admin': 'warning', // 系统管理员 - 橙色
    'student': 'success'     // 学生 - 绿色
  }
  return typeMap[role] || 'info'
}

// 获取角色显示标签
const getRoleLabel = (role) => {
  const option = roleOptions.find(opt => opt.value === role)
  return option ? option.label : '未知'
}

// 时间格式化函数
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  
  try {
    const date = new Date(dateTime)
    
    // 方法1: 使用原生Date方法
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
  } catch (error) {
    console.error('时间格式化错误:', error)
    return '-'
  }
}


</script>

<template>
  <div class="admin-user-list">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" :icon="Plus" @click="openAddDialog">添加用户</el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true">
          <el-form-item label="用户名称" prop="userName">
            <el-input v-model="queryParams.userName" placeholder="请输入用户名称" clearable @keyup.enter="handleQuery" style="width: 240px" />
          </el-form-item>
          <el-form-item label="手机号码" prop="phonenumber">
            <el-input v-model="queryParams.phonenumber" placeholder="请输入手机号码" clearable @keyup.enter="handleQuery" style="width: 240px" />
          </el-form-item>
          <!-- 添加角色搜索 -->
          <el-form-item label="角色" prop="role">
            <el-select v-model="queryParams.role" placeholder="用户角色" clearable style="width: 120px">
              <el-option label="全部" value="" />
              <el-option v-for="option in roleOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="用户状态" clearable style="width: 120px">
              <el-option label="启用" value="active" />
              <el-option label="禁用" value="inactive" />
            </el-select>
          </el-form-item>
          <el-form-item label="创建时间">
            <el-date-picker v-model="dateRange" style="width: 240px" value-format="yyyy-MM-dd" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 操作栏 -->
      <div class="action-bar">
        <el-button type="primary" plain :icon="Edit" :disabled="single" @click="openEditDialog(multipleSelection[0])">修改</el-button>
        <el-button type="danger" plain :icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>
        <el-button type="info" plain :icon="Upload" @click="handleImport">导入</el-button>
        <el-button type="warning" plain :icon="Download" @click="handleExport">导出</el-button>
        <el-button :icon="Refresh" @click="refreshList">刷新</el-button>
      </div>
      
      <!-- 用户列表 -->
      <el-table
        v-loading="loading"
        :data="currentPageData"
        style="width: 100%"
        border
        stripe
        empty-text="暂无数据"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="id" label="用户编号" width="100" />
        <el-table-column prop="username" label="用户名称" show-overflow-tooltip min-width="120" />
        <el-table-column prop="realName" label="真实姓名" show-overflow-tooltip min-width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.role)">
              {{ getRoleLabel(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号码" width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="scope">
            {{ scope.row.gender === 'male' ? '男' : scope.row.gender === 'female' ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-switch 
              v-model="scope.row.status" 
              :active-value="'active'" 
              :inactive-value="'inactive'"
              @change="async (val) => {
                try {
                  await userStore.updateUser(scope.row.id, { status: val })
                  ElMessage.success('状态更新成功')
                } catch (error) {
                  ElMessage.error('状态更新失败')
                }
              }"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button type="primary" size="small" :icon="Edit" @click="openEditDialog(scope.row)">
                编辑
              </el-button>
              <el-button type="danger" size="small" :icon="Delete" @click="handleDelete(scope.row)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px"
        label-position="right"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="!form.id" label="密码" prop="password">
              <el-input type="password" v-model="form.password" placeholder="请输入密码" maxlength="20" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号码" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号码" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男" value="male" />
                <el-option label="女" value="female" />
                <el-option label="未知" value="" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色" prop="role">
              <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
                <el-option 
                  v-for="option in roleOptions" 
                  :key="option.value" 
                  :label="option.label" 
                  :value="option.value" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="active">启用</el-radio>
                <el-radio label="inactive">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="userStore.loading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.admin-user-list {
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

  .action-buttons {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    
    .el-button {
      // 确保按钮高度一致
      height: 32px;
      line-height: 32px;
      padding: 0 12px;
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
    
    .el-button {
      margin-bottom: 10px;
      width: 100%;
    }
  }

  .el-table {
    .el-table__header-wrapper,
    .el-table__body-wrapper {
      overflow-x: auto;
    }
  }

  .action-buttons {
    flex-direction: column;
    align-items: stretch;
    gap: 5px;
    
    .el-button {
      width: 100%;
      margin: 0;
    }
  }

  .pagination {
    justify-content: center;
  }
  
  .el-dialog {
    width: 90% !important;
    max-width: 400px;
  }
}
</style>