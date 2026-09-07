import { reactive } from 'vue'
import { login as apiLogin, storeCaptcha } from '@/api/admin'
import { canAccessMenu, canWrite, requiresSuperAdmin } from '@/utils/permissions'

const auth = reactive({
  token: localStorage.getItem('admin_token') || '',
  user: JSON.parse(localStorage.getItem('admin_user') || 'null'),
  isLoggedIn: !!localStorage.getItem('admin_token')
})

export function useAuth() {
  async function doLogin(username, password, captcha) {
    await storeCaptcha(username, captcha)
    const res = await apiLogin(username, password, captcha)
    const { token, role, username: uname, agentType } = res.data || res
    auth.token = token
    auth.user = { username: uname, role, agentType }
    auth.isLoggedIn = true
    localStorage.setItem('admin_token', token)
    localStorage.setItem('admin_user', JSON.stringify(auth.user))
    return auth.user
  }

  function logout() {
    auth.token = ''
    auth.user = null
    auth.isLoggedIn = false
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
  }

  function isSuperAdmin() {
    return auth.user?.role === 'SUPER_ADMIN'
  }

  function hasMenuAccess(menuKey) {
    if (auth.user?.role === 'SUPER_ADMIN') return true
    return canAccessMenu(menuKey, auth.user?.agentType)
  }

  function hasWriteAccess(moduleKey) {
    if (auth.user?.role === 'SUPER_ADMIN') return true
    return canWrite(moduleKey, auth.user?.agentType)
  }

  return { auth, doLogin, logout, isSuperAdmin, hasMenuAccess, hasWriteAccess }
}
