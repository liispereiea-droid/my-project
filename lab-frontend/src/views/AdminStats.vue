<template>
  <div class="app-container" style="padding: 20px;">
    
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>📊 实验室数据驾驶舱</span>
          <el-tag>管理员专享</el-tag>
        </div>
      </template>

      <el-row :gutter="40" style="margin-top: 20px;">
        
        <el-col :span="12">
          <div class="chart-wrapper">
            <h3 class="chart-title">📉 库存紧缺预警 (Top 5)</h3>
            <div id="barChart" style="height: 400px;"></div>
          </div>
        </el-col>

        <el-col :span="12">
          <div class="chart-wrapper">
            <h3 class="chart-title">🧪 耗材分类分布</h3>
            <div id="pieChart" style="height: 400px;"></div>
          </div>
        </el-col>

      </el-row>
    </el-card>

  </div>
</template>

<script setup>
import { onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'

const initCharts = async () => {
  try {
    // 1. 获取耗材数据
    const res = await axios.get('http://localhost:8080/api/consumable/list?labId=1')
    const data = res.data

    // 2. 数据处理 - 柱状图 (取库存最少的5个)
    const sortedByStock = [...data].sort((a, b) => a.currentStock - b.currentStock).slice(0, 5)
    
    // 3. 数据处理 - 饼图 (按分类统计)
    const categoryMap = {}
    data.forEach(item => {
      categoryMap[item.categoryName] = (categoryMap[item.categoryName] || 0) + 1
    })
    const pieData = Object.keys(categoryMap).map(key => ({ value: categoryMap[key], name: key }))

    // --- 画柱状图 ---
    const barChart = echarts.init(document.getElementById('barChart'))
    barChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'value', name: '库存数量' },
      yAxis: { type: 'category', data: sortedByStock.map(item => item.name) },
      series: [{
        name: '当前库存',
        type: 'bar',
        data: sortedByStock.map(item => ({
          value: item.currentStock,
          itemStyle: { color: item.currentStock < item.safetyStock ? '#F56C6C' : '#409EFF' }
        })),
        label: { show: true, position: 'right' }
      }]
    })

    // --- 画饼图 ---
    const pieChart = echarts.init(document.getElementById('pieChart'))
    pieChart.setOption({
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

    // 响应式调整
    window.addEventListener('resize', () => {
      barChart.resize()
      pieChart.resize()
    })

  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  nextTick(() => { initCharts() })
})
</script>

<style scoped>
.chart-wrapper {
  background: #fdfdfd;
  padding: 10px;
  border-radius: 8px;
  border: 1px solid #eee;
}
.chart-title {
  text-align: center;
  margin-bottom: 10px;
  color: #333;
  font-size: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>