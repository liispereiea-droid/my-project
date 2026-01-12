<template>
  <div class="app-container" style="padding: 20px;">
    
    <el-card shadow="never" class="mb-20">
      <el-input v-model="searchText" placeholder="输入用户名或姓名搜索" style="width: 200px; margin-right: 10px;" clearable @clear="loadData" />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button type="success" @click="openDialog()">+ 新增用户</el-button>
    </el-card>

    <el-card shadow="never">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="账号" width="150" />
        <el-table-column prop="nickname" label="姓名" width="150" />
        <el-table-column prop="role" label="角色" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.role === 'admin'" type="danger">管理员</el-tag>
            <el-tag v-else-if="scope.row.role === 'teacher'" type="warning">导师</el-tag>
            <el-tag v-else type="success">学生</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="teacherId" label="所属导师ID" width="120" align="center">
          <template #default="scope">
             <span v-if="scope.row.role === 'student'">{{ scope.row.teacherId || '未绑定' }}</span>
             <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="密码" width="120">
          <template #default>******</template>
        </el-table-column>

        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="openDialog(scope.row)">编辑</el-button>
            <el-popconfirm title="确定删除该用户吗？此操作不可恢复！" @confirm="handleDelete(scope.row.id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="账号">
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.nickname" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="学生" value="student" />
            <el-option label="导师" value="teacher" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="导师ID" v-if="form.role === 'student'">
          <el-input v-model="form.teacherId" placeholder="请输入该学生的导师ID" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="为空则不修改 (新增默认123456)" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage , ElMessageBox } from 'element-plus'

const searchText = ref('')
const tableData = ref([])
const dialogVisible = ref(false)

// 表单数据
const form = reactive({
  id: null,
  username: '',
  nickname: '',
  role: 'student',
  teacherId: '',
  password: ''
})

// 加载列表
const loadData = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/user/list', {
      params: { name: searchText.value }
    })
    tableData.value = res.data
  } catch (error) {
    console.error(error)
  }
}

// 打开弹窗 (新增/编辑共用)
const openDialog = (row = null) => {
  if (row) {
    // 编辑模式：填充数据
    form.id = row.id
    form.username = row.username
    form.nickname = row.nickname
    form.role = row.role
    form.teacherId = row.teacherId
    form.password = '' // 编辑时不回显密码
  } else {
    // 新增模式：清空数据
    form.id = null
    form.username = ''
    form.nickname = ''
    form.role = 'student'
    form.teacherId = ''
    form.password = ''
  }
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!form.username || !form.nickname) return ElMessage.warning('请填写完整')
  
  try {
    if (form.id) {
      // === 编辑模式 (接口没变，还是返回 String) ===
      const res = await axios.put('http://localhost:8080/api/user/update', form)
      if (res.data === 'success') {
        ElMessage.success('修改成功')
        dialogVisible.value = false
        loadData()
      } else {
        ElMessage.error('修改失败')
      }
    } else {
      // === 新增模式 (接口改了，返回 JSON 对象) ===
      const res = await axios.post('http://localhost:8080/api/user/add', form)
      
      if (res.data.code === 200) {
        const newUser = res.data.data
        dialogVisible.value = false
        loadData()

        // 核心修改：如果是导师，弹窗告知 ID
        if (newUser.role === 'teacher') {
          ElMessageBox.alert(
            `导师账号创建成功！<br/><br/><strong>导师 ID：<span style="color:red;font-size:18px">${newUser.id}</span></strong><br/><br/>请将此 ID 告知学生用于注册。`,
            '重要提示',
            {
              confirmButtonText: '我知道了',
              dangerouslyUseHTMLString: true, // 允许使用 HTML 加粗变红
              type: 'success'
            }
          )
        } else {
          ElMessage.success(`添加成功，新用户ID: ${newUser.id}`)
        }

      } else {
        ElMessage.error(res.data.msg)
      }
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('网络错误')
  }
}

// 删除用户
const handleDelete = async (id) => {
  try {
    await axios.delete(`http://localhost:8080/api/user/delete/${id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.mb-20 { margin-bottom: 20px; }
</style>