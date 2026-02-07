<template>
  <div class="student-layout">
    <!-- PC端布局 -->
    <div class="pc-layout" v-if="!isMobile">
      <el-container>
        <!-- 侧边栏 -->
        <el-aside width="200px" class="sidebar">
          <div class="sidebar-header">
            <h3>学生中心</h3>
          </div>
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="dashboard">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="rooms">
              <el-icon><OfficeBuilding /></el-icon>
              <span>自习室列表</span>
            </el-menu-item>
            <el-menu-item index="reservations">
              <el-icon><Calendar /></el-icon>
              <span>我的预约</span>
            </el-menu-item>
            <el-menu-item index="checkin">
              <el-icon><Check /></el-icon>
              <span>签到签退</span>
            </el-menu-item>
            <el-menu-item index="profile">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <!-- 主内容区 -->
        <el-container>
          <!-- 顶部导航 -->
          <el-header class="header">
            <div class="header-left">
              <span class="breadcrumb">
                {{ currentBreadcrumb }}
              </span>
            </div>
            <div class="header-right">
              <el-dropdown>
                <span class="user-dropdown">
                  <el-avatar size="small">{{ userInitial }}</el-avatar>
                  <span class="user-name">{{ userName }}</span>
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="goToProfile">个人信息</el-dropdown-item>
                    <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </el-header>
          
          <!-- 内容区 -->
          <el-main class="main-content">
            <router-view />
          </el-main>
        </el-container>
      </el-container>
    </div>
    
    <!-- 手机端布局 -->
    <div class="mobile-layout" v-else>
      <!-- 顶部导航 -->
      <div class="mobile-header">
        <div class="header-left">
          <h1 class="app-title">自习室预约平台</h1>
        </div>
        <div class="header-right">
          <el-icon class="notification-icon"><Bell /></el-icon>
          <el-badge value="2" :hidden="false" class="notification-badge" />
        </div>
      </div>
      
      <!-- 内容区 -->
      <div class="mobile-content">
        <router-view />
      </div>
      
      <!-- 底部导航栏 -->
      <div class="mobile-footer">
        <div 
          v-for="item in footerItems" 
          :key="item.index"
          class="footer-item"
          :class="{ active: activeMenu === item.index }"
          @click="handleFooterItemClick(item.index)"
        >
          <el-icon class="footer-icon"><component :is="item.icon" /></el-icon>
          <span class="footer-text">{{ item.text }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { House, OfficeBuilding, Calendar, Check, User, ArrowDown, Bell } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 状态
const activeMenu = ref('dashboard')
const currentBreadcrumb = ref('首页')
const isMobile = ref(false)

// 底部导航栏项目
const footerItems = ref([
  { index: 'dashboard', text: '首页', icon: House, path: '/student/dashboard' },
  { index: 'rooms', text: '预约', icon: Calendar, path: '/student/rooms' },
  { index: 'profile', text: '个人中心', icon: User, path: '/student/profile' }
])

// 计算属性
const userName = computed(() => authStore.userInfo?.name || '学生')
const userInitial = computed(() => {
  return userName.value.charAt(0).toUpperCase()
})

// 方法
function handleMenuSelect(key: string) {
  const routeMap: Record<string, string> = {
    dashboard: '/student/dashboard',
    rooms: '/student/rooms',
    reservations: '/student/reservations',
    checkin: '/student/checkin',
    profile: '/student/profile'
  }
  
  router.push(routeMap[key] || '/student/dashboard')
}

function handleFooterItemClick(key: string) {
  const routeMap: Record<string, string> = {
    dashboard: '/student/dashboard',
    rooms: '/student/rooms',
    profile: '/student/profile'
  }
  
  activeMenu.value = key
  router.push(routeMap[key] || '/student/dashboard')
}

function goToProfile() {
  router.push('/student/profile')
}

function logout() {
  authStore.logout()
  router.push('/login')
  ElMessage.success('已退出登录')
}

function updateActiveMenu() {
  const path = route.path
  if (path.includes('/dashboard')) {
    activeMenu.value = 'dashboard'
    currentBreadcrumb.value = '首页'
  } else if (path.includes('/rooms')) {
    activeMenu.value = 'rooms'
    currentBreadcrumb.value = '自习室列表'
  } else if (path.includes('/reservations')) {
    activeMenu.value = 'reservations'
    currentBreadcrumb.value = '我的预约'
  } else if (path.includes('/checkin')) {
    activeMenu.value = 'checkin'
    currentBreadcrumb.value = '签到签退'
  } else if (path.includes('/profile')) {
    activeMenu.value = 'profile'
    currentBreadcrumb.value = '个人信息'
  }
}

// 检查是否为移动端
function checkMobile() {
  isMobile.value = window.innerWidth <= 768
}

// 生命周期
onMounted(() => {
  updateActiveMenu()
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

// 监听路由变化
router.afterEach(() => {
  updateActiveMenu()
})
</script>

<style scoped>
.student-layout {
  height: 100vh;
  overflow: hidden;
}

/* PC端布局 */
.pc-layout {
  height: 100%;
}

.sidebar {
  background-color: #2c3e50;
  color: white;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 100;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #34495e;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 18px;
  color: white;
}

.sidebar-menu {
  margin-top: 20px;
  background-color: transparent;
  border-right: none;
}

.sidebar-menu .el-menu-item {
  color: #ecf0f1;
  height: 60px;
  line-height: 60px;
  font-size: 14px;
  margin: 0 10px;
  border-radius: 8px;
}

.sidebar-menu .el-menu-item:hover {
  background-color: #34495e;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #3498db;
  color: white;
}

.header {
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  margin-left: 200px;
}

.header-left .breadcrumb {
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 20px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f5f7fa;
}

.user-name {
  margin: 0 8px;
  font-size: 14px;
  color: #303133;
}

.main-content {
  margin-left: 200px;
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 手机端布局 */
.mobile-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.mobile-header {
  height: 50px;
  background-color: #409EFF;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.app-title {
  font-size: 18px;
  font-weight: bold;
  margin: 0;
}

.notification-icon {
  font-size: 20px;
  margin-right: 8px;
  cursor: pointer;
}

.notification-badge {
  position: absolute;
  top: -5px;
  right: -5px;
}

.mobile-content {
  flex: 1;
  margin-top: 50px;
  margin-bottom: 60px;
  padding: 10px;
  overflow-y: auto;
  background-color: #f5f7fa;
}

.mobile-footer {
  height: 60px;
  background-color: white;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-around;
  align-items: center;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.footer-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  height: 100%;
  cursor: pointer;
  transition: all 0.3s;
}

.footer-item.active {
  color: #409EFF;
}

.footer-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.footer-text {
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .pc-layout {
    display: none;
  }
  
  .mobile-layout {
    display: flex;
  }
}

@media (min-width: 769px) {
  .pc-layout {
    display: block;
  }
  
  .mobile-layout {
    display: none;
  }
}
</style>