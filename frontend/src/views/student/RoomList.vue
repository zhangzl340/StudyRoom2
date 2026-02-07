<template>
  <div class="room-list-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="filterForm.keyword"
        placeholder="搜索自习室"
        prefix-icon="Search"
        clearable
        @keyup.enter="handleFilter"
        class="search-input"
      />
      <el-icon class="search-icon"><Search /></el-icon>
    </div>
    
    <!-- 自习室列表 -->
    <div class="room-list-section">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else class="room-grid">
        <div 
          v-for="room in filteredRooms" 
          :key="room.id" 
          class="room-card"
          @click="viewRoomDetail(room.id)"
        >
          <!-- 自习室图片 -->
          <div class="room-image">
            <img 
              :src="room.image || `https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20study%20room%20interior%20with%20tables%20and%20chairs&image_size=landscape_4_3`" 
              :alt="room.name"
              class="room-img"
            />
          </div>
          
          <!-- 自习室信息 -->
          <div class="room-info">
            <div class="room-header">
              <h3 class="room-name">{{ room.name }}</h3>
              <el-tag :type="room.status === 'available' ? 'success' : 'danger'" size="small">
                {{ room.status === 'available' ? '可预约' : '已满' }}
              </el-tag>
            </div>
            
            <div class="room-details">
              <div class="room-detail-item">
                <el-icon class="detail-icon"><OfficeBuilding /></el-icon>
                <span>{{ room.building }} {{ room.floor }}楼</span>
              </div>
              <div class="room-detail-item">
                <el-icon class="detail-icon"><Timer /></el-icon>
                <span>08:00-22:00</span>
              </div>
            </div>
            
            <div class="room-footer">
              <div class="room-seats">
                <span class="seats-label">余位：</span>
                <span class="seats-value">{{ room.availableSeats }}/{{ room.totalSeats }}</span>
              </div>
              <div class="room-price">
                <span class="price-label">价格：</span>
                <span class="price-value">¥4/小时</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="!loading && filteredRooms.length === 0" class="empty-state">
        <el-empty description="暂无可用自习室" />
      </div>
      
      <!-- 分页 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, prev, pager, next"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useRoomStore } from '../../stores/room'
import { OfficeBuilding, Timer, Search } from '@element-plus/icons-vue'

// 路由
const router = useRouter()

// 自习室Store
const roomStore = useRoomStore()

// 状态
const loading = ref(false)

// 筛选表单
const filterForm = reactive({
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
    name: '静思自习室',
    building: '第一教学楼',
    floor: 3,
    totalSeats: 30,
    availableSeats: 15,
    status: 'available',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=quiet%20study%20room%20with%20wooden%20desks%20and%20good%20lighting&image_size=landscape_4_3'
  },
  {
    id: 2,
    name: '致远阅览室',
    building: '图书馆',
    floor: 2,
    totalSeats: 45,
    availableSeats: 20,
    status: 'available',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20reading%20room%20with%20large%20windows&image_size=landscape_4_3'
  },
  {
    id: 3,
    name: '勤学自习室',
    building: '第二教学楼',
    floor: 4,
    totalSeats: 25,
    availableSeats: 5,
    status: 'available',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=study%20room%20with%20individual%20study%20cubicles&image_size=landscape_4_3'
  },
  {
    id: 4,
    name: '创新自习室',
    building: '实验楼',
    floor: 1,
    totalSeats: 20,
    availableSeats: 0,
    status: 'unavailable',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20study%20room%20with%20group%20study%20areas&image_size=landscape_4_3'
  }
])

// 计算属性
const total = computed(() => filteredRooms.value.length)

const filteredRooms = computed(() => {
  let result = [...rooms.value]
  
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

// 方法
const handleFilter = () => {
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
  padding: 15px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

// 搜索栏
.search-bar {
  position: relative;
  margin-bottom: 20px;
  padding: 0 10px;

  .search-input {
    height: 45px;
    border-radius: 25px;
    padding-left: 40px;
    padding-right: 40px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .search-icon {
    position: absolute;
    right: 25px;
    top: 50%;
    transform: translateY(-50%);
    color: #909399;
    font-size: 18px;
    cursor: pointer;
  }
}

// 自习室列表
.room-list-section {
  .loading-container {
    margin-bottom: 20px;
  }

  .room-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 15px;
  }

  .room-card {
    background-color: white;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
    cursor: pointer;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
    }

    // 自习室图片
    .room-image {
      height: 160px;
      overflow: hidden;

      .room-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.3s ease;
      }

      &:hover .room-img {
        transform: scale(1.05);
      }
    }

    // 自习室信息
    .room-info {
      padding: 15px;

      .room-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;

        .room-name {
          font-size: 16px;
          font-weight: bold;
          color: #303133;
          margin: 0;
        }
      }

      .room-details {
        margin-bottom: 15px;

        .room-detail-item {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;
          font-size: 14px;
          color: #606266;

          .detail-icon {
            font-size: 14px;
            color: #909399;
          }
        }
      }

      .room-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-top: 10px;
        border-top: 1px solid #f0f0f0;

        .room-seats {
          .seats-label {
            font-size: 14px;
            color: #606266;
          }

          .seats-value {
            font-size: 14px;
            font-weight: bold;
            color: #409EFF;
          }
        }

        .room-price {
          .price-label {
            font-size: 14px;
            color: #606266;
          }

          .price-value {
            font-size: 14px;
            font-weight: bold;
            color: #F56C6C;
          }
        }
      }
    }
  }

  // 空状态
  .empty-state {
    margin: 40px 0;
    text-align: center;
  }

  // 分页
  .pagination-section {
    margin-top: 30px;
    display: flex;
    justify-content: center;

    .el-pagination {
      .el-pagination__sizes {
        margin-right: 10px;
      }
    }
  }
}

// 响应式调整
@media (max-width: 768px) {
  .room-list-container {
    padding: 10px;
  }

  .search-bar {
    padding: 0 5px;

    .search-input {
      height: 40px;
      border-radius: 20px;
    }
  }

  .room-list-section {
    .room-grid {
      grid-template-columns: 1fr;
      gap: 12px;
    }

    .room-card {
      .room-image {
        height: 140px;
      }

      .room-info {
        padding: 12px;

        .room-header {
          .room-name {
            font-size: 15px;
          }
        }

        .room-details {
          .room-detail-item {
            font-size: 13px;
          }
        }

        .room-footer {
          .room-seats,
          .room-price {
            font-size: 13px;
          }
        }
      }
    }
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .room-list-section {
    .room-grid {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}
</style>
