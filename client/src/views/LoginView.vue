<script setup lang="ts">
/**
 * 登录页 - 仿京东风格
 * 密码登录 / 短信登录 + 60s倒计时 + 日夜切换
 */
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const router = useRouter()
const userStore = useUserStore()
const isRegister = ref(false)
const loginMode = ref<'password' | 'sms'>('password')
const isDark = ref(localStorage.getItem('theme') === 'dark')

// 密码登录
const loginForm = reactive({ account: '', password: '' })
const loginRules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 短信登录
const smsForm = reactive({ phone: '', code: '' })
const smsRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为4位', trigger: 'blur' }
  ]
}

// 普通注册
const registerForm = reactive({ username: '', password: '', nickname: '' })
const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

// 电话注册
const phoneRegisterForm = reactive({ phone: '', code: '', username: '' })
const phoneRegisterRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
}

const loginFormRef = ref()
const smsFormRef = ref()
const registerFormRef = ref()
const phoneRegisterFormRef = ref()

// 60s 倒计时
const countdown = ref(0)
let countdownTimer: ReturnType<typeof setInterval> | null = null

function startCountdown() {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      if (countdownTimer) clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})

function sendSmsCode() {
  const phone = isRegister.value ? phoneRegisterForm.phone : smsForm.phone
  if (phone && /^1[3-9]\d{9}$/.test(phone) && countdown.value === 0) {
    ElMessage.success('验证码已发送')
    startCountdown()
  }
}

function toggleTheme() {
  isDark.value = !isDark.value
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
}

async function handleLogin() {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return
  await userStore.login(loginForm.account, loginForm.password)
  await userStore.fetchMe()
  ElMessage.success('登录成功')
  const r = userStore.userInfo?.role
  if (r === 2) { router.push('/admin') } else if (r === 1) { router.push('/seller') } else { router.push('/') }
}

async function handleSmsLogin() {
  const valid = await smsFormRef.value?.validate().catch(() => false)
  if (!valid) return
  if (smsForm.code !== '1234') {
    ElMessage.error('验证码错误')
    return
  }
  await userStore.login(smsForm.phone, '123456')
  await userStore.fetchMe()
  ElMessage.success('登录成功')
  const r = userStore.userInfo?.role
  if (r === 2) { router.push('/admin') } else if (r === 1) { router.push('/seller') } else { router.push('/') }
}

async function handleRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return
  await request.post('/auth/register', { username: registerForm.username, password: registerForm.password })
  ElMessage.success('注册成功，请登录')
  isRegister.value = false
  loginForm.account = registerForm.username
  loginForm.password = ''
}

async function handlePhoneRegister() {
  const valid = await phoneRegisterFormRef.value?.validate().catch(() => false)
  if (!valid) return
  await request.post('/auth/register', { username: phoneRegisterForm.username, password: '123456', phone: phoneRegisterForm.phone })
  ElMessage.success('注册成功，请登录')
  isRegister.value = false
  loginForm.account = phoneRegisterForm.username
  loginForm.password = ''
}
</script>

<template>
  <div class="login-page" :class="{ dark: isDark }">
    <!-- 主内容区 -->
    <div class="login-body">
      <!-- 登录卡片 -->
      <div class="login-card">
        <!-- 左侧装饰区 -->
        <div class="card-left">
          <div class="left-content">
            <img src="/images/优购logo设计.png" alt="优购" class="big-logo" />
            <p class="left-title">欢迎回来</p>
            <p class="left-subtitle">品质生活 · 从这里开始</p>
          </div>
        </div>

        <!-- 右侧表单区 -->
        <div class="card-right">
          <!-- 用户登录标题 -->
          <h2 class="page-title">用户登录</h2>

          <!-- 登录标签 -->
          <div v-if="!isRegister" class="form-tabs">
            <span
              class="form-tab"
              :class="{ active: loginMode === 'password' }"
              @click="loginMode = 'password'"
            >密码登录</span>
            <span class="form-tab-divider">|</span>
            <span
              class="form-tab"
              :class="{ active: loginMode === 'sms' }"
              @click="loginMode = 'sms'"
            >短信登录</span>
          </div>
          <!-- 注册标签 -->
          <div v-else class="form-tabs">
            <span
              class="form-tab"
              :class="{ active: loginMode === 'password' }"
              @click="loginMode = 'password'"
            >普通注册</span>
            <span class="form-tab-divider">|</span>
            <span
              class="form-tab"
              :class="{ active: loginMode === 'sms' }"
              @click="loginMode = 'sms'"
            >电话注册</span>
          </div>

          <!-- 密码登录表单 -->
          <el-form v-if="loginMode === 'password' && !isRegister" :key="'login-pwd'" ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="0">
            <el-form-item prop="account">
              <el-input v-model="loginForm.account" placeholder="账号名/手机号/邮箱" size="large" class="jd-input">
                <template #prefix><el-icon><User /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="密码" size="large" show-password class="jd-input" @keyup.enter="handleLogin">
                <template #prefix><el-icon><Lock /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <button type="button" class="login-btn" @click="handleLogin">登 录</button>
            </el-form-item>
          </el-form>

          <!-- 短信登录表单 -->
          <el-form v-if="loginMode === 'sms' && !isRegister" :key="'login-sms'" ref="smsFormRef" :model="smsForm" :rules="smsRules" label-width="0">
            <el-form-item prop="phone">
              <el-input v-model="smsForm.phone" placeholder="手机号" size="large" class="jd-input">
                <template #prefix><el-icon><Iphone /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="code">
              <div class="sms-row">
                <el-input v-model="smsForm.code" placeholder="验证码" size="large" class="jd-input sms-input" @keyup.enter="handleSmsLogin">
                  <template #prefix><el-icon><Message /></el-icon></template>
                </el-input>
                <button
                  type="button"
                  class="sms-btn"
                  :class="{ disabled: countdown > 0 }"
                  :disabled="countdown > 0"
                  @click="sendSmsCode"
                >
                  {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                </button>
              </div>
            </el-form-item>
            <el-form-item>
              <button type="button" class="login-btn" @click="handleSmsLogin">登 录</button>
            </el-form-item>
          </el-form>

          <!-- 普通注册表单 -->
          <el-form v-if="isRegister && loginMode === 'password'" :key="'reg-pwd'" ref="registerFormRef" :model="registerForm" :rules="registerRules" label-width="0">
            <el-form-item prop="username">
              <el-input v-model="registerForm.username" placeholder="请输入用户名" size="large" class="jd-input">
                <template #prefix><el-icon><User /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="nickname">
              <el-input v-model="registerForm.nickname" placeholder="请输入昵称" size="large" class="jd-input">
                <template #prefix><el-icon><UserFilled /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" size="large" show-password class="jd-input">
                <template #prefix><el-icon><Lock /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <button type="button" class="login-btn register-btn" @click="handleRegister">注 册</button>
            </el-form-item>
          </el-form>

          <!-- 电话注册表单 -->
          <el-form v-if="isRegister && loginMode === 'sms'" :key="'reg-sms'" ref="phoneRegisterFormRef" :model="phoneRegisterForm" :rules="phoneRegisterRules" label-width="0">
            <el-form-item prop="phone">
              <el-input v-model="phoneRegisterForm.phone" placeholder="手机号" size="large" class="jd-input">
                <template #prefix><el-icon><Iphone /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="code">
              <div class="sms-row">
                <el-input v-model="phoneRegisterForm.code" placeholder="验证码" size="large" class="jd-input sms-input">
                  <template #prefix><el-icon><Message /></el-icon></template>
                </el-input>
                <button
                  type="button"
                  class="sms-btn"
                  :class="{ disabled: countdown > 0 }"
                  :disabled="countdown > 0"
                  @click="sendSmsCode"
                >
                  {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                </button>
              </div>
            </el-form-item>
            <el-form-item prop="username">
              <el-input v-model="phoneRegisterForm.username" placeholder="请输入用户名" size="large" class="jd-input">
                <template #prefix><el-icon><User /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <button type="button" class="login-btn register-btn" @click="handlePhoneRegister">注 册</button>
            </el-form-item>
          </el-form>

          <!-- 底部链接 -->
          <div class="form-footer">
            <a href="javascript:;" class="footer-link" @click="isRegister = !isRegister; loginMode = 'password'">
              {{ isRegister ? '已有账号？去登录' : '立即注册' }}
            </a>
            <span class="footer-divider">|</span>
            <router-link to="/forgot-password" class="footer-link">忘记密码</router-link>
            <span class="footer-divider">|</span>
            <router-link to="/" class="footer-link">不登陆，先浏览</router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部 -->
    <div class="login-footer">
      <div class="footer-links">
        <router-link to="/about">关于我们</router-link>
        <span>|</span>
        <router-link to="/contact">联系我们</router-link>
        <span>|</span>
        <router-link to="/merchant-login">商家入驻</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #fff;
  color: #333;
  transition: background 0.5s, color 0.5s;
}
.login-page.dark {
  background: #1a1a1a;
  color: #e0e0e0;
}

/* 日夜切换按钮 */
.theme-toggle {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 100;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid #e0e0e0;
  background: #fff;
  color: #333;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.dark .theme-toggle {
  background: #333;
  color: #ffd700;
  border-color: #555;
  box-shadow: 0 2px 8px rgba(0,0,0,0.4);
}
.theme-toggle:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

/* 顶部 */
.login-header {
  border-bottom: 1px solid #f0f0f0;
}
.dark .login-header {
  border-bottom-color: #333;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 30px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.logo-area {
  display: flex;
  align-items: center;
  gap: 10px;
}
.header-logo {
  width: 40px;
  height: 40px;
  object-fit: contain;
  border-radius: 8px;
}
.logo-text {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}
.dark .logo-text {
  color: #fff;
}
.header-right {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}
.header-right::before {
  content: '😊';
  font-size: 14px;
}

/* 主内容区 */
.login-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 30px;
}

/* 登录卡片 */
.login-card {
  width: 100%;
  max-width: 900px;
  min-height: 420px;
  display: flex;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 30px rgba(0,0,0,0.08);
  background: #fff;
  border: 1px solid #f0f0f0;
}
.dark .login-card {
  background: #2a2a2a;
  border-color: #444;
  box-shadow: 0 4px 30px rgba(0,0,0,0.3);
}

/* 左侧 */
.card-left {
  width: 380px;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.dark .card-left {
  background: linear-gradient(135deg, #333, #2a2a2a);
}
.left-content {
  text-align: center;
}
.big-logo {
  width: 80px;
  height: 80px;
  object-fit: contain;
  margin-bottom: 20px;
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(225,37,27,0.3);
}
.left-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}
.dark .left-title {
  color: #fff;
}
.left-subtitle {
  font-size: 14px;
  color: #999;
}

/* 右侧表单 */
.card-right {
  flex: 1;
  padding: 40px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.page-title { font-size: 22px; font-weight: 600; color: #333; margin-bottom: 20px; }
.dark .page-title { color: #fff; }

/* 表单内标签 */
.form-tabs {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 30px;
}
.form-tab {
  font-size: 18px;
  cursor: pointer;
  color: #999;
  transition: color 0.3s;
}
.form-tab.active {
  color: #e1251b;
  font-weight: 600;
}
.form-tab-divider {
  color: #ddd;
}
.dark .form-tab-divider {
  color: #555;
}

/* 短信验证码行 */
.sms-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.sms-input {
  flex: 1;
}
.sms-btn {
  height: 40px;
  padding: 0 16px;
  border: 1px solid #e1251b;
  background: #fff;
  color: #e1251b;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  white-space: nowrap;
  transition: all 0.3s;
  flex-shrink: 0;
}
.sms-btn:hover:not(.disabled) {
  background: #e1251b;
  color: #fff;
}
.sms-btn.disabled {
  border-color: #ccc;
  color: #999;
  cursor: not-allowed;
}

/* 输入框 */
.jd-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e0e0e0 inset;
  background: #f8f9fa;
  transition: all 0.3s;
}
.dark .jd-input :deep(.el-input__wrapper) {
  background: #3a3a3a;
  box-shadow: 0 0 0 1px #555 inset;
}
.jd-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #ccc inset;
}
.jd-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #e1251b inset;
}
.dark .jd-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #e1251b inset;
}
.jd-input :deep(.el-input__inner) {
  color: #333;
}
.dark .jd-input :deep(.el-input__inner) {
  color: #e0e0e0;
}
.jd-input :deep(.el-input__prefix) {
  color: #999;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #e1251b, #ff4e3a);
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 6px;
  cursor: pointer;
  transition: all 0.3s;
}
.login-btn:hover {
  filter: brightness(1.05);
  box-shadow: 0 4px 16px rgba(225,37,27,0.4);
  transform: translateY(-1px);
}
.login-btn:active {
  transform: translateY(0);
}
.register-btn {
  background: linear-gradient(135deg, #ff6700, #ff8533);
}

/* 底部链接 */
.form-footer {
  margin-top: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
}
.footer-link {
  color: #999;
  cursor: pointer;
  transition: color 0.2s;
  text-decoration: none;
}
.footer-link:hover {
  color: #e1251b;
}
.footer-divider {
  color: #ddd;
}
.dark .footer-divider {
  color: #555;
}

/* 页面底部 */
.login-footer {
  padding: 24px 30px;
  text-align: center;
  border-top: 1px solid #f0f0f0;
}
.dark .login-footer {
  border-top-color: #333;
}
.footer-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}
.footer-links a {
  font-size: 12px;
  color: #999;
  transition: color 0.2s;
  text-decoration: none;
}
.footer-links a:hover {
  color: #e1251b;
}
.footer-links span {
  color: #ddd;
  font-size: 10px;
}
.dark .footer-links span {
  color: #555;
}
</style>
