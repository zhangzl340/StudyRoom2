<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Refresh } from '@element-plus/icons-vue'

// 学院列表数据
const colleges = ref([])
// 加载状态
const loading = ref(true)
// 搜索关键词
const searchKeyword = ref('')
// 对话框状态
const dialogVisible = ref(false)
// 编辑模式
const editMode = ref(false)
// 当前编辑的学院
const currentCollege = ref({})

// 获取学院列表
const getColleges = async () => {
  try {
    loading.value = true
    const response = await fetch('/api/college/list')
    const data = await response.json()
    
    if (data.success) {
      colleges.value = data.data
    } else {
      throw new Error(data.message || '获取学院列表失败')
    }
  } catch (error) {
    console.error('获取学院列表失败:', error)
    ElMessage.error('获取学院列表失败')
  } finally {
    loading.value = false
  }
}

// 打开新增对话框
const openAddDialog = () => {
  editMode.value = false
  currentCollege.value = {
    name: '',
    color: '#409eff'
  }
  dialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (college) => {
  editMode.value = true
  currentCollege.value = { ...college }
  dialogVisible.value = true
}

// 保存学院
const saveCollege = async () => {
  try {
    if (!currentCollege.value.name || currentCollege.value.name.trim() === '') {
      ElMessage.warning('学院名称不能为空')
      return
    }
    
    const url = editMode.value 
      ? `/api/college/update/${currentCollege.value.id}`
      : '/api/college/create'
    
    const response = await fetch(url, {
      method: editMode.value ? 'PUT' : 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(currentCollege.value)
    })
    
    const data = await response.json()
    if (data.success) {
      ElMessage.success(editMode.value ? '学院更新成功' : '学院创建成功')
      dialogVisible.value = false
      await getColleges()
    } else {
      throw new Error(data.message || (editMode.value ? '学院更新失败' : '学院创建失败'))
    }
  } catch (error) {
    console.error('保存学院失败:', error)
    ElMessage.error(error.message || '保存学院失败')
  }
}

// 删除学院
const deleteCollege = async (college) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除学院「${college.name}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await fetch(`/api/college/delete/${college.id}`, {
      method: 'DELETE'
    })
    
    const data = await response.json()
    if (data.success) {
      ElMessage.success('学院删除成功')
      await getColleges()
    } else {
      throw new Error(data.message || '学院删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除学院失败:', error)
      ElMessage.error(error.message || '删除学院失败')
    }
  }
}

// 搜索学院
const searchColleges = () => {
  // 这里可以实现客户端搜索，或者调用后端API搜索
  // 暂时使用客户端搜索
  if (searchKeyword.value.trim() === '') {
    getColleges()
  } else {
    const keyword = searchKeyword.value.toLowerCase()
    const filteredColleges = colleges.value.filter(college => 
      college.name.toLowerCase().includes(keyword)
    )
    colleges.value = filteredColleges
  }
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  getColleges()
}

// 初始化
onMounted(async () => {
  await getColleges()
})
</script>

<template>
  <div class="college-list">
    <!-- 页面标题和操作区 -->
    <div class="header">
      <h1 class="page-title">学院管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="openAddDialog">
          <Plus style="margin-right: 4px;" />
          新增学院
        </el-button>
        <el-button @click="getColleges">
          <Refresh style="margin-right: 4px;" />
          刷新
        </el-button>
      </div>
    </div>

    <!-- 搜索区 -->
    <div class="search-area">
      <el-input
        v-model="searchKeyword"
        placeholder="输入学院名称搜索"
        clearable
        prefix-icon="Search"
        style="width: 300px;"
      />
      <el-button type="primary" @click="searchColleges">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 学院列表 -->
    <div class="college-table">
      <el-table
        v-loading="loading"
        :data="colleges"
        style="width: 100%"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="学院名称" />
        <el-table-column label="颜色" width="120">
          <template #default="scope">
            <div 
              class="color-block"
              :style="{ backgroundColor: scope.row.color }"
            ></div>
            <span class="color-code">{{ scope.row.color }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="openEditDialog(scope.row)"
            >
              <Edit style="margin-right: 4px;" />
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="deleteCollege(scope.row)"
            >
              <Delete style="margin-right: 4px;" />
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 学院编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editMode ? '编辑学院' : '新增学院'"
      width="500px"
    >
      <el-form :model="currentCollege" label-width="100px">
        <el-form-item label="学院名称">
          <el-input v-model="currentCollege.name" placeholder="请输入学院名称" />
        </el-form-item>
        <el-form-item label="学院颜色">
          <el-color-picker v-model="currentCollege.color" show-alpha />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveCollege">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.college-list {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .page-title {
      font-size: 24px;
      color: #1d2129;
      font-weight: 600;
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .search-area {
    display: flex;
    gap: 12px;
    margin-bottom: 24px;
    align-items: center;
  }

  .college-table {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    padding: 20px;

    .color-block {
      display: inline-block;
      width: 20px;
      height: 20px;
      border-radius: 4px;
      margin-right: 8px;
      vertical-align: middle;
    }

    .color-code {
      font-size: 14px;
      color: #666;
      vertical-align: middle;
    }
  }
}
</style>
