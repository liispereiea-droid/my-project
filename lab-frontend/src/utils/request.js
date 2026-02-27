import axios from 'axios'
import { ElMessage } from 'element-plus'

// 1. 创建 axios 实例
const request = axios.create({
  baseURL: 'http://localhost:8080', 
  timeout: 5000
})

// 2. 请求拦截器 (自动带 Token)
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = token
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 3. 响应拦截器 (=== 核心修改点 ===)
request.interceptors.response.use(
  response => {
    // ❌ 以前是 return response.data (太勤快了，拆包了)
    // ✅ 现在改成 return response (保持原样，交给页面去拆)
    return response
  },
  error => {
    // 处理 401
    if (error.response && error.response.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('user_info')
      location.href = '/' 
    } else {
      ElMessage.error(error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default request