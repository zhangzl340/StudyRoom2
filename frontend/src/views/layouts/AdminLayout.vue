<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const authStore = useAuthStore()

// 侧边栏折叠状态
const sidebarCollapsed = ref(false)

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
    icon: 'House',
    path: '/admin/dashboard'
  },
  {
    name: 'room',
    label: '自习室管理',
    icon: 'Building',
    path: '/admin/rooms'
  },
  {
    name: 'seat',
    label: '座位管理',
    icon: 'Suitcase',
    path: '/admin/seats'
  },
  {
    name: 'reservation',
    label: '预约管理',
    icon: 'Calendar',
    path: '/admin/reservations'
  },
  {
    name: 'user',
    label: '用户管理',
    icon: 'User',
    path: '/admin/users'
  }
]

// 切换侧边栏折叠状态
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// 退出登录
const logout = () => {
  authStore.logout()
  window.location.href = '/login'
}
</script>

<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <h3 class="logo">管理后台</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="sidebarCollapsed"
        :background-color="'#001529'"
        :text-color="'#bfcbd9'"
        :unique-opened="true"
        :active-text-color="'#409EFF'"
        :collapse-transition="false"
        mode="vertical"
      >
        <el-menu-item v-for="item in sidebarItems" :key="item.path" :index="item.path">
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <template #title>{{ item.label }}</template>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 主内容区 -->
    <div class="main-content" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
      <!-- 顶部导航栏 -->
      <div class="navbar">
        <div class="navbar-left">
          <el-button :icon="sidebarCollapsed ? 'ArrowRight' : 'ArrowLeft'" @click="toggleSidebar" circle>
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
                <el-dropdown-item @click="logout">
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

  .sidebar {
    width: 200px;
    height: 100%;
    background-color: #001529;
    color: #bfcbd9;
    transition: width 0.3s;
    overflow: hidden;

    &.collapsed {
      width: 64px;
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
      }
    }

    .el-menu {
      border-right: none;

      .el-menu-item {
        height: 60px;
        line-height: 60px;
        margin: 0;

        &:hover {
          background-color: rgba(255, 255, 255, 0.08);
        }

        &.is-active {
          background-color: rgba(64, 158, 255, 0.2);
        }
      }
    }
  }

  .main-content {
    flex: 1;
    height: 100%;
    display: flex;
    flex-direction: column;
    transition: margin-left 0.3s;

    &.sidebar-collapsed {
      margin-left: 64px;
    }

    .navbar {
      height: 60px;
      background-color: #ffffff;
      border-bottom: 1px solid #ebeef5;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 20px;

      .navbar-left {
        display: flex;
        align-items: center;
        gap: 10px;

        .page-title {
          font-size: 18px;
          font-weight: bold;
          color: #303133;
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

          &:hover {
            background-color: #f5f7fa;
          }

          .username {
            font-size: 14px;
            color: #303133;
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
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    z-index: 1000;
    height: 100vh;
    box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);

    &.collapsed {
      transform: translateX(-100%);
    }
  }

  .main-content {
    margin-left: 0 !important;
  }

  .content {
    padding: 10px !important;
  }
}
</style>