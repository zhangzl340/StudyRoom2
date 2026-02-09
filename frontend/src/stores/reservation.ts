import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getReservationList, createReservation, cancelReservation, getUpcomingReservations, getTodayReservations, signInReservation, signOutReservation } from '../services/reservation'

// 定义后端返回数据类型
interface ApiResponse {
  code: number
  message: string
  data: any
}

export const useReservationStore = defineStore('reservation', () => {
  // 状态
  const reservations = ref<any[]>([])
  const upcomingReservations = ref<any[]>([])
  const todayReservations = ref<any[]>([])
  const loading = ref<boolean>(false)
  const error = ref<string | null>(null)

  // 计算属性
  const activeReservations = computed(() => {
    return reservations.value.filter(res => res.status === 'active')
  })

  const completedReservations = computed(() => {
    return reservations.value.filter(res => res.status === 'completed')
  })

  const cancelledReservations = computed(() => {
    return reservations.value.filter(res => res.status === 'cancelled')
  })

  // 操作
  async function fetchReservations(params?: any) {
    loading.value = true
    error.value = null
    try {
      const response = await getReservationList(params) as unknown as ApiResponse
      if (response.code === 200) {
        reservations.value = response.data
      }
      return response
    } catch (err: any) {
      error.value = err.message || '获取预约列表失败'
      return { code: 500, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function createReservationRequest(reservationData: any) {
    loading.value = true
    error.value = null
    try {
      const response = await createReservation(reservationData) as unknown as ApiResponse
      if (response.code === 200) {
        // 添加到预约列表
        reservations.value.unshift(response.data)
      }
      return response
    } catch (err: any) {
      error.value = err.message || '创建预约失败'
      return { code: 500, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function cancelReservationRequest(reservationId: number) {
    loading.value = true
    error.value = null
    try {
      const response = await cancelReservation(reservationId) as unknown as ApiResponse
      if (response.code === 200) {
        // 更新预约状态
        const index = reservations.value.findIndex(res => res.id === reservationId)
        if (index !== -1) {
          reservations.value[index].status = 'cancelled'
        }
      }
      return response
    } catch (err: any) {
      error.value = err.message || '取消预约失败'
      return { code: 500, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function fetchUpcomingReservations(userId: number) {
    loading.value = true
    error.value = null
    try {
      const response = await getUpcomingReservations(userId) as unknown as ApiResponse
      if (response.code === 200) {
        upcomingReservations.value = response.data
      }
      return response
    } catch (err: any) {
      error.value = err.message || '获取即将开始的预约失败'
      return { code: 500, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function fetchTodayReservationsRequest(userId: number) {
    loading.value = true
    error.value = null
    try {
      const response = await getTodayReservations(userId) as unknown as ApiResponse
      if (response.code === 200) {
        todayReservations.value = response.data
      }
      return response
    } catch (err: any) {
      error.value = err.message || '获取今日预约失败'
      return { code: 500, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function signInReservationRequest(reservationId: number) {
    loading.value = true
    error.value = null
    try {
      const response = await signInReservation(reservationId) as unknown as ApiResponse
      if (response.code === 200) {
        // 更新预约状态
        const index = reservations.value.findIndex(res => res.id === reservationId)
        if (index !== -1) {
          reservations.value[index].reservationStatus = '使用中'
          reservations.value[index].signInTime = new Date().toISOString().replace('T', ' ').split('.')[0]
        }
      }
      return response
    } catch (err: any) {
      error.value = err.message || '签到失败'
      return { code: 500, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function signOutReservationRequest(reservationId: number) {
    loading.value = true
    error.value = null
    try {
      // 注意：这里需要获取checkinId，而不是直接使用reservationId
      // 暂时使用reservationId作为checkinId，需要后端支持
      const response = await signOutReservation(reservationId) as unknown as ApiResponse
      if (response.code === 200) {
        // 更新预约状态
        const index = reservations.value.findIndex(res => res.id === reservationId)
        if (index !== -1) {
          reservations.value[index].reservationStatus = '完成预约'
          reservations.value[index].signOutTime = new Date().toISOString().replace('T', ' ').split('.')[0]
        }
      }
      return response
    } catch (err: any) {
      error.value = err.message || '签退失败'
      return { code: 500, message: error.value }
    } finally {
      loading.value = false
    }
  }

  return {
    reservations,
    upcomingReservations,
    todayReservations,
    loading,
    error,
    activeReservations,
    completedReservations,
    cancelledReservations,
    fetchReservations,
    createReservationRequest,
    cancelReservationRequest,
    fetchUpcomingReservations,
    fetchTodayReservationsRequest,
    signInReservationRequest,
    signOutReservationRequest
  }
})
