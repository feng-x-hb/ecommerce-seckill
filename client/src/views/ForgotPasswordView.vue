<script setup lang="ts">
/**
 * 找回密码页 - 账号名找回 / 手机号找回
 */
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const router = useRouter()
const isDark = ref(document.documentElement.getAttribute('data-theme') === 'dark')
const resetMode = ref<'account' | 'phone'>('account')

let _observer: MutationObserver | null = null
onMounted(() => {
  _observer = new MutationObserver(() => {
    isDark.value = document.documentElement.getAttribute('data-theme') === 'dark'
  })
  _observer.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] })
})
onUnmounted(() => { _observer?.disconnect() })

// 账号名找回
const accountForm = reactive({ username: '', newPassword: '', confirmPassword: '' })
const accountRules = {
  username: [{ required: true, message: '请输入账号名', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string, callback: (err?: Error) => void) => {
        if (value !== accountForm.newPassword) {
          callback(new Error('两次密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 手机号找回
const phoneForm = reactive({ phone: '', code: '', newPassword: '', confirmPassword: '' })
const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string, callback: (err?: Error) => void) => {
        if (value !== phoneForm.newPassword) {
          callback(new Error('两次密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const accountFormRef = ref()
const phoneFormRef = ref()

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
  if (phoneForm.phone && /^1[3-9]\d{9}$/.test(phoneForm.phone) && countdown.value === 0) {
    ElMessage.success('验证码已发送')
    startCountdown()
  }
}

async function handleAccountReset() {
  const valid = await accountFormRef.value?.validate().catch(() => false)
  if (!valid) return
  await request.post('/auth/reset-password', { username: accountForm.username, newPassword: accountForm.newPassword })
  ElMessage.success('密码已重置，请登录')
  router.push('/login')
}

async function handlePhoneReset() {
  const valid = await phoneFormRef.value?.validate().catch(() => false)
  if (!valid) return
  await request.post('/auth/reset-password', { phone: phoneForm.phone, newPassword: phoneForm.newPassword })
  ElMessage.success('密码已重置，请登录')
  router.push('/login')
}
</script>

<template>
  <div class="login-page" :class="{ dark: isDark }">
    <!-- 主内容区 -->
    <div class="login-body">
      <div class="login-card forgot-card">
        <div class="card-right full-width">
          <h2 class="forgot-title">找回密码</h2>

          <!-- 切换标签 -->
          <div class="form-tabs">
            <span class="form-tab" :class="{ active: resetMode === 'account' }" @click="resetMode = 'account'">账号名找回</span>
            <span class="form-tab-divider">|</span>
            <span class="form-tab" :class="{ active: resetMode === 'phone' }" @click="resetMode = 'phone'">手机号找回</span>
          </div>

          <!-- 账号名找回 -->
          <el-form v-if="resetMode === 'account'" :key="'reset-account'" ref="accountFormRef" :model="accountForm" :rules="accountRules" label-width="0">
            <el-form-item prop="username">
              <el-input v-model="accountForm.username" placeholder="请输入账号名" size="large" class="jd-input">
                <template #prefix><el-icon><User /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="newPassword">
              <el-input v-model="accountForm.newPassword" type="password" placeholder="新密码" size="large" show-password class="jd-input">
                <template #prefix><el-icon><Lock /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input v-model="accountForm.confirmPassword" type="password" placeholder="确认新密码" size="large" show-password class="jd-input">
                <template #prefix><el-icon><Lock /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <button type="button" class="login-btn" @click="handleAccountReset">确认重置</button>
            </el-form-item>
          </el-form>

          <!-- 手机号找回 -->
          <el-form v-if="resetMode === 'phone'" :key="'reset-phone'" ref="phoneFormRef" :model="phoneForm" :rules="phoneRules" label-width="0">
            <el-form-item prop="phone">
              <el-input v-model="phoneForm.phone" placeholder="手机号" size="large" class="jd-input">
                <template #prefix><el-icon><Iphone /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="code">
              <div class="sms-row">
                <el-input v-model="phoneForm.code" placeholder="验证码" size="large" class="jd-input sms-input">
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
            <el-form-item prop="newPassword">
              <el-input v-model="phoneForm.newPassword" type="password" placeholder="新密码" size="large" show-password class="jd-input">
                <template #prefix><el-icon><Lock /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input v-model="phoneForm.confirmPassword" type="password" placeholder="确认新密码" size="large" show-password class="jd-input">
                <template #prefix><el-icon><Lock /></el-icon></template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <button type="button" class="login-btn" @click="handlePhoneReset">确认重置</button>
            </el-form-item>
          </el-form>

          <div class="back-link">
            <router-link to="/login">← 返回登录</router-link>
          </div>
        </div>
      </div>
    </div>

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
}
.theme-toggle:hover { transform: scale(1.1); }

.login-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 30px;
}

.forgot-card {
  width: 100%;
  max-width: 460px;
  border-radius: 16px;
  box-shadow: 0 4px 30px rgba(0,0,0,0.08);
  background: #fff;
  border: 1px solid #f0f0f0;
  padding: 50px 40px;
}
.dark .forgot-card {
  background: #2a2a2a;
  border-color: #444;
  box-shadow: 0 4px 30px rgba(0,0,0,0.3);
}

.full-width { width: 100%; }

.forgot-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #333;
}
.dark .forgot-title { color: #fff; }

.form-tabs {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 30px;
}
.form-tab {
  font-size: 16px;
  cursor: pointer;
  color: #999;
  transition: color 0.3s;
}
.form-tab.active {
  color: #e1251b;
  font-weight: 600;
}
.form-tab-divider { color: #ddd; }
.dark .form-tab-divider { color: #555; }

.sms-row { display: flex; gap: 10px; width: 100%; }
.sms-input { flex: 1; }
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
.sms-btn:hover:not(.disabled) { background: #e1251b; color: #fff; }
.sms-btn.disabled { border-color: #ccc; color: #999; cursor: not-allowed; }

.jd-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e0e0e0 inset;
  background: #f8f9fa;
}
.dark .jd-input :deep(.el-input__wrapper) {
  background: #3a3a3a;
  box-shadow: 0 0 0 1px #555 inset;
}
.jd-input :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 2px #e1251b inset; }
.jd-input :deep(.el-input__inner) { color: #333; }
.dark .jd-input :deep(.el-input__inner) { color: #e0e0e0; }

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

.back-link {
  text-align: center;
  margin-top: 20px;
}
.back-link a {
  font-size: 13px;
  color: #999;
  text-decoration: none;
  transition: color 0.2s;
}
.back-link a:hover { color: #e1251b; }

.login-footer {
  padding: 24px 30px;
  text-align: center;
  border-top: 1px solid #f0f0f0;
}
.dark .login-footer { border-top-color: #333; }
.footer-links { display: flex; align-items: center; justify-content: center; gap: 16px; }
.footer-links a { font-size: 12px; color: #999; text-decoration: none; transition: color 0.2s; }
.footer-links a:hover { color: #e1251b; }
.footer-links span { color: #ddd; font-size: 10px; }
.dark .footer-links span { color: #555; }
</style>
