<template>
  <div class="common-layout">
    <el-container>
      <el-header class="header">
        <div class="logo">
          <!-- 这里改为系统的正式名称 -->
          <el-icon style="margin-right: 10px; vertical-align: middle;"><DataLine /></el-icon>
          <span class="neon-text">车间生产任务管理系统</span>
          <span class="sub-text">_4746</span>
        </div>
        <el-menu 
          mode="horizontal" 
          router 
          :default-active="$route.path" 
          background-color="transparent" 
          text-color="#fff" 
          active-text-color="#c5f015"
          class="custom-menu"
        >
          <!-- 菜单项回归中文数据库属性 -->
          <el-menu-item index="/tasks">生产任务管理</el-menu-item>
          <el-menu-item index="/products">产品信息管理</el-menu-item>
        </el-menu>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<style scoped>
.header {
  background-color: #000;
  border-bottom: 2px solid var(--theme-neon);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  height: 70px;
}
.logo {
  font-size: 20px;
  font-weight: bold;
  color: #fff;
  display: flex;
  align-items: center;
}
.neon-text {
  color: var(--theme-neon); /* 荧光绿 */
  margin-right: 5px;
}
.sub-text {
  color: #888;
  font-size: 16px;
}
.custom-menu {
  border-bottom: none !important;
  width: 400px;
}
.main-content {
  padding: 20px 40px;
  min-height: calc(100vh - 70px);
}
</style>