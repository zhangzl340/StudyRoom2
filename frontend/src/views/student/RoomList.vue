<template>
  <div class="room-list-container">
    <el-card class="room-list-card">
      <template #header>
        <div class="card-header">
          <h2>自习室列表</h2>
        </div>
      </template>
      
      <!-- 筛选和搜索 -->
      <div class="filter-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="楼栋">
              <el-select v-model="filterForm.building" placeholder="请选择楼栋">
                <el-option label="第一教学楼" value="第一教学楼" />
                <el-option label="第二教学楼" value="第二教学楼" />
                <el-option label="图书馆" value="图书馆" />
                <el-option label="实验楼" value="实验楼" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="楼层">
              <el-select v-model="filterForm.floor" placeholder="请选择楼层">
                <el-option label="1楼" value="1" />
                <el-option label="2楼" value="2" />
                <el-option label="3楼" value="3" />
                <el-option label="4楼" value="4" />
                <el-option label="5楼" value="5" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="状态">
              <el-select v-model="filterForm.status" placeholder="请选择状态">
                <el-option label="可用" value="available" />
                <el-option label="不可用" value="unavailable" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="搜索">
              <el-input
                v-model="filterForm.keyword"
                placeholder="请输入自习室名称"
                prefix-icon="Search"
                clearable
                @keyup.enter="handleFilter"
              >
                <template #append>
                  <el-button @click="handleFilter">搜索</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <div class="filter-actions">
          <el-button type="primary" @click="handleFilter">应用筛选</el-button>
          <el-button @click="resetFilter">重置筛选</el-button>
          <el-button @click="showAvailableRooms">只看可用</el-button>
        </div>
      </div>
      
      <!-- 自习室列表 -->
      <div class="room-list-section">
        <el-table
          :data="filteredRooms"
          style="width: 100%"
          border
          v-loading="loading"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="自习室名称" width="180" />
          <el-table-column prop="building" label="楼栋" width="120" />
          <el-table-column prop="floor" label="楼层" width="80" />
          <el-table-column prop="totalSeats" label="总座位数" width="100" />
          <el-table-column prop="availableSeats" label="可用座位数" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'available' ? 'success' : 'danger'">
                {{ scope.row.status === 'available' ? '可用' : '不可用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small"
                @click="viewRoomDetail(scope.row.id)"
                :disabled="scope.row.status !== 'available'"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-section">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
      
      <!-- 统计信息 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ totalRooms }}</div>
                <div class="stat-label">总自习室</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ availableRoomsCount }}</div>
                <div class="stat-label">可用自习室</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ totalSeatsCount }}</div>
                <div class="stat-label">总座位数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ totalAvailableSeatsCount }}</div>
                <div class="stat-label">可用座位数</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useRoomStore } from '../../stores/room'

// 路由
const router = useRouter()

// 自习室Store
const roomStore = useRoomStore()

// 状态
const loading = ref(false)

// 筛选表单
const filterForm = reactive({
  building: '',
  floor: '',
  status: '',
  keyword: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10
})

// 自习室数据
const rooms = ref([
  {
    id: 1,
    name: '第一自习室',
    building: '第一教学楼',
    floor: 1,
    totalSeats: 50,
    availableSeats: 35,
    status: 'available'
  },
  {
    id: 2,
    name: '第二自习室',
    building: '第一教学楼',
    floor: 2,
    totalSeats: 40,
    availableSeats: 20,
    status: 'available'
  },
  {
    id: 3,
    name: '第三自习室',
    building: '第二教学楼',
    floor: 1,
    totalSeats: 60,
    availableSeats: 45,
    status: 'available'
  },
  {
    id: 4,
    name: '第四自习室',
    building: '图书馆',
    floor: 3,
    totalSeats: 100,
    availableSeats: 70,
    status: 'available'
  },
  {
    id: 5,
    name: '第五自习室',
    building: '实验楼',
    floor: 2,
    totalSeats: 30,
    availableSeats: 0,
    status: 'unavailable'
  }
])

// 计算属性
const total = computed(() => filteredRooms.value.length)

const filteredRooms = computed(() => {
  let result = [...rooms.value]
  
  // 按楼栋筛选
  if (filterForm.building) {
    result = result.filter(room => room.building === filterForm.building)
  }
  
  // 按楼层筛选
  if (filterForm.floor) {
    result = result.filter(room => room.floor === parseInt(filterForm.floor))
  }
  
  // 按状态筛选
  if (filterForm.status) {
    result = result.filter(room => room.status === filterForm.status)
  }
  
  // 按关键词搜索
  if (filterForm.keyword) {
    const keyword = filterForm.keyword.toLowerCase()
    result = result.filter(room => 
      room.name.toLowerCase().includes(keyword) ||
      room.building.toLowerCase().includes(keyword)
    )
  }
  
  // 分页
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return result.slice(start, end)
})

const totalRooms = computed(() => rooms.value.length)

const availableRoomsCount = computed(() => {
  return rooms.value.filter(room => room.status === 'available').length
})

const totalSeatsCount = computed(() => {
  return rooms.value.reduce((sum, room) => sum + room.totalSeats, 0)
})

const totalAvailableSeatsCount = computed(() => {
  return rooms.value.reduce((sum, room) => sum + room.availableSeats, 0)
})

// 方法
const handleFilter = () => {
  pagination.currentPage = 1
}

const resetFilter = () => {
  filterForm.building = ''
  filterForm.floor = ''
  filterForm.status = ''
  filterForm.keyword = ''
  pagination.currentPage = 1
}

const showAvailableRooms = () => {
  filterForm.status = 'available'
  pagination.currentPage = 1
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
}

const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
}

const viewRoomDetail = (roomId: number) => {
  router.push(`/student/room/${roomId}`)
}

// 加载自习室列表
const loadRooms = async () => {
  loading.value = true
  try {
    const params = {
      building: filterForm.building,
      floor: filterForm.floor,
      status: filterForm.status,
      page: pagination.currentPage,
      size: pagination.pageSize
    }
    const response = await roomStore.fetchRooms(params) as any
    if (response.code === 200) {
      // 更新自习室数据
      rooms.value = response.data
    }
  } catch (error) {
    console.error('加载自习室列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 页面加载时加载自习室列表
onMounted(() => {
  loadRooms()
})
</script>

<style scoped lang="scss">
.room-list-container {
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.room-list-card {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  h2 {
    margin: 0;
    font-size: 20px;
    color: #303133;
  }
}

.filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.filter-actions {
  margin-top: 10px;
  text-align: right;
}

.room-list-section {
  margin-bottom: 30px;
}

.pagination-section {
  margin-top: 20px;
  text-align: right;
}

.stats-section {
  margin-top: 30px;
}

.stat-card {
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-item {
  text-align: center;
  
  .stat-value {
    font-size: 24px;
    font-weight: bold;
    color: #1890ff;
    margin-bottom: 5px;
  }
  
  .stat-label {
    font-size: 14px;
    color: #606266;
  }
}

@media (max-width: 768px) {
  .room-list-container {
    padding: 10px;
  }
  
  .room-list-card {
    padding: 10px;
  }
  
  .filter-section {
    padding: 15px;
  }
  
  .el-row {
    .el-col {
      margin-bottom: 10px;
    }
  }
  
  .stats-section {
    .el-col {
      margin-bottom: 15px;
    }
  }
}
</style>
