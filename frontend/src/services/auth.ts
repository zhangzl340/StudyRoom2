import service from './axios'

// 登录
export async function login(username: string, password: string) {
  return service({
    url: '/auth/login',
    method: 'post',
    params: {
      username,
      password
    }
  })
}

// 注册
export async function register(userData: any) {
  return service({
    url: '/auth/register',
    method: 'post',
    data: userData
  })
}

// 刷新令牌
export async function refreshToken(refreshToken: string) {
  return service({
    url: '/auth/refresh',
    method: 'post',
    params: {
      refreshToken
    }
  })
}

// 修改密码
export async function changePassword(userId: number, oldPassword: string, newPassword: string) {
  return service({
    url: '/auth/change-password',
    method: 'post',
    params: {
      userId,
      oldPassword,
      newPassword
    }
  })
}

// 忘记密码请求
export async function forgotPasswordRequest(email: string) {
  return service({
    url: '/auth/reset-password/request',
    method: 'post',
    params: {
      email
    }
  })
}

// 重置密码
export async function resetPassword(email: string, newPassword: string, verificationCode: string) {
  return service({
    url: '/auth/reset-password',
    method: 'post',
    params: {
      email,
      newPassword,
      verificationCode
    }
  })
}

// 实名认证
export async function verifyIdentity(userId: number, realName: string, idCard: string) {
  return service({
    url: '/auth/verify-identity',
    method: 'post',
    params: {
      userId,
      realName,
      idCard
    }
  })
}

// 获取当前用户信息
export async function getCurrentUserInfo() {
  return service({
    url: '/auth/me',
    method: 'get'
  })
}

// 注销
export async function logout() {
  return service({
    url: '/auth/logout',
    method: 'post'
  })
}
