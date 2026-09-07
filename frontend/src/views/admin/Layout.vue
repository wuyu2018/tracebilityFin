<template>
  <div class="admin-layout">
    <el-container>
      <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
        <div class="aside-header">
          <span v-show="!isCollapse" class="aside-title">管理后台</span>
          <span v-show="isCollapse" class="aside-title-mini">FT</span>
        </div>

        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
        >
          <el-menu-item index="/manage/dashboard" v-if="hasMenuAccess('dashboard')">
            <el-icon><Odometer /></el-icon>
            <span>数据看板</span>
          </el-menu-item>
          <el-menu-item index="/manage/products" v-if="hasMenuAccess('products')">
            <el-icon><Goods /></el-icon>
            <span>产品管理</span>
          </el-menu-item>
          <el-menu-item index="/manage/materials" v-if="hasMenuAccess('materials')">
            <el-icon><Box /></el-icon>
            <span>原料管理</span>
          </el-menu-item>
          <el-menu-item index="/manage/batches" v-if="hasMenuAccess('batches')">
            <el-icon><List /></el-icon>
            <span>生产批次管理</span>
          </el-menu-item>
          <el-menu-item index="/manage/storages" v-if="hasMenuAccess('storages')">
            <el-icon><OfficeBuilding /></el-icon>
            <span>仓储管理</span>
          </el-menu-item>
          <el-menu-item index="/manage/transports" v-if="hasMenuAccess('transports')">
            <el-icon><Van /></el-icon>
            <span>运输销售管理</span>
          </el-menu-item>
          <el-menu-item index="/manage/inspections" v-if="hasMenuAccess('inspections')">
            <el-icon><Checked /></el-icon>
            <span>质检管理</span>
          </el-menu-item>
          <el-menu-item index="/manage/complaints" v-if="hasMenuAccess('complaints')">
            <el-icon><WarningFilled /></el-icon>
            <span>投诉管理</span>
          </el-menu-item>
          <el-menu-item v-if="hasMenuAccess('blockchain')" index="/manage/blockchain">
            <el-icon><Link /></el-icon>
            <span>区块链监控</span>
          </el-menu-item>
          <el-menu-item v-if="hasMenuAccess('agents')" index="/manage/agents">
            <el-icon><Monitor /></el-icon>
            <span>溯源智能体</span>
          </el-menu-item>
          <el-menu-item v-if="hasMenuAccess('companies')" index="/manage/companies">
            <el-icon><OfficeBuilding /></el-icon>
            <span>公司管理</span>
          </el-menu-item>
          <el-menu-item v-if="hasMenuAccess('admins')" index="/manage/admins">
            <el-icon><UserFilled /></el-icon>
            <span>管理员管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="admin-header">
          <div class="header-left">
            <el-icon
              :size="20"
              class="collapse-btn"
              @click="isCollapse = !isCollapse"
            >
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </div>
          <div class="header-right">
            <span class="user-info">{{ auth.user?.username }}</span>
            <el-button text @click="handleLogout">退出</el-button>
          </div>
        </el-header>

        <el-main class="admin-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '@/stores/auth'
import {
  Odometer, Goods, Box, List, OfficeBuilding, Van,
  Checked, WarningFilled, Link, Monitor, UserFilled, Fold, Expand
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const { auth, logout, hasMenuAccess } = useAuth()

const isCollapse = ref(false)

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/manage/batches')) return '/manage/batches'
  if (path.startsWith('/manage/materials')) return '/manage/materials'
  return path
})

function handleLogout() {
  logout()
  router.push('/manage/login')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.admin-layout .el-container {
  min-height: 100vh;
}

.aside {
  background: #304156;
  overflow: hidden;
  transition: width 0.3s;
}

.aside-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.aside-title {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  white-space: nowrap;
}

.aside-title-mini {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
}

.el-menu {
  border-right: none;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  cursor: pointer;
  color: #606266;
}

.collapse-btn:hover {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.company-name {
  font-size: 14px;
  color: #409eff;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: #ecf5ff;
  border-radius: 6px;
  margin-right: 8px;
}

.user-info {
  font-size: 14px;
  color: #606266;
}

.admin-main {
  background: #f5f7fa;
  padding: 20px;
}
</style>
