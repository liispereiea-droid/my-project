<template>
  <div class="app-container">
    
    <el-card class="filter-container" shadow="never">
      <div class="header-title">
        <el-icon><Search /></el-icon> <span>筛选搜索</span>
      </div>
      <div class="filter-content">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="耗材名称：">
            <el-input v-model="searchForm.name" placeholder="请输入耗材名称" clearable @clear="loadData" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询结果</el-button>
            <el-button v-if="userInfo.role !== 'student'" type="success" @click="openDialog()">+ 入库登记</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card class="table-container" shadow="never">
      <template #header>
        <div class="table-header">
          <div class="left-panel"><el-icon><List /></el-icon> <span>数据列表</span></div>
        </div>
      </template>

      <el-table :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column label="耗材名称" min-width="150">
          <template #default="scope">
            <div style="font-weight: bold;">{{ scope.row.name }}</div>
            <div style="font-size: 12px; color: #999;">规格: {{ scope.row.spec }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100" align="center">
           <template #default="scope"><el-tag size="small">{{ scope.row.categoryName }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="price" label="单价" width="100" align="center">
           <template #default="scope">¥{{ scope.row.price }}</template>
        </el-table-column>
        
        <el-table-column label="库存状态" width="140" align="center">
          <template #default="scope">
             <div v-if="scope.row.currentStock < scope.row.safetyStock" style="color:red">
                <el-icon><Warning /></el-icon> 缺货 ({{ scope.row.currentStock }})
             </div>
             <div v-else style="color:green">
                <el-icon><Check /></el-icon> 充足 ({{ scope.row.currentStock }})
             </div>
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="60" align="center" />

        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button v-if="userInfo.role === 'student'" type="primary" size="small" @click="openApplyDialog(scope.row)">申请领用</el-button>
            
            <div v-else>
               <el-button type="primary" link size="small" @click="openDialog(scope.row)">编辑</el-button>
               <el-popconfirm title="确定删除该耗材吗？" @confirm="handleDelete(scope.row.id)">
                  <template #reference>
                    <el-button type="danger" link size="small">删除</el-button>
                  </template>
               </el-popconfirm>
               <el-button type="info" link size="small" @click="openApplyDialog(scope.row)">领用</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="applyDialogVisible" title="📝 填写领用申请" width="500px">
      <el-form :model="applyForm" label-width="100px">
        <el-form-item label="申请耗材"><el-input v-model="applyForm.consumableName" disabled /></el-form-item>
        <el-form-item label="当前库存"><el-tag type="info">{{ currentStock }}</el-tag></el-form-item>
        <el-form-item label="申请数量"><el-input-number v-model="applyForm.applyCount" :min="1" :max="currentStock" /></el-form-item>
        <el-form-item label="申请用途"><el-input v-model="applyForm.reason" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="formDialogVisible" :title="stockForm.id ? '编辑耗材信息' : '耗材入库登记'" width="500px">
      <el-form :model="stockForm" label-width="100px">
        <el-form-item label="耗材名称">
          <el-input v-model="stockForm.name" placeholder="例如：无水乙醇" />
        </el-form-item>
        <el-form-item label="耗材分类">
           <el-select v-model="stockForm.categoryName" placeholder="请选择" style="width:100%">
              <el-option label="化学试剂" value="化学试剂" />
              <el-option label="玻璃器皿" value="玻璃器皿" />
              <el-option label="实验仪器" value="实验仪器" />
              <el-option label="办公用品" value="办公用品" />
           </el-select>
        </el-form-item>
        <el-form-item label="规格型号">
          <el-input v-model="stockForm.spec" placeholder="例如：500ml/瓶" />
        </el-form-item>
        <el-form-item label="计量单位">
          <el-input v-model="stockForm.unit" placeholder="例如：瓶、个、盒" />
        </el-form-item>
        <el-form-item label="当前库存">
          <el-input-number v-model="stockForm.currentStock" :min="0" />
        </el-form-item>
        <el-form-item label="安全库存线">
          <el-input-number v-model="stockForm.safetyStock" :min="0" />
          <div style="font-size:12px; color:#999">低于此数量将触发红色预警</div>
        </el-form-item>
        <el-form-item label="单价">
          <el-input-number v-model="stockForm.price" :precision="2" :step="0.1" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStockForm">保存信息</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, List, Warning, Check } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}')
const searchForm = reactive({ name: '' })
const tableData = ref([])

// --- 申请相关 ---
const applyDialogVisible = ref(false)
const currentStock = ref(0)
const applyForm = reactive({
  userId: '', userName: '', labId: '', consumableId: '', consumableName: '', applyCount: 1, reason: ''
})

// --- 入库/编辑相关 ---
const formDialogVisible = ref(false)
const stockForm = reactive({
  id: null, name: '', categoryName: '', spec: '', unit: '', currentStock: 0, safetyStock: 10, price: 0
})

// 加载列表
const loadData = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/consumable/list', {
      params: { labId: 1, name: searchForm.name }
    })
    tableData.value = res.data
  } catch (error) { console.error(error) }
}

// 打开申请弹窗
const openApplyDialog = (row) => {
  const userStr = localStorage.getItem('user_info')
  if (!userStr) return ElMessage.warning('请先登录')
  const user = JSON.parse(userStr)
  applyForm.userId = user.id; applyForm.userName = user.nickname; applyForm.labId = user.labId
  applyForm.consumableId = row.id; applyForm.consumableName = row.name
  applyForm.applyCount = 1; applyForm.reason = ''
  currentStock.value = row.currentStock
  applyDialogVisible.value = true
}

// 提交申请
const submitApply = async () => {
  if (!applyForm.reason) return ElMessage.warning('请填写申请用途')
  try {
    const res = await axios.post('http://localhost:8080/api/apply/submit', applyForm)
    if (res.data === 'success') {
      ElMessage.success('申请提交成功')
      applyDialogVisible.value = false
      loadData() // 刷新库存显示
    } else ElMessage.error('提交失败')
  } catch (error) { ElMessage.error('网络错误') }
}

// --- 打开入库/编辑弹窗 ---
const openDialog = (row = null) => {
  if (row) {
    // 编辑模式：回显数据
    Object.assign(stockForm, row)
  } else {
    // 新增模式：重置数据
    stockForm.id = null
    stockForm.name = ''
    stockForm.categoryName = '化学试剂'
    stockForm.spec = ''
    stockForm.unit = '个'
    stockForm.currentStock = 100
    stockForm.safetyStock = 20
    stockForm.price = 0
  }
  formDialogVisible.value = true
}

// --- 提交耗材信息 ---
const submitStockForm = async () => {
  if(!stockForm.name) return ElMessage.warning('请输入名称')
  try {
    let res;
    if (stockForm.id) {
      res = await axios.put('http://localhost:8080/api/consumable/update', stockForm)
    } else {
      res = await axios.post('http://localhost:8080/api/consumable/add', stockForm)
    }
    
    if (res.data === 'success') {
      ElMessage.success('操作成功')
      formDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error('操作失败')
    }
  } catch (error) { ElMessage.error('网络错误') }
}

// --- 删除耗材 ---
const handleDelete = async (id) => {
  try {
    const res = await axios.delete(`http://localhost:8080/api/consumable/delete/${id}`)
    if(res.data === 'success') {
       ElMessage.success('删除成功')
       loadData()
    } else { ElMessage.error('删除失败') }
  } catch (error) { ElMessage.error('网络错误') }
}

onMounted(() => { loadData() })
</script>