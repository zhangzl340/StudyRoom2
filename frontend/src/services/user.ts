import service from './axios'

// 获取用户列表
export async function getUserList(params?: any) {
  return service({
    url: '/user/list',
    method: 'get',
    params
  })
}

// 获取用户详情
export async function getUserDetail(userId: number) {
  return service({
    url: `/user/detail/${userId}`,
    method: 'get'
  })
}

// 创建用户
export async function createUser(userData: any) {
  return service({
    url: '/user/create',
    method: 'post',
    data: userData
  })
}

// 更新用户
export async function updateUser(userId: number, userData: any) {
  return service({
    url: `/user/update/${userId}`,
    method: 'put',
    data: userData
  })
}

// 删除用户
export async function deleteUser(userId: number) {
  return service({
    url: `/user/delete/${userId}`,
    method: 'delete'
  })
}

// 更新用户状态
export async function updateUserStatus(userId: number, status: string) {
  return service({
    url: `/user/status/${userId}`,
    method: 'patch',
    params: {
      status
    }
  })
}

// 重置用户密码
export async function resetUserPassword(userId: number, newPassword: string) {
  return service({
    url: `/user/reset-password/${userId}`,
    method: 'post',
    params: {
      newPassword
    }
  })
}

// 调整用户信用分
export async function adjustUserCredit(userId: number, score: number) {
  return service({
    url: `/user/adjust-credit/${userId}`,
    method: 'post',
    params: {
      score
    }
  })
}

// 获取个人信息
export async function getProfile(userId: number) {
  return service({
    url: '/user/profile',
    method: 'get',
    params: {
      userId
    }
  })
}

// 更新个人信息
export async function updateProfile(userId: number, userData: any) {
  return service({
    url: '/user/profile',
    method: 'put',
    params: {
      userId
    },
    data: userData
  })
}
