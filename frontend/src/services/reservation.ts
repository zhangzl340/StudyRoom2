import service from './axios'

// 获取预约列表
export async function getReservationList(params?: any) {
  return service({
    url: '/reservation/list',
    method: 'get',
    params
  })
}

// 获取预约详情
export async function getReservationDetail(reservationId: number) {
  return service({
    url: `/reservation/detail/${reservationId}`,
    method: 'get'
  })
}

// 创建预约
export async function createReservation(reservationData: any) {
  return service({
    url: '/reservation/create',
    method: 'post',
    data: reservationData
  })
}

// 更新预约
export async function updateReservation(reservationId: number, reservationData: any) {
  return service({
    url: `/reservation/update/${reservationId}`,
    method: 'put',
    data: reservationData
  })
}

// 取消预约
export async function cancelReservation(reservationId: number) {
  return service({
    url: '/reservation/cancel',
    method: 'post',
    params: {
      id: reservationId
    }
  })
}

// 确认预约
export async function confirmReservation(reservationId: number) {
  return service({
    url: '/reservation/check',
    method: 'post',
    params: {
      id: reservationId
    }
  })
}

// 计算预约费用
export async function calculateReservationFee(reservationId: number) {
  return service({
    url: '/reservation/fee',
    method: 'post',
    params: {
      id: reservationId
    }
  })
}

// 签到
export async function signInReservation(reservationId: number) {
  return service({
    url: '/checkin/in',
    method: 'post',
    params: {
      reservationId: reservationId,
      userId: localStorage.getItem('userId') ? parseInt(localStorage.getItem('userId')!) : null,
      method: 'manual'
    }
  })
}

// 签退
export async function signOutReservation(checkinId: number) {
  return service({
    url: '/checkin/out',
    method: 'post',
    params: {
      id: checkinId
    }
  })
}

// 暂离
export async function leaveReservation(checkinId: number) {
  return service({
    url: '/checkin/leave',
    method: 'post',
    params: {
      id: checkinId
    }
  })
}

// 暂离返回
export async function returnFromLeaveReservation(checkinId: number) {
  return service({
    url: '/checkin/return',
    method: 'post',
    params: {
      id: checkinId
    }
  })
}

// 获取即将开始的预约
export async function getUpcomingReservations(userId: number) {
  return service({
    url: '/reservation/upcoming',
    method: 'get',
    params: {
      userId
    }
  })
}

// 获取今日预约
export async function getTodayReservations(userId: number) {
  return service({
    url: '/reservation/today',
    method: 'get',
    params: {
      userId
    }
  })
}

// 检查预约冲突
export async function checkAvailability(roomId: number, seatId: number, startTime: string, endTime: string) {
  return service({
    url: '/reservation/availability',
    method: 'get',
    params: {
      roomId,
      seatId,
      startTime,
      endTime
    }
  })
}

// 获取今日预约统计
export async function getReservationStatistics() {
  return service({
    url: '/reservation/statistics',
    method: 'get'
  })
}
