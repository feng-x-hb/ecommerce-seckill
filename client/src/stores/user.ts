/**
 * 用户状态管理（Pinia Store）
 *
 * 职责：
 *   1. 存储 token 和用户信息
 *   2. 提供 login / logout / fetchMe 方法
 *   3. 持久化到 localStorage（刷新页面不丢失）
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/api/request'

export interface UserInfo {
  id: number
  account: string
  nickname: string
  avatar: string
  signature: string
  role: number
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  // 从 localStorage 恢复用户信息
  const savedUser = localStorage.getItem('user')
  if (savedUser) {
    try { userInfo.value = JSON.parse(savedUser) } catch {}
  }

  /** 登录 */
  async function login(account: string, password: string) {
    const res: any = await request.post('/auth/login', { account, password })
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    // 登录返回的基本信息也存起来
    const basicInfo = { id: res.data.userId, nickname: res.data.nickname, role: res.data.role }
    userInfo.value = basicInfo as UserInfo
    localStorage.setItem('user', JSON.stringify(basicInfo))
  }

  /** 获取当前用户信息 */
  async function fetchMe() {
    const res: any = await request.get('/auth/me')
    userInfo.value = res.data
    localStorage.setItem('user', JSON.stringify(res.data))
  }

  /** 登出 */
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return { token, userInfo, login, fetchMe, logout }
})
