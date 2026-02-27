<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <span>🧪 实验室管理</span>
      </div>
      <el-menu
        active-text-color="#409EFF"
        background-color="#304156"
        text-color="#bfcbd9"
        :default-active="$route.path"
        router
        class="el-menu-vertical"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>首页</span>
        </el-menu-item>
        
        <el-menu-item index="/stock" v-if="userInfo.role !== 'admin'">
          <el-icon><Box /></el-icon>
          <span>耗材申请</span>
        </el-menu-item>

        <el-menu-item index="/inventory" v-if="userInfo.role === 'admin'">
          <el-icon><Management /></el-icon> <span>库存管理</span>
        </el-menu-item>

        <el-menu-item index="/apply-list">
          <el-icon><Document /></el-icon>
          <span>领用记录</span>
        </el-menu-item>

        <el-menu-item index="/user-manage" v-if="userInfo.role === 'admin'">
          <el-icon><UserFilled /></el-icon> <span>用户管理</span>
        </el-menu-item>

        <el-menu-item index="/admin-stats" v-if="userInfo.role === 'admin'">
          <el-icon><TrendCharts /></el-icon> <span>数据统计</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="breadcrumb">
          <span>首页 / {{ $route.name === 'Stock' ? '耗材管理 / 耗材列表' : '仪表盘' }}</span>
        </div>
        <div class="user-info">
  <el-avatar :size="30" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
  
  <span style="margin-left: 10px; font-size: 14px;">
    {{ userInfo.nickname || '未登录' }} 
    <el-tag size="small" effect="plain" style="margin-left: 5px">
      {{ roleMap[userInfo.role] || userInfo.role }}
    </el-tag>
  </span>
  
  <el-button link type="danger" style="margin-left: 15px;" @click="handleLogout">退出</el-button>
</div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue' // 引入 ref
import { useRouter } from 'vue-router' // 引入路由
import { Odometer, Box, Document, UserFilled ,TrendCharts} from '@element-plus/icons-vue'


const router = useRouter()

// 从 localStorage 读取用户信息
const userInfo = ref(JSON.parse(localStorage.getItem('user_info') || '{}'))

// 角色字典（把英文 role 转成中文显示）
const roleMap = {
  'admin': '管理员',
  'teacher': '导师',
  'student': '学生'
}

// 退出登录逻辑
const handleLogout = () => {
  localStorage.removeItem('user_info') // 清除缓存
  router.push('/login')
}
</script>
<style scoped>
.layout-container {
  height: 100vh;
}
.aside {
  background-color: #304156;
  color: white;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  background-color: #2b3649;
  color: white;
}
.el-menu-vertical {
  border-right: none;
}
.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}
.main-content {
  background-color: #f0f2f5; /* 灰色背景，突出卡片 */
  padding: 20px;
}
</style>