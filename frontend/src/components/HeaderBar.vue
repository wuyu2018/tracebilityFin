<template>
  <header class="header">
    <div class="header-inner">
      <router-link to="/" class="logo">
        <el-icon :size="28"><Checked /></el-icon>
        <span class="logo-text">食品溯源平台</span>
      </router-link>
      <nav class="nav nav-desktop">
        <router-link to="/" class="nav-item" active-class="nav-item--active" exact>首页</router-link>
        <router-link to="/verify" class="nav-item" active-class="nav-item--active">追溯查询</router-link>
        <router-link to="/complaint" class="nav-item" active-class="nav-item--active">投诉反馈</router-link>
        <router-link to="/intro" class="nav-item" active-class="nav-item--active">系统介绍</router-link>
      </nav>
      <el-icon :size="24" class="hamburger" @click="drawerVisible = true">
        <Menu />
      </el-icon>
    </div>
  </header>

  <el-drawer
    v-model="drawerVisible"
    direction="rtl"
    size="260px"
    :with-header="false"
  >
    <div class="drawer-logo">
      <el-icon :size="24" color="#409eff"><Checked /></el-icon>
      <span>食品溯源平台</span>
    </div>
    <div class="drawer-nav">
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="drawer-nav-item"
        active-class="drawer-nav-item--active"
        :exact="item.exact"
        @click="drawerVisible = false"
      >
        <el-icon :size="18"><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
      </router-link>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref } from 'vue'
import { Checked, Menu, Search, WarningFilled, Document, HomeFilled } from '@element-plus/icons-vue'

const drawerVisible = ref(false)

const navItems = [
  { path: '/', label: '首页', icon: HomeFilled, exact: true },
  { path: '/verify', label: '追溯查询', icon: Search, exact: false },
  { path: '/complaint', label: '投诉反馈', icon: WarningFilled, exact: false },
  { path: '/intro', label: '系统介绍', icon: Document, exact: false }
]
</script>

<style scoped>
.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #409eff;
  font-size: 20px;
  font-weight: 700;
  flex-shrink: 0;
}

.nav {
  display: flex;
  gap: 4px;
}

.nav-item {
  padding: 8px 18px;
  border-radius: 6px;
  font-size: 15px;
  color: #555;
  transition: all 0.2s;
}

.nav-item:hover {
  color: #409eff;
  background: #ecf5ff;
}

.nav-item--active {
  color: #409eff;
  background: #ecf5ff;
}

.hamburger {
  display: none;
  cursor: pointer;
  color: #606266;
}

.hamburger:hover { color: #409eff; }

@media (max-width: 768px) {
  .nav-desktop { display: none; }
  .hamburger { display: block; }
  .logo-text { font-size: 17px; }
}

.drawer-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.drawer-nav {
  padding: 8px;
}

.drawer-nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 8px;
  font-size: 16px;
  color: #555;
  transition: all 0.2s;
}

.drawer-nav-item:hover {
  color: #409eff;
  background: #ecf5ff;
}

.drawer-nav-item--active {
  color: #409eff;
  background: #ecf5ff;
}
</style>
