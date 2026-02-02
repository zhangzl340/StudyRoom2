<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { HomeFilled, UserFilled, Comment } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
const route = useRoute()
const router = useRouter()

// 定义菜单项
const menuItems = [
  {
    name: 'home',
    label: '首页',
    icon: HomeFilled,
    path: '/student/dashboard'
  },
  {
    name: 'reservation',
    label: '预约',
    icon: Comment,
    path: '/student/reservations'
  },
  {
    name: 'user',
    label: '个人中心',
    icon: UserFilled,
    path: '/student/profile'
  }
]

// 当前激活的菜单项
const activeMenu = ref('')

// 监听路由变化，更新激活菜单项
watch(() => route.path, (newPath) => {
  const menuItem = menuItems.find(item => item.path === newPath)
  if (menuItem) {
    activeMenu.value = menuItem.name
  }
}, { immediate: true })

// 菜单点击处理函数
const handleMenuClick = (path) => {
  router.replace(path)
}
</script>

<template>
  <div class="user-layout">
    <el-container>
      <el-main>
        <router-view></router-view>
      </el-main>
      <el-footer>
        <div v-for="item in menuItems" :key="item.name" class="menu" :class="{ active: activeMenu === item.name }"
          @click="handleMenuClick(item.path)">
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <span>{{ item.label }}</span>
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<style lang="scss" scoped>
.user-layout {
  height: 100vh;

  .el-container {
    height: 100%;
    background-color: #f1f1f1;
    overflow: hidden;

    .el-main {
      padding: 0;
      overflow-y: auto;
    }

    .el-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 50px;
      background-color: #FFFFFF;

      .menu {
        display: flex;
        align-items: center;
        justify-content: center;
        flex-direction: column;
        width: 30%;
        color: #707070;
        font-size: 10px;

        .el-icon {
          font-size: 20px;
        }

        &.active {
          color: #409eff;
        }
      }
    }
  }
}

/* 响应式设计 */
@media (min-width: 768px) {
  .user-layout {
    max-width: 768px;
    margin: 0 auto;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  }
}
</style>