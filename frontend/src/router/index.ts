import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: (to)=>{
      // 根据登录状态和角色动态重定向
      const token = localStorage.getItem('token')
      const role = localStorage.getItem('role')
      
      if (!token) {
        return '/login'
      } else if (role === 'admin') {
        return '/admin/dashboard'
      } else {
        return '/student/dashboard'
      }
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue'),
    meta: {
      title: '注册',
      requiresAuth: false
    }
  },
  {
    path: '/student',
    name: 'UserLayout',
    component: () => import('../views/layouts/UserLayout.vue'),
    meta: {
      title: '学生中心',
      requiresAuth: true,
      role: 'student'
    },
    children: [
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: () => import('../views/student/Dashboard.vue'),
        meta: {
          title: '学生首页'
        }
      },
      {
        path: 'rooms',
        name: 'StudentRooms',
        component: () => import('../views/student/RoomList.vue'),
        meta: {
          title: '自习室列表'
        }
      },
      {
        path: 'room/:id',
        name: 'StudentRoomDetail',
        component: () => import('../views/student/RoomDetail.vue'),
        meta: {
          title: '自习室详情'
        }
      },
      {
        path: 'reservations',
        name: 'StudentReservations',
        component: () => import('../views/student/ReservationList.vue'),
        meta: {
          title: '我的预约'
        }
      },
      {
        path: 'checkin',
        name: 'StudentCheckin',
        component: () => import('../views/student/Checkin.vue'),
        meta: {
          title: '签到签退'
        }
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('../views/student/Profile.vue'),
        meta: {
          title: '个人中心'
        }
      }
    ]
  },
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('../views/layouts/AdminLayout.vue'),
    meta: {
      title: '管理后台',
      requiresAuth: true,
      role: 'admin'
    },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: {
          title: '仪表盘'
        }
      },
      {
        path: 'rooms',
        name: 'AdminRooms',
        component: () => import('../views/admin/RoomList.vue'),
        meta: {
          title: '自习室管理'
        }
      },
      {
        path: 'seats',
        name: 'AdminSeats',
        component: () => import('../views/admin/SeatList.vue'),
        meta: {
          title: '座位管理'
        }
      },
      {
        path: 'reservations',
        name: 'AdminReservations',
        component: () => import('../views/admin/ReservationList.vue'),
        meta: {
          title: '预约管理'
        }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/UserList.vue'),
        meta: {
          title: '用户管理'
        }
      },
      {
        path: 'announcements',
        name: 'AdminAnnouncements',
        component: () => import('../views/admin/AnnouncementList.vue'),
        meta: {
          title: '公告管理'
        }
      },
      {
        path: 'carousels',
        name: 'AdminCarousels',
        component: () => import('../views/admin/CarouselList.vue'),
        meta: {
          title: '轮播图管理'
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  // 设置页面标题
  document.title = to.meta.title as string || '高校智能自习室系统'

  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('role')

  if ((to.path === '/login' || to.path === '/register') && token) {
    if (userRole === 'admin') {
      return next({ path: '/admin/dashboard' })
    } else {
      return next({ path: '/student/dashboard' })
    }
  }

  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      // 未登录，重定向到登录页
      return next({ path: '/login' })
    } else {
      // 检查角色权限
      const requiredRole = to.meta.role
      if (requiredRole && requiredRole !== userRole) {
      // 角色不匹配，根据实际角色重定向
        if (userRole === 'admin') {
          return next({ path: '/admin/dashboard' })
        } else {
          return next({ path: '/student/dashboard' })
        }
      } else {
        return next()
      }
    }
  } else {
    return next()
  }
})

export default router
