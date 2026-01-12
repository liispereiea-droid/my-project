<template>
  <div class="app-container" style="padding: 20px;">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>📝 领用申请记录</span>
          <el-tag v-if="userInfo.role === 'teacher'" type="warning">导师审批模式</el-tag>
          <el-tag v-else-if="userInfo.role === 'admin'" type="danger">管理员审批模式</el-tag>
          <el-tag v-else type="success">我的申请</el-tag>
        </div>
      </template>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="单号" width="80" align="center" />
        <el-table-column prop="userName" label="申请人" width="100" />
        <el-table-column prop="consumableName" label="申请耗材" width="150" />
        <el-table-column prop="applyCount" label="数量" width="80" align="center" />
        <el-table-column prop="createTime" label="提交时间" width="170">
          <template #default="scope">{{ formatDate(scope.row.createTime) }}</template>
        </el-table-column>
        
        <el-table-column label="当前状态" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="warning">⏳ 待导师审批</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="primary">⏳ 待管理员审批</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="success">✅ 已完成</el-tag>
            <el-tag v-else type="danger">❌ 已驳回</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" align="center">
          <template #default="scope">
            <div v-if="(userInfo.role === 'teacher' && scope.row.status === 0) || (userInfo.role === 'admin' && scope.row.status === 1)">
              <el-button type="success" size="small" @click="handleAudit(scope.row, 1)">同意</el-button>
              <el-button type="danger" size="small" @click="handleAudit(scope.row, 2)">驳回</el-button>
            </div>
            
            <div v-else>
               <el-button type="primary" size="small" plain @click="openReceiptDialog(scope.row)">
                 {{ scope.row.status === 2 ? '查看单据 / 打印' : '查看进度' }}
               </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="📋 申请详情单" width="700px">
      
      <div class="audit-timeline">
        <h4>审批流转记录</h4>
        <el-timeline>
          <el-timeline-item :timestamp="formatDate(currentApply.createTime)" placement="top" type="primary">
            <el-card shadow="hover" class="timeline-card">
              <h4>{{ currentApply.userName }} 提交申请</h4>
              <p>理由：{{ currentApply.reason }}</p>
            </el-card>
          </el-timeline-item>
          
          <el-timeline-item 
            v-if="currentApply.teacherAuditTime" 
            :timestamp="formatDate(currentApply.teacherAuditTime)" 
            :type="currentApply.status >= 1 ? 'success' : 'danger'">
            <el-card shadow="hover" class="timeline-card">
              <h4>导师审批</h4>
              <p>意见：{{ currentApply.teacherAuditOpinion || '无意见' }}</p>
            </el-card>
          </el-timeline-item>

          <el-timeline-item 
            v-if="currentApply.adminAuditTime" 
            :timestamp="formatDate(currentApply.adminAuditTime)" 
            :type="currentApply.status === 2 ? 'success' : 'danger'">
            <el-card shadow="hover" class="timeline-card">
              <h4>管理员审批</h4>
              <p>意见：{{ currentApply.adminAuditOpinion || '无意见' }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>

      <el-divider />

      <div v-if="currentApply.status === 2">
        <div class="actions-bar">
          <span>👇 下方为线下领用凭证，请下载打印</span>
          <el-button type="primary" @click="downloadPDF">🖨️ 下载 PDF</el-button>
        </div>

        <div id="pdf-content" class="receipt-box">
          <div class="watermark">
            APPROVED {{ formatDateSimple(currentApply.adminAuditTime) }}
          </div>
          
          <h2 class="receipt-title">实验室耗材领用单</h2>
          <div class="receipt-header">
            <div>单号：#{{ currentApply.id }}</div>
            <div>打印日期：{{ new Date().toLocaleDateString() }}</div>
          </div>

          <table class="receipt-table">
            <tr>
              <td class="label">申请人</td>
              <td class="value">{{ currentApply.userName }}</td>
              <td class="label">所属实验室ID</td>
              <td class="value">{{ currentApply.labId }}</td>
            </tr>
            <tr>
              <td class="label">耗材名称</td>
              <td class="value bold">{{ currentApply.consumableName }}</td>
              <td class="label">领用数量</td>
              <td class="value bold">{{ currentApply.applyCount }}</td>
            </tr>
            <tr>
              <td class="label">申请用途</td>
              <td colspan="3" class="value">{{ currentApply.reason }}</td>
            </tr>
          </table>

          <div class="signatures">
            <div class="sign-box">
              <p>导师签字：(电子签)</p>
              <div class="sign-name">同意</div>
              <p class="sign-date">{{ formatDateSimple(currentApply.teacherAuditTime) }}</p>
            </div>
            <div class="sign-box">
              <p>管理员签字：(电子签)</p>
              <div class="sign-name">同意</div>
              <p class="sign-date">{{ formatDateSimple(currentApply.adminAuditTime) }}</p>
            </div>
            <div class="sign-box">
              <p>领用人签字：</p>
              <div class="sign-name manual-sign">_____________</div>
              <p class="sign-date">____年__月__日</p>
            </div>
          </div>
          <div class="receipt-footer">
             * 请凭此单前往仓库领取物资，涂改无效。
          </div>
        </div>
      </div>
      <div v-else class="text-center text-gray">
        (申请尚未走完流程，暂无法生成领用单)
      </div>

    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'

const tableData = ref([])
const userInfo = ref(JSON.parse(localStorage.getItem('user_info') || '{}'))
const dialogVisible = ref(false)
const currentApply = ref({})

// 日期格式化工具
const formatDate = (timeStr) => {
  if(!timeStr) return ''
  return new Date(timeStr).toLocaleString()
}
// 简单日期格式化 (用于单据)
const formatDateSimple = (timeStr) => {
  if(!timeStr) return ''
  return new Date(timeStr).toLocaleDateString()
}

// 加载列表
const loadData = async () => {
  try {
    const res = await axios.get(`http://localhost:8080/api/apply/list`, {
      params: { role: userInfo.value.role, userId: userInfo.value.id }
    })
    tableData.value = res.data
  } catch (error) { console.error(error) }
}

// 打开单据详情
const openReceiptDialog = (row) => {
  currentApply.value = row
  dialogVisible.value = true
}

// 审批
const handleAudit = (row, result) => {
  ElMessageBox.prompt('请输入审批意见（选填）', result === 1 ? '同意申请' : '驳回申请', {
    confirmButtonText: '确定提交',
    cancelButtonText: '取消',
  }).then(async ({ value }) => {
    try {
      const res = await axios.post('http://localhost:8080/api/apply/audit', {
        id: row.id, result: result, opinion: value || (result === 1 ? '同意' : '驳回'), role: userInfo.value.role
      })
      if (res.data === 'success') {
        ElMessage.success('审批成功')
        loadData()
      }
    } catch (error) {}
  }).catch(() => {})
}

// === 核心：生成 PDF ===
const downloadPDF = () => {
  const element = document.getElementById('pdf-content') // 获取要截图的DOM元素
  
  html2canvas(element, {
    scale: 2, // 提高清晰度
    useCORS: true
  }).then((canvas) => {
    const contentWidth = canvas.width
    const contentHeight = canvas.height
    
    // A4纸尺寸 (mm)
    const pdf = new jsPDF('p', 'mm', 'a4')
    const imgWidth = 190 // 留边距
    const imgHeight = (190 / contentWidth) * contentHeight
    
    const pageData = canvas.toDataURL('image/jpeg', 1.0)
    
    pdf.addImage(pageData, 'JPEG', 10, 10, imgWidth, imgHeight)
    pdf.save(`领用单_${currentApply.value.userName}_${currentApply.value.consumableName}.pdf`)
  })
}

onMounted(() => { loadData() })
</script>

<style scoped>
/* 时间轴样式 */
.audit-timeline {
  padding: 10px 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}
.timeline-card h4 { margin: 0 0 5px 0; font-size: 14px; }
.timeline-card p { margin: 0; font-size: 12px; color: #666; }

/* 领用单样式 (模仿真实纸质单据) */
.receipt-box {
  position: relative;
  border: 2px solid #333;
  padding: 30px;
  margin-top: 20px;
  background: white;
  color: #000;
  font-family: 'SimSun', 'Songti SC', serif; /* 宋体，更有正式感 */
}

/* 水印样式 */
.watermark {
  position: absolute;
  top: 40%;
  left: 50%;
  transform: translate(-50%, -50%) rotate(-30deg);
  font-size: 50px;
  font-weight: bold;
  color: rgba(255, 0, 0, 0.25); 
  border: 3px solid rgba(255, 0, 0, 0.25);
  padding: 10px 40px;
  z-index: 999; 
  pointer-events: none; 
}

.receipt-title {
  text-align: center;
  font-size: 24px;
  margin-bottom: 20px;
  letter-spacing: 2px;
}
.receipt-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 12px;
}
.receipt-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 30px;
  position: relative;
}
.receipt-table td {
  border: 1px solid #333;
  padding: 10px;
  font-size: 14px;
}
.receipt-table .label {
  background-color: #f0f0f0;
  width: 15%;
  font-weight: bold;
}
.receipt-table .value {
  width: 35%;
}
.receipt-table .bold {
  font-weight: bold;
  font-size: 16px;
}

/* 签字区 */
.signatures {
  display: flex;
  justify-content: space-between;
  margin-top: 40px;
}
.sign-box {
  text-align: center;
  width: 30%;
}
.sign-name {
  font-family: 'Kaushan Script', cursive; /* 稍微像手写的字体 */
  font-size: 20px;
  margin: 10px 0;
  border-bottom: 1px solid #ccc;
  min-height: 30px;
}
.manual-sign {
  color: #fff; /* 空白让人手写 */
  border-bottom: 1px solid #333;
}
.sign-date {
  font-size: 12px;
  color: #666;
}
.receipt-footer {
  margin-top: 30px;
  font-size: 12px;
  text-align: center;
  color: #666;
}
.actions-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.text-gray { color: #999; margin-top: 20px; }
</style>