import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getRoomList, getRoomDetail, getAvailableRooms, getSeatsByRoomId, getAvailableSeatsByRoomId } from '../services/room'

// 定义后端返回数据类型
interface ApiResponse {
  code: number
  message: string
  data: any
}

export const useRoomStore = defineStore('room', () => {
  // 状态
  const rooms = ref<any[]>([])
  const currentRoom = ref<any>(null)
  const seats = ref<any[]>([])
  const loading = ref<boolean>(false)
  const error = ref<string | null>(null)

  // 计算属性
  const availableRooms = computed(() => {
    return rooms.value.filter(room => room.status === 'available')
  })

  // 操作
  async function fetchRooms(params?: any) {
    loading.value = true
    error.value = null
    try {
      const response = await getRoomList(params) as unknown as ApiResponse
      if (response.code === 200) {
        rooms.value = response.data
      }
      return response
    } catch (err: any) {
      error.value = err.message || '获取自习室列表失败'
      return { code: 500, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function fetchRoomDetail(roomId: number) {
    loading.value = true
    error.value = null
    try {
      const response = await getRoomDetail(roomId) as unknown as ApiResponse
      if (response.code === 200) {
        currentRoom.value = response.data
      }
      return response
    } catch (err: any) {
      error.value = err.message || '获取自习室详情失败'
      return { code: 500, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function fetchSeats(roomId: number) {
    loading.value = true
    error.value = null
    try {
      const response = await getSeatsByRoomId(roomId) as unknown as ApiResponse
      if (response.code === 200) {
        seats.value = response.data
      }
      return response
    } catch (err: any) {
      error.value = err.message || '获取座位列表失败'
      return { code: 500, message: error.value }
    } finally {
      loading.value = false
    }
  }

  async function fetchAvailableSeats(roomId: number) {
    loading.value = true
    error.value = null
    try {
      const response = await getAvailableSeatsByRoomId(roomId) as unknown as ApiResponse
      if (response.code === 200) {
        return response.data
      }
      return []
    } catch (err: any) {
      error.value = err.message || '获取可用座位失败'
      return []
    } finally {
      loading.value = false
    }
  }

  async function fetchAvailableRoomsList() {
    loading.value = true
    error.value = null
    try {
      const response = await getAvailableRooms() as unknown as ApiResponse
      if (response.code === 200) {
        return response.data
      }
      return []
    } catch (err: any) {
      error.value = err.message || '获取可用自习室失败'
      return []
    } finally {
      loading.value = false
    }
  }

  return {
    rooms,
    currentRoom,
    seats,
    loading,
    error,
    availableRooms,
    fetchRooms,
    fetchRoomDetail,
    fetchSeats,
    fetchAvailableSeats,
    fetchAvailableRoomsList
  }
})
