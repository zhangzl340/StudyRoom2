import service from './axios'

// 获取自习室列表
export async function getRoomList(params?: any) {
  return service({
    url: '/room/list',
    method: 'get',
    params
  })
}

// 获取自习室详情
export async function getRoomDetail(roomId: number) {
  return service({
    url: `/room/detail/${roomId}`,
    method: 'get'
  })
}

// 获取自习室座位列表
export async function getSeatsByRoomId(roomId: number) {
  return service({
    url: `/room/${roomId}/seats`,
    method: 'get'
  })
}

// 获取自习室可用座位
export async function getAvailableSeatsByRoomId(roomId: number) {
  return service({
    url: `/room/${roomId}/available-seats`,
    method: 'get'
  })
}

// 获取自习室实时状态
export async function getRoomStatus(roomId: number) {
  return service({
    url: `/room/status/${roomId}`,
    method: 'get'
  })
}

// 获取可用的自习室
export async function getAvailableRooms() {
  return service({
    url: '/room/available',
    method: 'get'
  })
}

// 创建自习室
export async function createRoom(roomData: any) {
  return service({
    url: '/room/create',
    method: 'post',
    data: roomData
  })
}

// 更新自习室
export async function updateRoom(roomId: number, roomData: any) {
  return service({
    url: `/room/update/${roomId}`,
    method: 'put',
    data: roomData
  })
}

// 删除自习室
export async function deleteRoom(roomId: number) {
  return service({
    url: `/room/delete/${roomId}`,
    method: 'delete'
  })
}

// 更新自习室状态
export async function updateRoomStatus(roomId: number, status: string) {
  return service({
    url: `/room/status/${roomId}`,
    method: 'put',
    params: {
      status
    }
  })
}

// 创建座位
export async function createSeats(roomId: number, seats: any[]) {
  return service({
    url: '/room/seats/create',
    method: 'post',
    params: {
      roomId
    },
    data: seats
  })
}

// 更新座位状态
export async function updateSeatStatus(seatId: number, status: string) {
  return service({
    url: `/room/seat/status/${seatId}`,
    method: 'put',
    params: {
      status
    }
  })
}

// 更新座位布局
export async function updateSeatLayout(roomId: number, seats: any[]) {
  return service({
    url: '/room/seat/layout',
    method: 'put',
    params: {
      roomId
    },
    data: seats
  })
}

// 批量导入座位
export async function importSeats(roomId: number, seats: any[]) {
  return service({
    url: '/room/seat/import',
    method: 'post',
    params: {
      roomId
    },
    data: seats
  })
}

// 检查座位可用性
export async function checkSeatAvailability(roomId: number, seatId: number, startTime: string, endTime: string) {
  return service({
    url: '/room/seat/availability',
    method: 'get',
    params: {
      roomId,
      seatId,
      startTime,
      endTime
    }
  })
}
