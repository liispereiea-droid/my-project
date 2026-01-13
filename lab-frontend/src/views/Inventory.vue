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
        <el-table-column prop="name" label="名称" width="180" />
        <el-table-column prop="categoryName" label="分类" width="120" align="center">
           <template #default="scope"><el-tag>{{ scope.row.categoryName }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="spec" label="规格" width="120" />
        <el-table-column prop="price" label="单价" width="100">
           <template #default="scope">¥{{ scope.row.price }}</template>
        </el-table-column>
        <el-table-column prop="currentStock" label="当前库存" width="120">
           <template #default="scope">
             <span :style="{color: scope.row.currentStock < scope.row.safetyStock ? 'red':'black'}">
               {{ scope.row.currentStock }} {{ scope.row.unit }}
             </span>
           </template>
        </el-table-column>
        
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
             <el-button type="primary" link size="small" @click="openDialog(scope.row)">编辑</el-button>
             <el-popconfirm title="确定删除吗？" @confirm="handleDelete(scope.row.id)">
                <template #reference>
                  <el-button type="danger" link size="small">删除</el-button>
                </template>
             </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="formDialogVisible" :title="stockForm.id ? '编辑耗材' : '耗材入库'" width="500px">
      <el-form :model="stockForm" label-width="100px">
        <el-form-item label="耗材名称"><el-input v-model="stockForm.name" /></el-form-item>
        <el-form-item label="耗材分类">
           <el-select v-model="stockForm.categoryName" style="width:100%">
              <el-option label="化学试剂" value="化学试剂" />
              <el-option label="玻璃器皿" value="玻璃器皿" />
              <el-option label="实验仪器" value="实验仪器" />
              <el-option label="办公用品" value="办公用品" />
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
import axios from 'axios'
import { ElMessage } from 'element-plus'

const searchText = ref('')
const tableData = ref([])
const formDialogVisible = ref(false)
const stockForm = reactive({ id: null, name: '', categoryName: '', spec: '', unit: '', currentStock: 0, safetyStock: 10, price: 0 })

const loadData = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/consumable/list', { params: { labId: 1, name: searchText.value } })
    tableData.value = res.data
  } catch (error) { console.error(error) }
}

const openDialog = (row = null) => {
  if (row) Object.assign(stockForm, row)
  else {
    stockForm.id = null; stockForm.name = ''; stockForm.categoryName = '化学试剂'; stockForm.spec = ''; 
    stockForm.unit = '个'; stockForm.currentStock = 100; stockForm.safetyStock = 20; stockForm.price = 0
  }
  formDialogVisible.value = true
}

const submitStockForm = async () => {
  try {
    const url = stockForm.id ? 'http://localhost:8080/api/consumable/update' : 'http://localhost:8080/api/consumable/add'
    const method = stockForm.id ? 'put' : 'post'
    const res = await axios[method](url, stockForm)
    if (res.data === 'success') { ElMessage.success('操作成功'); formDialogVisible.value = false; loadData() }
    else ElMessage.error('失败')
  } catch (error) { ElMessage.error('网络错误') }
}

const handleDelete = async (id) => {
  try {
    await axios.delete(`http://localhost:8080/api/consumable/delete/${id}`)
    ElMessage.success('删除成功'); loadData()
  } catch (error) { ElMessage.error('失败') }
}

onMounted(() => { loadData() })
</script>