<template>
  <div class="app-container" style="padding: 20px;">
    
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>📊 实验室数据驾驶舱</span>
          <el-tag>管理员专享</el-tag>
        </div>
      </template>

      <el-row :gutter="40" style="margin-top: 10px;">
        <el-col :span="12">
          <div class="chart-wrapper">
            <h3 class="chart-title">📉 库存紧缺预警 (Top 5)</h3>
            <div id="barChart" style="height: 350px;"></div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="chart-wrapper">
            <h3 class="chart-title">🧪 耗材分类分布</h3>
            <div id="pieChart" style="height: 350px;"></div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>🔮 智能补给预测与采购建议清单</span>
          <el-button 
            type="primary" 
            color="#8a2be2" 
            :loading="isPredicting" 
            @click="runAllPredictions">
            🚀 一键生成全库补给预测
          </el-button>
        </div>
      </template>

      <el-table 
        :data="warningList" 
        border 
        stripe 
        style="width: 100%" 
        v-loading="isPredicting"
        element-loading-text="正在呼叫算法微服务，疯狂计算中..."
      >
        <el-table-column prop="consumableName" label="耗材名称" align="center" />
        
        <el-table-column label="当前实际库存" align="center" width="150">
          <template #default="scope">
            <span style="color: #F56C6C; font-weight: bold; font-size: 16px;">
              {{ scope.row.currentStock }} {{ scope.row.unit }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="AI预测安全阈值" align="center" width="150">
          <template #default="scope">
            <span style="color: #67C23A; font-weight: bold; font-size: 16px;">
              {{ scope.row.dynamicThreshold || '待计算' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="predictedUsage30d" label="未来30天预计消耗" align="center" width="160" />

        <el-table-column label="采购建议" align="center" width="180">
          <template #default="scope">
            <el-tag type="danger" effect="dark" size="large">
              建议补给: {{ scope.row.dynamicThreshold - scope.row.currentStock }} {{ scope.row.unit }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-empty 
        v-if="!isPredicting && warningList.length === 0" 
        description="🎉 太棒了！当前所有耗材库存均高于AI预测阈值，无需补给" 
        :image-size="100"
      />
    </el-card>

  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
// 💡 关键修复：不再使用原生 axios，而是使用你项目里封装好的带 Token 的 request
import request from '../utils/request' 

// 数据状态
const allConsumables = ref([])
const warningList = ref([])
const isPredicting = ref(false)

// 声明图表实例变量，方便销毁
let barChartInstance = null
let pieChartInstance = null

// 1. 获取并渲染数据
const initDataAndCharts = async () => {
  try {
    // 💡 关键修复：使用 request 工具发送请求，它会自动带上 Token
    const res = await request.get('/api/consumable/list?labId=1')
    
    // 🛡️ 兼容解析你的 CommonResult 数据结构
    let rawData = [];
    if (res.code === 200 && res.data) {
        rawData = res.data; // 你的 request.js 可能已经拦截处理过外层了
    } else if (res.data && res.data.code === 200 && res.data.data) {
        rawData = res.data.data;
    } else {
        rawData = res; // 兜底
    }

    if (!Array.isArray(rawData)) {
        console.error("❌ 获取到的数据不是数组，解析结果:", rawData)
        ElMessage.error("数据格式异常")
        return
    }

    allConsumables.value = rawData

    // 过滤出需要补给的耗材
    warningList.value = allConsumables.value.filter(
      item => item.dynamicThreshold && item.currentStock < item.dynamicThreshold
    )

    // 数据就绪，开始渲染图表
    renderCharts(allConsumables.value)
  } catch (error) {
    console.error("获取数据失败:", error)
    ElMessage.error("获取图表数据失败，请确保您已登录且后端服务已启动")
  }
}

// 2. 渲染ECharts图表
const renderCharts = (data) => {
  if (data.length === 0) return;

  const sortedByStock = [...data].sort((a, b) => a.currentStock - b.currentStock).slice(0, 5)
  
  const categoryMap = {}
  data.forEach(item => {
    const cName = item.categoryName || `分类${item.categoryId || '未知'}`
    categoryMap[cName] = (categoryMap[cName] || 0) + 1
  })
  const pieData = Object.keys(categoryMap).map(key => ({ value: categoryMap[key], name: key }))

  // --- 初始化或更新柱状图 ---
  const barDom = document.getElementById('barChart')
  if (barDom) {
    barChartInstance = echarts.getInstanceByDom(barDom) || echarts.init(barDom)
    barChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'value', name: '库存数量' },
      yAxis: { type: 'category', data: sortedByStock.map(item => item.consumableName) },
      series: [{
        name: '当前库存',
        type: 'bar',
        data: sortedByStock.map(item => ({
          value: item.currentStock,
          itemStyle: { color: item.currentStock < (item.dynamicThreshold || item.safetyStock) ? '#F56C6C' : '#409EFF' }
        })),
        label: { show: true, position: 'right' }
      }]
    })
  }

  // --- 初始化或更新饼图 ---
  const pieDom = document.getElementById('pieChart')
  if (pieDom) {
    pieChartInstance = echarts.getInstanceByDom(pieDom) || echarts.init(pieDom)
    pieChartInstance.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: '0%' },
      series: [{
        name: '耗材分类',
        type: 'pie',
        radius: '60%',
        data: pieData,
        emphasis: {
          itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' }
        }
      }]
    })
  }
}

// 3. 一键预测逻辑
const runAllPredictions = async () => {
  if (allConsumables.value.length === 0) return ElMessage.warning("当前没有耗材数据可预测")
  
  isPredicting.value = true
  let successCount = 0

  try {
    for (const item of allConsumables.value) {
      try {
        // 💡 关键修复：同样使用 request.js 发送预测请求
        const res = await request.get(`/api/consumable/predict/${item.id}`)
        if (res.code === 200 || (res.data && res.data.code === 200)) {
           successCount++
        }
      } catch (e) {
        console.warn(`耗材ID ${item.id} 预测失败:`, e)
      }
    }
    ElMessage.success(`AI预测完毕！成功更新 ${successCount} 项耗材的预警线`)
    await initDataAndCharts() // 重新拉取数据刷新图表
  } catch (error) {
    ElMessage.error('预测过程出现异常，请检查后端服务')
  } finally {
    isPredicting.value = false
  }
}

// 监听窗口大小变化，让图表自适应缩放
const handleResize = () => {
  if (barChartInstance) barChartInstance.resize()
  if (pieChartInstance) pieChartInstance.resize()
}

onMounted(() => {
  nextTick(() => { 
    initDataAndCharts() 
    window.addEventListener('resize', handleResize)
  })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (barChartInstance) barChartInstance.dispose()
  if (pieChartInstance) pieChartInstance.dispose()
})
</script>