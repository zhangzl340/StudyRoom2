<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { 
  House, 
  OfficeBuilding, 
  Suitcase, 
  Calendar, 
  User, 
  ArrowDown, 
  ArrowRight, 
  ArrowLeft,
  SwitchButton,
  Expand,
  Fold
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 侧边栏折叠状态
const sidebarCollapsed = ref(false)
// 移动端侧边栏显示状态
const mobileSidebarVisible = ref(false)

// 计算当前激活的菜单项
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

// 侧边栏菜单项
const sidebarItems = [
  {
    name: 'dashboard',
    label: '仪表盘',
    icon: House,
    path: '/admin/dashboard'
  },
  {
    name: 'room',
    label: '自习室管理',
    icon: OfficeBuilding,
    path: '/admin/rooms'
  },
  {
    name: 'seat',
    label: '座位管理',
    icon: Suitcase,
    path: '/admin/seats'
  },
  {
    name: 'reservation',
    label: '预约管理',
    icon: Calendar,
    path: '/admin/reservations'
  },
  {
    name: 'user',
    label: '用户管理',
    icon: User,
    path: '/admin/users'
  }
]

// 切换侧边栏折叠状态
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// 处理移动端侧边栏
const toggleMobileSidebar = () => {
  mobileSidebarVisible.value = !mobileSidebarVisible.value
}

// 退出登录
const logout = async () => {
  try {
    await authStore.logout()
    router.push('/login')
  } catch (error) {
    console.error('退出登录失败:', error)
  }
}

// 处理菜单点击
const handleMenuClick = (path) => {
  router.push(path)
  // 如果是移动端，点击后关闭侧边栏
  if (isMobile.value) {
    mobileSidebarVisible.value = false
  }
}

// 响应式处理
const isMobile = ref(false)
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

// 监听窗口大小变化
onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<template>
  <div class="admin-layout">
    <!-- 移动端遮罩层 -->
    <div 
      v-if="isMobile && mobileSidebarVisible" 
      class="mobile-mask"
      @click="mobileSidebarVisible = false"
    ></div>

    <!-- 侧边栏 -->
    <div 
      class="sidebar" 
      :class="{ 
        collapsed: sidebarCollapsed && !isMobile,
        'mobile-visible': mobileSidebarVisible && isMobile
      }"
    >
      <div class="sidebar-header">
        <h3 class="logo" v-if="!sidebarCollapsed || isMobile">管理后台</h3>
        <h3 class="logo" v-else>管</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="sidebarCollapsed && !isMobile"
        :background-color="'#001529'"
        :text-color="'#bfcbd9'"
        :unique-opened="true"
        :active-text-color="'#409EFF'"
        :collapse-transition="false"
        mode="vertical"
        router
      >
        <el-menu-item 
          v-for="item in sidebarItems" 
          :key="item.path" 
          :index="item.path"
          @click="handleMenuClick(item.path)"
        >
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <template #title>{{ item.label }}</template>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <div class="navbar">
        <div class="navbar-left">
          <el-button 
            :icon="isMobile ? (mobileSidebarVisible ? Fold : Expand) : (sidebarCollapsed ? ArrowRight : ArrowLeft)" 
            @click="isMobile ? toggleMobileSidebar() : toggleSidebar()" 
            circle
            size="small"
          >
          </el-button>
          <h2 class="page-title">{{ route.meta.title || '管理后台' }}</h2>
        </div>
        <div class="navbar-right">
          <el-dropdown>
            <span class="user-info">
              <el-avatar size="small">{{ authStore.user?.username?.charAt(0) || '管' }}</el-avatar>
              <span class="username">{{ authStore.user?.username || '管理员' }}</span>
              <el-icon class="el-icon--right">
                <ArrowDown />
              </el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/admin/profile')">
                  <el-icon>
                    <User />
                  </el-icon>
                  <span>个人中心</span>
                </el-dropdown-item>
                <el-dropdown-item divided @click="logout">
                  <el-icon>
                    <SwitchButton />
                  </el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 内容区 -->
      <div class="content">
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  position: relative;

  .mobile-mask {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 999;
    display: none;
  }

  .sidebar {
    width: 200px;
    height: 100%;
    background-color: #001529;
    color: #bfcbd9;
    transition: width 0.3s, transform 0.3s;
    overflow: hidden;
    z-index: 1000;

    &.collapsed {
      width: 64px;
    }

    &.mobile-visible {
      transform: translateX(0);
    }

    .sidebar-header {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0 20px;
      border-bottom: 1px solid #002140;

      .logo {
        font-size: 18px;
        font-weight: bold;
        color: #ffffff;
        white-space: nowrap;
        overflow: hidden;
      }
    }

    .el-menu {
      border-right: none;
      height: calc(100% - 60px);
      overflow-y: auto;

      .el-menu-item {
        height: 56px;
        line-height: 56px;
        margin: 0;
        border-radius: 0;

        &:hover {
          background-color: rgba(255, 255, 255, 0.08);
        }

        &.is-active {
          background-color: rgba(64, 158, 255, 0.2);
        }

        .el-icon {
          font-size: 18px;
        }
      }
    }
  }

  .main-content {
    flex: 1;
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .navbar {
      height: 60px;
      background-color: #ffffff;
      border-bottom: 1px solid #ebeef5;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 20px;
      box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
      z-index: 100;

      .navbar-left {
        display: flex;
        align-items: center;
        gap: 16px;

        .page-title {
          font-size: 18px;
          font-weight: 600;
          color: #303133;
          margin: 0;
        }
      }

      .navbar-right {
        display: flex;
        align-items: center;

        .user-info {
          display: flex;
          align-items: center;
          gap: 8px;
          cursor: pointer;
          padding: 8px 12px;
          border-radius: 4px;
          transition: background-color 0.3s;

          &:hover {
            background-color: #f5f7fa;
          }

          .username {
            font-size: 14px;
            color: #303133;
            font-weight: 500;
          }
        }
      }
    }

    .content {
      flex: 1;
      padding: 20px;
      overflow-y: auto;
      background-color: #f5f7fa;
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .mobile-mask {
    display: block !important;
  }

  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
    transform: translateX(-100%);

    &.collapsed {
      width: 200px;
    }

    &.mobile-visible {
      transform: translateX(0);
    }

    .sidebar-header {
      justify-content: flex-start;
      padding-left: 24px;
    }
  }

  .main-content {
    width: 100%;

    .content {
      padding: 16px !important;
    }
  }
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;

  &:hover {
    background: #a8a8a8;
  }
}

.sidebar .el-menu::-webkit-scrollbar-track {
  background: #002140;
}

.sidebar .el-menu::-webkit-scrollbar-thumb {
  background: #4a4a4a;
}
</style>