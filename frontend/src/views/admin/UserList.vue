<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Refresh, Download, Upload } from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()

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

// 对话框状态
const dialogVisible = ref(false)
const dialogTitle = ref('添加用户')

// 表单数据
const form = ref({
  id: '',
  username: '',
  password: '',
  email: '',
  phone: '',
  gender: '',
  status: '1'
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 5, message: '密码长度不能小于5位', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  phone: [
    { pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

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
    // 这里可以调用后端API获取用户列表
    // 暂时使用模拟数据
    userList.value = [
      {
        id: 1,
        username: 'user1',
        email: 'user1@example.com',
        phone: '13800138001',
        gender: 'male',
        status: '1',
        createdAt: '2026-02-01 12:00:00'
      },
      {
        id: 2,
        username: 'user2',
        email: 'user2@example.com',
        phone: '13800138002',
        gender: 'female',
        status: '1',
        createdAt: '2026-02-02 12:00:00'
      }
    ]
    pagination.value.total = userList.value.length
  } catch (error) {
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
    email: '',
    phone: '',
    gender: '',
    status: '1'
  }
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (user) => {
  dialogTitle.value = '编辑用户'
  form.value = {
    id: user.id,
    username: user.username,
    password: '',
    email: user.email,
    phone: user.phone,
    gender: user.gender,
    status: user.status
  }
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (formRef.value) {
    formRef.value.validate(async (valid) => {
      if (valid) {
        try {
          // 这里可以调用后端API更新用户
          ElMessage.success(dialogTitle.value === '添加用户' ? '添加成功' : '编辑成功')
          dialogVisible.value = false
          getUsers()
        } catch (error) {
          ElMessage.error('操作失败')
        }
      }
    })
  }
}

// 删除用户
const handleDelete = (user) => {
  const ids = user ? [user.id] : multipleSelection.value.map(item => item.id)
  ElMessageBox.confirm(`确定要删除选中的${ids.length}个用户吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 这里可以调用后端API删除用户
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
            <el-input v-model="queryParams.userName" placeholder="请输入用户名称" clearable @keyup.enter.native="handleQuery" style="width: 240px" />
          </el-form-item>
          <el-form-item label="手机号码" prop="phonenumber">
            <el-input v-model="queryParams.phonenumber" placeholder="请输入手机号码" clearable @keyup.enter.native="handleQuery" style="width: 240px" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="用户状态" clearable style="width: 120px">
              <el-option label="启用" value="1" />
              <el-option label="禁用" value="0" />
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
        <el-button type="primary" plain :icon="Plus" :disabled="single" @click="openEditDialog(multipleSelection[0])">修改</el-button>
        <el-button type="danger" plain :icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>
        <el-button type="info" plain :icon="Upload" @click="handleImport">导入</el-button>
        <el-button type="warning" plain :icon="Download" @click="handleExport">导出</el-button>
        <el-button :icon="Refresh" @click="refreshList">刷新</el-button>
      </div>
      
      <!-- 用户列表 -->
      <el-table
        v-loading="loading"
        :data="userList"
        style="width: 100%"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="id" label="用户编号" width="100" />
        <el-table-column prop="username" label="用户名称" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号码" width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="scope">
            {{ scope.row.gender === 'male' ? '男' : scope.row.gender === 'female' ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-switch v-model="scope.row.status" :active-value="'1'" :inactive-value="'0'" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" :icon="Edit" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" :icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="用户名称" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名称" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户密码" prop="password" v-if="!form.id">
              <el-input type="password" v-model="form.password" placeholder="请输入用户密码" maxlength="20" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号码" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号码" maxlength="11" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="用户性别">
              <el-select v-model="form.gender" placeholder="请选择性别">
                <el-option label="男" value="male" />
                <el-option label="女" value="female" />
                <el-option label="未知" value="" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio label="1">启用</el-radio>
                <el-radio label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
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