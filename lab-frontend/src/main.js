import { createApp } from 'vue'
import App from './App.vue'
import router from './router' // 稍后创建
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css' // 引入样式
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import axios from 'axios'

const app = createApp(App)

// === 核心修改 1: 配置 Axios 全局拦截器 ===
// 请求拦截器：每次发请求前，自动带上 Token
axios.interceptors.request.use(config => {
  // 从 localStorage 获取 token
  const token = localStorage.getItem('token')
  if (token) {
    // 如果有 token，加到请求头里
    // header 的名字要和后端 LoginInterceptor 里取的一样 ('Authorization')
    config.headers['Authorization'] = token
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 响应拦截器：如果后端返回 401 (Token 过期或不对)，强制退出
axios.interceptors.response.use(response => {
  return response
}, error => {
  if (error.response && error.response.status === 401) {
    // 清除本地数据
    localStorage.removeItem('token')
    localStorage.removeItem('user_info')
    // 强制跳转登录页
    router.push('/login')
  }
  return Promise.reject(error)
})

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router)
app.use(ElementPlus)
app.mount('#app')