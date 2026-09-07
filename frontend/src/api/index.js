import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_user')
        if (window.location.pathname.startsWith('/manage') && !window.location.pathname.includes('/manage/login')) {
          window.location.href = '/manage/login'
        }
      }
      const message = typeof data === 'string' ? data : (data?.message || data?.error || '请求失败')
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

export default request
