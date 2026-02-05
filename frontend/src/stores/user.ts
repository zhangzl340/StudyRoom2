import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserList, getUserDetail, createUser, updateUser, deleteUser, updateUserStatus } from '../services/user'

// 定义后端返回数据类型
interface ApiResponse {
  code: number
  message: string
  data: any
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const users = ref<any[]>([])
  const currentUser = ref<any>(null)
  const loading = ref<boolean>(false)
  const error = ref<string | null>(null)

  // 计算属性
  const activeUsers = computed(() => {
    return users.value.filter(user => user.status === '1' || user.status === 'active')
  })

  const inactiveUsers = computed(() => {
    return users.value.filter(user => user.status === '0' || user.status === 'inactive')
  })

  // 操作
  async function fetchUsers(params?: any) {
    loading.value = true
    error.value = null
    try {
      const response = await getUserList(params) as unknown as ApiResponse
      if (response.code === 200) {
        users.value = response.data
      }
      return response
    } catch (err: any) {
      error.value = err.message || '获取用户列表失败'
      return { code: 500, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function getUsers() {
    loading.value = true
    error.value = null
    try {
      const response = await fetchUsers()
      return response
    } finally {
      loading.value = false
    }
  }

  async function fetchUserDetail(userId: number) {
    loading.value = true
    error.value = null
    try {
      const response = await getUserDetail(userId) as unknown as ApiResponse
      if (response.code === 200) {
        currentUser.value = response.data
      }
      return response
    } catch (err: any) {
      error.value = err.message || '获取用户详情失败'
      return { code: 500, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function createUserAction(userData: any) {
    loading.value = true
    error.value = null
    try {
      const response = await createUser(userData) as unknown as ApiResponse
      if (response.code === 200) {
        // 重新获取用户列表
        await fetchUsers()
      }
      return response
    } catch (err: any) {
      error.value = err.message || '创建用户失败'
      return { code: 500, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function updateUserAction(userId: number, userData: any) {
    loading.value = true
    error.value = null
    try {
      const response = await updateUser(userId, userData) as unknown as ApiResponse
      if (response.code === 200) {
        // 重新获取用户列表
        await fetchUsers()
      }
      return response
    } catch (err: any) {
      error.value = err.message || '更新用户失败'
      return { code: 500, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function deleteUserAction(userId: number) {
    loading.value = true
    error.value = null
    try {
      const response = await deleteUser(userId) as unknown as ApiResponse
      if (response.code === 200) {
        // 重新获取用户列表
        await fetchUsers()
      }
      return response
    } catch (err: any) {
      error.value = err.message || '删除用户失败'
      return { code: 500, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function updateUserStatusAction(userId: number, status: string) {
    loading.value = true
    error.value = null
    try {
      const response = await updateUserStatus(userId, status) as unknown as ApiResponse
      if (response.code === 200) {
        // 重新获取用户列表
        await fetchUsers()
      }
      return response
    } catch (err: any) {
      error.value = err.message || '更新用户状态失败'
      return { code: 500, message: error.value }
    } finally {
      loading.value = false
    }
  }

  return {
    users,
    currentUser,
    loading,
    error,
    activeUsers,
    inactiveUsers,
    fetchUsers,
    getUsers,
    fetchUserDetail,
    createUser: createUserAction,
    updateUser: updateUserAction,
    deleteUser: deleteUserAction,
    updateUserStatus: updateUserStatusAction
  }
})
