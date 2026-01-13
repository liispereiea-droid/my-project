<template>
  <div class="app-container" style="padding: 20px;">
    
    <el-card shadow="never">
      <template #header>
        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
          <span>🏷️ 耗材分类目录</span>
          <el-button v-if="userInfo.role === 'admin'" type="primary" size="small" @click="openDialog()">+ 新增分类</el-button>
        </div>
      </template>

      <el-table :data="tableData" border stripe style="width: 100%" row-key="id">
        
        <el-table-column type="expand">
          <template #default="props">
            <div style="padding: 10px 50px; background-color: #f8f9fa;">
              <h3>📦 {{ props.row.name }} - 下属耗材清单</h3>
              
              <el-table :data="getConsumablesByCategory(props.row.name)" size="small" border>
                <el-table-column prop="name" label="耗材名称" />
                <el-table-column prop="spec" label="规格" width="150" />
                <el-table-column prop="currentStock" label="当前库存" width="120">
                  <template #default="subScope">
                    <span :style="{ color: subScope.row.currentStock < subScope.row.safetyStock ? 'red' : 'green', fontWeight: 'bold' }">
                      {{ subScope.row.currentStock }} {{ subScope.row.unit }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="price" label="单价" width="100">
                  <template #default="subScope">¥{{ subScope.row.price }}</template>
                </el-table-column>
              </el-table>

              <div v-if="getConsumablesByCategory(props.row.name).length === 0" style="text-align: center; color: #909399; padding: 20px;">
                (该分类下暂无耗材数据)
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="分类名称" width="200">
          <template #default="scope">
            <el-tag effect="dark">{{ scope.row.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注说明" />
        
        <el-table-column label="操作" width="180" align="center" v-if="userInfo.role === 'admin'">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="openDialog(scope.row)">编辑</el-button>
            <el-popconfirm title="确定删除该分类吗？" @confirm="handleDelete(scope.row.id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="form.name" placeholder="例如：生物制剂" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="分类描述..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定保存</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const userInfo = JSON.parse(localStorage.getItem('user_info') || '{}')
const tableData = ref([]) // 存放分类
const allConsumables = ref([]) // 存放所有耗材
const dialogVisible = ref(false)
const form = reactive({ id: null, name: '', remark: '' })

// 加载数据：同时获取“分类”和“耗材”
const loadData = async () => {
  try {
    // 并行请求，提高速度
    const [categoryRes, consumableRes] = await Promise.all([
      axios.get('http://localhost:8080/api/category/list'),
      axios.get('http://localhost:8080/api/consumable/list?labId=1')
    ])
    
    tableData.value = categoryRes.data
    allConsumables.value = consumableRes.data
    
  } catch (error) { console.error(error) }
}

// 辅助函数：根据分类名称筛选耗材
// 这里利用 JS 在前端过滤，避免频繁请求后端
const getConsumablesByCategory = (categoryName) => {
  return allConsumables.value.filter(item => item.categoryName === categoryName)
}

// 打开弹窗
const openDialog = (row = null) => {
  if (row) {
    form.id = row.id; form.name = row.name; form.remark = row.remark
  } else {
    form.id = null; form.name = ''; form.remark = ''
  }
  dialogVisible.value = true
}

// 提交
const submitForm = async () => {
  if (!form.name) return ElMessage.warning('请输入名称')
  try {
    const url = form.id ? 'http://localhost:8080/api/category/update' : 'http://localhost:8080/api/category/add'
    const method = form.id ? 'put' : 'post'
    const res = await axios[method](url, form)
    
    if (res.data === 'success') {
      ElMessage.success('操作成功')
      dialogVisible.value = false
      loadData()
    } else { ElMessage.error('失败') }
  } catch (error) { ElMessage.error('网络错误') }
}

// 删除
const handleDelete = async (id) => {
  try {
    await axios.delete(`http://localhost:8080/api/category/delete/${id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) { ElMessage.error('失败') }
}

onMounted(() => { loadData() })
</script>