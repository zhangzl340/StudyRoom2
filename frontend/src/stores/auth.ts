import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getCurrentUserInfo } from '../services/auth'

// 定义后端返回数据类型
interface ApiResponse {
  code: number
  message: string
  data: any
}

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<any>(null)
  const loading = ref<boolean>(false)
  const error = ref<string | null>(null)

  // 计算属性
  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'admin')
  const isStudent = computed(() => userInfo.value?.role === 'student')

  // 操作
  async function login(username: string, password: string) {
    loading.value = true
    error.value = null
    try {
      const response = await loginApi(username, password) as unknown as ApiResponse
      if (response.code === 200) {
        token.value = response.data.token
        userInfo.value = response.data.user
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('role', response.data.user.role)
        localStorage.setItem('userId', response.data.user.id.toString())
        return response
      } else {
        error.value = response.message
        return response
      }
    } catch (err: any) {
      error.value = err.message || '登录失败'
      return { code: 500, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function register(userData: any) {
    loading.value = true
    error.value = null
    try {
      const response = await registerApi(userData) as unknown as ApiResponse
      if (response.code !== 200) {
        error.value = response.message
      }
      return response
    } catch (err: any) {
      error.value = err.message || '注册失败'
      return { code: 500, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    localStorage.removeItem('userId')
  }

  async function fetchUserInfo() {
    if (!token.value) return
    loading.value = true
    error.value = null
    try {
      const response = await getCurrentUserInfo() as unknown as ApiResponse
      if (response.code === 200) {
        userInfo.value = response.data
      }
      return response
    } catch (err: any) {
      error.value = err.message || '获取用户信息失败'
      return { code: 500, message: error.value }
    } finally {
      loading.value = false
    }
  }

  return {
    token,
    userInfo,
    loading,
    error,
    isAuthenticated,
    isAdmin,
    isStudent,
    login,
    register,
    logout,
    fetchUserInfo
  }
})
