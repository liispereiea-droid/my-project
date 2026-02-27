<template>
  <div class="dashboard-container">
    <div class="welcome-card">
      <img src="https://img.freepik.com/free-vector/science-lab-concept-illustration_114360-3882.jpg" alt="Lab" class="welcome-img">
      
      <h1>🎉 欢迎回来，{{ nickname }}</h1>
      <el-tag size="large" effect="dark" style="margin: 10px 0;">当前身份：{{ roleName }}</el-tag>
      
      <p class="sub-text">您可以点击左侧菜单进行耗材查询、领用申请等操作。</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const nickname = ref('')
const roleName = ref('')

onMounted(() => {
  // 从缓存读取用户信息
  const userStr = localStorage.getItem('user_info')
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      nickname.value = user.nickname || user.username
      
      // 翻译角色
      const roleMap = { 'admin': '系统管理员', 'teacher': '导师', 'student': '学生' }
      roleName.value = roleMap[user.role] || user.role
    } catch (e) {
      console.error('用户信息解析失败', e)
    }
  }
})
</script>

<style scoped>
.dashboard-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 80vh;
  background-color: white;
  border-radius: 10px;
}
.welcome-card {
  text-align: center;
}
.welcome-img {
  width: 300px;
  margin-bottom: 20px;
}
h1 {
  color: #303133;
  font-size: 24px;
}
.sub-text {
  color: #909399;
  margin-top: 10px;
}
</style>