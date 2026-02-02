import axios from 'axios'
import type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api', // 后端API基础路径
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  },
  withCredentials: true, // 关键：开启跨域携带凭证
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从本地存储获取token
    const token = localStorage.getItem('token')
    if (token) {
      // 设置Authorization请求头
      config.headers = config.headers || {}
      config.headers['Authorization'] = token
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response
    return data
  },
  (error) => {
    console.error('响应错误:', error)
    
    // 处理常见错误
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 401:
          // 未授权，清除token并跳转到登录页
          localStorage.removeItem('token')
          localStorage.removeItem('role')
          localStorage.removeItem('userId')
          window.location.href = '/login'
          break
        case 403:
          // 禁止访问
          console.error('禁止访问:', data.message || '没有权限访问该资源')
          break
        case 404:
          // 资源不存在
          console.error('资源不存在:', data.message || '请求的资源不存在')
          break
        case 500:
          // 服务器错误
          console.error('服务器错误:', data.message || '服务器内部错误')
          break
        default:
          console.error('请求失败:', data.message || '未知错误')
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      console.error('网络错误:', '服务器无响应，请检查网络连接')
    } else {
      // 请求配置错误
      console.error('请求配置错误:', error.message)
    }
    
    return Promise.reject(error)
  }
)

export default service
