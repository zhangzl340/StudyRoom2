<template>
  <div class="college-list">
    <div class="header">
      <h1 class="page-title">学院管理</h1>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon> 新增学院
      </el-button>
    </div>
    
    <el-table :data="colleges" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="学院名称" />
      <el-table-column label="颜色" width="120">
        <template #default="scope">
          <div :style="{ backgroundColor: scope.row.color }" class="color-block"></div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="openEditDialog(scope.row)">
            <el-icon><Edit /></el-icon> 编辑
          </el-button>
          <el-button type="danger" size="small" @click="confirmDelete(scope.row.id)">
            <el-icon><Delete /></el-icon> 删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="学院名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入学院名称" />
        </el-form-item>
        <el-form-item label="学院颜色" prop="color">
          <el-color-picker v-model="form.color" show-alpha />
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

<script setup>
import { ref, onMounted } from 'vue'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const colleges = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增学院')
const form = ref({
  id: '',
  name: '',
  color: '#409EFF'
})

const loadColleges = async () => {
  try {
    const response = await axios.get('/api/college/list')
    if (response.data.success) {
      colleges.value = response.data.data
    } else {
      ElMessage.error('获取学院列表失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
  }
}

const openAddDialog = () => {
  dialogTitle.value = '新增学院'
  form.value = {
    id: '',
    name: '',
    color: '#409EFF'
  }
  dialogVisible.value = true
}

const openEditDialog = (college) => {
  dialogTitle.value = '编辑学院'
  form.value = { ...college }
  dialogVisible.value = true
}

const saveCollege = async () => {
  try {
    let response
    if (form.value.id) {
      response = await axios.put('/api/college', form.value)
    } else {
      response = await axios.post('/api/college', form.value)
    }
    if (response.data.success) {
      ElMessage.success('操作成功')
      dialogVisible.value = false
      loadColleges()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
  }
}

const confirmDelete = async (id) => {
  try {
    const response = await axios.delete(`/api/college/${id}`)
    if (response.data.success) {
      ElMessage.success('删除成功')
      loadColleges()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试')
  }
}

onMounted(() => {
  loadColleges()
})
</script>

<style scoped>
.college-list {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
}

.color-block {
  width: 40px;
  height: 20px;
  border-radius: 4px;
  display: inline-block;
}

.dialog-footer {
  width: 100%;
  text-align: right;
}
</style>