import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/consumer/Home.vue')
  },
  {
    path: '/verify',
    name: 'Verify',
    component: () => import('@/views/consumer/Verify.vue')
  },
  {
    path: '/complaint',
    name: 'Complaint',
    component: () => import('@/views/consumer/Complaint.vue')
  },
  {
    path: '/intro',
    name: 'Intro',
    component: () => import('@/views/consumer/Intro.vue')
  },
  {
    path: '/manage/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/Login.vue')
  },
  {
    path: '/manage',
    component: () => import('@/views/admin/Layout.vue'),
    meta: { requiresAuth: true },
    redirect: '/manage/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'products',
        name: 'ProductManage',
        component: () => import('@/views/admin/ProductManage.vue')
      },
      {
        path: 'materials',
        name: 'MaterialManage',
        meta: { requiredAgentTypes: ['PRODUCTION'] },
        component: () => import('@/views/admin/MaterialManage.vue')
      },
      {
        path: 'batches',
        name: 'BatchManage',
        meta: { requiredAgentTypes: ['PRODUCTION'] },
        component: () => import('@/views/admin/BatchManage.vue')
      },
      {
        path: 'batches/:id',
        name: 'BatchDetail',
        meta: { requiresAuth: true },
        component: () => import('@/views/admin/BatchDetail.vue')
      },
      {
        path: 'storages',
        name: 'StorageManage',
        meta: { requiredAgentTypes: ['CIRCULATION'] },
        component: () => import('@/views/admin/StorageManage.vue')
      },
      {
        path: 'transports',
        name: 'TransportManage',
        meta: { requiredAgentTypes: ['CIRCULATION', 'SALES'] },
        component: () => import('@/views/admin/TransportManage.vue')
      },
      {
        path: 'inspections',
        name: 'InspectionManage',
        meta: { requiredAgentTypes: ['PRODUCTION'] },
        component: () => import('@/views/admin/InspectionManage.vue')
      },
      {
        path: 'complaints',
        name: 'ComplaintManage',
        meta: { requiredAgentTypes: ['SALES'] },
        component: () => import('@/views/admin/ComplaintManage.vue')
      },
      {
        path: 'blockchain',
        name: 'BlockchainMonitor',
        component: () => import('@/views/admin/BlockchainMonitor.vue')
      },
      {
        path: 'agents',
        name: 'AgentManage',
        component: () => import('@/views/admin/AgentManage.vue')
      },
      {
        path: 'companies',
        name: 'CompanyManage',
        meta: { requiresAuth: true, requiresSuperAdmin: true },
        component: () => import('@/views/admin/CompanyManage.vue')
      },
      {
        path: 'admins',
        name: 'AdminManage',
        meta: { requiresSuperAdmin: true },
        component: () => import('@/views/admin/AdminManage.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    redirect: '/404'
  },
  {
    path: '/404',
    component: () => import('@/views/consumer/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  if (to.matched.some((r) => r.meta.requiresAuth)) {
    const token = localStorage.getItem('admin_token')
    if (!token) {
      return next({ name: 'AdminLogin', query: { redirect: to.fullPath } })
    }
  }
  if (to.meta.requiresSuperAdmin) {
    const user = JSON.parse(localStorage.getItem('admin_user') || 'null')
    if (user?.role !== 'SUPER_ADMIN') {
      ElMessage.warning('仅超级管理员可访问')
      return next({ name: 'Dashboard' })
    }
  }
  if (to.meta.requiredAgentTypes) {
    const user = JSON.parse(localStorage.getItem('admin_user') || 'null')
    if (user?.role === 'SUPER_ADMIN') {
      return next()
    }
    const allowed = to.meta.requiredAgentTypes
    if (!allowed.includes(user?.agentType)) {
      ElMessage.warning('无权限访问该模块')
      return next({ name: 'Dashboard' })
    }
  }
  next()
})

export default router
