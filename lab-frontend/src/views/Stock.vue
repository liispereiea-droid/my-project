<template>
  <div class="app-container" style="padding: 20px;">
    
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>📦 耗材申请列表</span>
          <el-tag type="info">请展开分类查找并申请</el-tag>
        </div>
      </template>

      <el-table :data="tableData" border stripe style="width: 100%" row-key="id" :expand-row-keys="expandKeys">
        
        <el-table-column type="expand">
          <template #default="props">
            <div style="padding: 10px 50px; background-color: #fcfcfc;">
              
              <el-table :data="getConsumablesByCategory(props.row.id)" size="small" border>
                <el-table-column prop="consumableName" label="耗材名称" />
                <el-table-column prop="spec" label="规格" width="120" />
                <el-table-column prop="currentStock" label="当前库存" width="120">
                  <template #default="subScope">
                     <span :style="{ color: subScope.row.currentStock < subScope.row.safetyStock ? 'red' : 'green', fontWeight: 'bold' }">
                       {{ subScope.row.currentStock }} {{ subScope.row.unit }}
                     </span>
                     <el-tag v-if="subScope.row.currentStock < subScope.row.safetyStock" type="danger" size="small" effect="dark">缺货</el-tag>
                  </template>
                </el-table-column>
                
                <el-table-column label="操作" width="150" align="center">
                  <template #default="subScope">
                    <el-button type="primary" size="small" @click="openApplyDialog(subScope.row)" :disabled="subScope.row.currentStock <= 0">
                      申请领用
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>

              <div v-if="getConsumablesByCategory(props.row.id).length === 0" style="color:#999; text-align:center; padding:10px;">
                (暂无数据)
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="categoryName" label="分类名称" width="200">
           <template #default="scope">
             <el-tag effect="dark" size="large">{{ scope.row.categoryName }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="remark" label="分类描述" />
      </el-table>
    </el-card>

    <el-dialog v-model="applyDialogVisible" title="📝 填写领用申请" width="500px">
      <el-form :model="applyForm" label-width="100px">
        <el-form-item label="申请耗材"><el-input v-model="applyForm.consumableName" disabled /></el-form-item>
        <el-form-item label="当前库存"><el-tag type="info">{{ currentStock }}</el-tag></el-form-item>
        <el-form-item label="申请数量"><el-input-number v-model="applyForm.applyCount" :min="1" :max="currentStock" /></el-form-item>
        <el-form-item label="申请用途"><el-input v-model="applyForm.reason" type="textarea" placeholder="请简述用途" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '../utils/request' // 核心修复：使用相对路径引入 request
import { ElMessage } from 'element-plus'

const tableData = ref([])       // 存分类
const allConsumables = ref([])  // 存耗材
const expandKeys = ref([])      // 默认展开的行

// 申请相关
const applyDialogVisible = ref(false)
const currentStock = ref(0)
// 注意：consumableName 对应实体类
const applyForm = reactive({ userId: '', userName: '', labId: '', consumableId: '', consumableName: '', applyCount: 1, reason: '' })

// 1. 加载数据
const loadData = async () => {
  try {
    const [catRes, conRes] = await Promise.all([
      request.get('/api/category/list'),
      request.get('/api/consumable/list?labId=1')
    ])
    
    // 解包数据 (如果 request.js 已经解包，这里直接取；如果没有，取 .data)
    // 假设 request.js 拦截器返回的是 response，则需要 .data
    // 如果你改了 request.js 返回 response，下面写法兼容性最强：
    
    const categories = catRes.data ? catRes.data : catRes
    const consumables = conRes.data ? conRes.data : conRes

    if (categories.code === 200) tableData.value = categories.data
    if (consumables.code === 200) allConsumables.value = consumables.data
    
    if(tableData.value.length > 0) {
        expandKeys.value = [tableData.value[0].id]
    }
  } catch (error) { console.error(error) }
}

// 2. 前端过滤：把耗材归类 (根据 categoryId)
const getConsumablesByCategory = (categoryId) => {
  return allConsumables.value.filter(item => item.categoryId === categoryId)
}

// 3. 打开申请弹窗
const openApplyDialog = (row) => {
  const userStr = localStorage.getItem('user_info')
  if (!userStr) return ElMessage.warning('请先登录')
  const user = JSON.parse(userStr)

  applyForm.userId = user.id; 
  applyForm.userName = user.nickname; 
  applyForm.labId = user.labId
  applyForm.consumableId = row.id; 
  // 核心修复：这里取 consumableName
  applyForm.consumableName = row.consumableName 
  applyForm.applyCount = 1; 
  applyForm.reason = ''
  
  currentStock.value = row.currentStock
  applyDialogVisible.value = true
}

// 4. 提交申请
const submitApply = async () => {
  if (!applyForm.reason) return ElMessage.warning('请填写申请用途')
  try {
    const res = await request.post('/api/apply/submit', applyForm)
    
    // 兼容处理：检查 res.data 是否存在
    const result = res.data ? res.data : res
    
    if (result.code === 200) {
      ElMessage.success('申请提交成功')
      applyDialogVisible.value = false
      loadData() // 刷新库存
    } else {
      // 核心修复：修正拼写 es -> res, 并处理 msg
      ElMessage.error(result.msg || '提交失败')
    }
  } catch (error) { 
    console.error(error)
    ElMessage.error('网络错误') 
  }
}

onMounted(() => { loadData() })
</script>