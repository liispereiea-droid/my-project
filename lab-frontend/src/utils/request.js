import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const service = axios.create({
  baseURL: 'http://localhost:8080', // 指向你的后端地址
  timeout: 5000 // 请求超时时间
})

// 响应拦截器：统一处理错误
service.interceptors.response.use(
  response => {
    return response
  },
  error => {
    console.log('err' + error)
    ElMessage({
      message: error.message || '网络连接失败',
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

export default service