<template>
  <div class="app-container" style="padding: 20px;">
    
    <el-card shadow="never">
      <template #header>
        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
          <span>🏭 库存入库管理</span>
          <el-button type="success" @click="openDialog()">+ 新增耗材入库</el-button>
        </div>
      </template>

      <div style="margin-bottom: 20px;">
        <el-input v-model="searchText" placeholder="搜索耗材名称..." style="width: 200px; margin-right: 10px;" clearable @clear="loadData" />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="consumableName" label="名称" width="180" /> <el-table-column label="分类" width="120" align="center">
           <template #default="scope">
             <el-tag>{{ getCategoryName(scope.row.categoryId) }}</el-tag>
           </template>
        </el-table-column>
        
        <el-table-column prop="spec" label="规格" width="120" />
        <el-table-column prop="price" label="单价" width="100">
           <template #default="scope">¥{{ scope.row.price }}</template>
        </el-table-column>
        <el-table-column prop="currentStock" label="当前库存" width="120">
           <template #default="scope">
             <span :style="{color: scope.row.currentStock < scope.row.safetyStock ? 'red':'black', fontWeight: 'bold'}">
               {{ scope.row.currentStock }} {{ scope.row.unit }}
             </span>
           </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
             <el-button type="primary" link size="small" @click="openDialog(scope.row)">编辑</el-button>
             <el-popconfirm title="确定删除吗？" @confirm="handleDelete(scope.row.id)">
                <template #reference><el-button type="danger" link size="small">删除</el-button></template>
             </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formDialogVisible" :title="stockForm.id ? '编辑耗材' : '耗材入库'" width="500px">
      <el-form :model="stockForm" label-width="100px">
        <el-form-item label="耗材名称"><el-input v-model="stockForm.consumableName" /></el-form-item>
        
        <el-form-item label="耗材分类">
           <el-select v-model="stockForm.categoryId" style="width:100%" placeholder="请选择分类">
              <el-option 
                v-for="cat in categoryList" 
                :key="cat.id" 
                :label="cat.categoryName" 
                :value="cat.id" 
              />
           </el-select>
        </el-form-item>
        
        <el-form-item label="规格"><el-input v-model="stockForm.spec" /></el-form-item>
        <el-form-item label="单位"><el-input v-model="stockForm.unit" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="stockForm.currentStock" :min="0" /></el-form-item>
        <el-form-item label="安全库存"><el-input-number v-model="stockForm.safetyStock" :min="0" /></el-form-item>
        <el-form-item label="单价"><el-input-number v-model="stockForm.price" :step="0.1" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStockForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const searchText = ref('')
const tableData = ref([])
const categoryList = ref([]) // 存分类字典
const formDialogVisible = ref(false)
// 注意：consumableName 对应实体类字段
const stockForm = reactive({ id: null, consumableName: '', categoryId: null, spec: '', unit: '', currentStock: 0, safetyStock: 10, price: 0 })

// 1. 加载数据（同时加载分类和库存）
const loadData = async () => {
  try {
    const [catRes, conRes] = await Promise.all([
      request.get('http://localhost:8080/api/category/list'),
      request.get('http://localhost:8080/api/consumable/list')
    ])

    // 处理分类
    if (catRes.data && catRes.data.code === 200) {
      categoryList.value = catRes.data.data
    }
    // 处理库存
    if (conRes.data && conRes.data.code === 200) {
      tableData.value = conRes.data.data
    }
  } catch (error) {
    console.error("加载失败", error)
  }
}

// 辅助：ID 转 Name
const getCategoryName = (id) => {
  const find = categoryList.value.find(c => c.id === id)
  return find ? find.categoryName : '未知分类'
}

const openDialog = (row = null) => {
  if (row) {
    Object.assign(stockForm, row)
  } else {
    // 重置，categoryId 设为 null，让用户自己选
    stockForm.id = null; stockForm.consumableName = ''; stockForm.categoryId = null; 
    stockForm.spec = ''; stockForm.unit = '个'; stockForm.currentStock = 100; stockForm.safetyStock = 20; stockForm.price = 0
  }
  formDialogVisible.value = true
}

const submitStockForm = async () => {
  if(!stockForm.categoryId) return ElMessage.warning("请选择分类") // 必填校验

  try {
    const url = stockForm.id ? 'http://localhost:8080/api/consumable/update' : 'http://localhost:8080/api/consumable/add'
    const res = await request.post(url, stockForm)
    
    if (res.data.code === 200) { 
      ElMessage.success('操作成功'); 
      formDialogVisible.value = false; 
      loadData() 
    } else { 
      ElMessage.error(res.data.msg || '操作失败') 
    }
  } catch (error) { ElMessage.error('网络错误') }
}

const handleDelete = async (id) => {
  try {
    const res = await request.post(`http://localhost:8080/api/consumable/delete/${id}`)
    if (res.data.code === 200) {
        ElMessage.success('删除成功'); 
        loadData()
    } else {
        ElMessage.error(res.data.msg || '删除失败')
    }
  } catch (error) { ElMessage.error('网络错误') }
}

onMounted(() => { loadData() })
</script>