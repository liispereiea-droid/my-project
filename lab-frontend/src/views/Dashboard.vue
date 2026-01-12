<template>
  <div class="dashboard-container">
    <div class="welcome-card">
      <img src="https://img.freepik.com/free-vector/science-lab-concept-illustration_114360-3882.jpg" alt="Lab" class="welcome-img">
      <h1>🎉 欢迎使用实验室耗材智能管理系统</h1>
      <p class="sub-text">您可以点击左侧菜单进行耗材查询、领用申请等操作。</p>
    </div>
  </div>
</template>

<style scoped>
.dashboard-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 80vh; /* 让内容垂直居中 */
  background-color: white;
  border-radius: 10px;
}
.welcome-card {
  text-align: center;
}
.welcome-img {
  width: 300px;
  margin-bottom: 20px;
}
h1 {
  color: #303133;
  font-size: 24px;
}
.sub-text {
  color: #909399;
  margin-top: 10px;
}
</style>