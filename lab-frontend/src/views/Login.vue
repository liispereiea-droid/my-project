<template>
  <div class="login-container">
    <div class="login-box">
      <h2>🔬 实验室耗材智能补给系统</h2>
      
      <el-tabs v-model="activeTab" stretch>
        
        <el-tab-pane label="用户登录" name="login">
          <el-form :model="loginForm" label-width="0">
            <el-form-item>
              <el-input v-model="loginForm.username" placeholder="请输入账号" :prefix-icon="User" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
            </el-form-item>
            <el-button type="primary" class="w-100" @click="handleLogin" :loading="loading">立即登录</el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="学生注册" name="register">
          <el-alert title="注意：必须输入正确的导师ID才能注册成功" type="warning" :closable="false" class="mb-10" />
          <el-form :model="regForm" label-width="0" class="mt-10">
            <el-form-item>
              <el-input v-model="regForm.username" placeholder="设置账号" :prefix-icon="User" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="regForm.password" type="password" placeholder="设置密码" :prefix-icon="Lock" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="regForm.nickname" placeholder="真实姓名" :prefix-icon="Postcard" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="regForm.teacherId" placeholder="导师ID (请询问导师)" :prefix-icon="School" />
            </el-form-item>
            <el-button type="success" class="w-100" @click="handleRegister" :loading="loading">注册账号</el-button>
          </el-form>
        </el-tab-pane>

      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock, Postcard, School } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '../utils/request'

const router = useRouter()
const activeTab = ref('login')
const loading = ref(false)

// 后端接口地址 (注意：这里直接写死方便测试，生产环境应用 request.js 封装)
const API_URL = 'http://localhost:8080/api/user'

// 登录数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 注册数据
const regForm = reactive({
  username: '',
  password: '',
  nickname: '',
  teacherId: ''
})

// --- 登录逻辑 ---
const handleLogin = async () => {
  if(!loginForm.username || !loginForm.password) return ElMessage.error('请填写完整')
  
  loading.value = true
  try {
    const res = await request.post(`${API_URL}/login`, loginForm)
    
    // 修改点：判断 code === 200
    if (res.data.code === 200) {
      ElMessage.success('登录成功')
      const result = res.data.data

    // 1. 存 Token (注意这里是 result.token)
      localStorage.setItem('token', result.token)
      // 2. 存用户信息 (注意这里是 result.data)
      localStorage.setItem('user_info', JSON.stringify(result.data))
      
      
      router.push('/dashboard')
    } else {
      ElMessage.error(res.data.msg || '登录失败')
    }
  } catch (error) {
    ElMessage.error('服务器连接失败')
  } finally {
    loading.value = false
  }
}

// --- 注册逻辑 ---
const handleRegister = async () => {
  if(!regForm.teacherId) return ElMessage.warning('导师ID不能为空')
  
  loading.value = true
  try {
    const res = await request.post(`${API_URL}/register`, regForm)
   if (res.data && res.data.code === 200) {
      ElMessage.success('注册成功！请切换到登录页登录')
      activeTab.value = 'login' // 自动切回登录 Tab
    }else {
      ElMessage.error(res.data.msg || '注册失败') // 显示错误 (如：导师ID不存在)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1c92d2 0%, #f2fcfe 100%); /* 科技蓝背景 */
}
.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}
h2 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
}
.w-100 {
  width: 100%;
}
.mb-10 { margin-bottom: 10px; }
.mt-10 { margin-top: 10px; }
</style>