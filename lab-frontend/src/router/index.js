import { createRouter, createWebHistory } from 'vue-router'
import MyLayout from '../layout/MyLayout.vue' // 引入布局

const routes = [
  { path: '/', redirect: '/login' },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  // === 核心修改：使用布局组件包裹业务页面 ===
  {
    path: '/',
    component: MyLayout, // 父路由使用布局
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: 'stock',
        name: 'Stock',
        component: () => import('../views/Stock.vue')
      },
      // === 新增：领用记录页 ===
      {
        path: 'apply-list',
        name: 'ApplyList',
        component: () => import('../views/ApplyList.vue')
      },
      // === 新增：用户管理 ===
      {
        path: 'user-manage',
        name: 'UserManage',
        component: () => import('../views/UserManage.vue')
      },// === 新增：数据统计页 ===
      {
        path: 'admin-stats',
        name: 'AdminStats',
        component: () => import('../views/AdminStats.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router