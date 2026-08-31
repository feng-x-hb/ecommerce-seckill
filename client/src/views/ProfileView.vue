<script setup lang="ts">
/**
 * 个人中心页（ProfileView.vue）
 * 路由：/profile
 * 功能：买家/商家可编辑昵称、头像、个性签名；管理员不可访问
 * 只读字段：商家显示手机号（只读）
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const nickname = ref('')
const avatar = ref('')
const signature = ref('')
const phone = ref('')
const username = ref('')
const userId = ref(0)
const role = ref(0)
const saving = ref(false)

// 预设头像列表
const avatarOptions = [
  'https://api.dicebear.com/7.x/adventurer/svg?seed=Felix',
  'https://api.dicebear.com/7.x/adventurer/svg?seed=Aneka',
  'https://api.dicebear.com/7.x/adventurer/svg?seed=Milo',
  'https://api.dicebear.com/7.x/adventurer/svg?seed=Jasper',
  'https://api.dicebear.com/7.x/adventurer/svg?seed=Luna',
  'https://api.dicebear.com/7.x/adventurer/svg?seed=Nala',
  'https://api.dicebear.com/7.x/adventurer/svg?seed=Cleo',
  'https://api.dicebear.com/7.x/adventurer/svg?seed=Simba',
]

const showAvatarPicker = ref(false)

// 修改账号密码
const showCredentialDialog = ref(false)
const credentialForm = reactive({
  oldPassword: '',
  newUsername: '',
  newPassword: ''
})
const credentialSaving = ref(false)

async function handleChangeCredential() {
  if (!credentialForm.oldPassword) {
    ElMessage.warning('请输入当前密码')
    return
  }
  if (!credentialForm.newUsername && !credentialForm.newPassword) {
    ElMessage.warning('请至少填写新账号或新密码')
    return
  }
  credentialSaving.value = true
  try {
    await request.put('/auth/credential', {
      oldPassword: credentialForm.oldPassword,
      newUsername: credentialForm.newUsername || undefined,
      newPassword: credentialForm.newPassword || undefined
    })
    await userStore.fetchMe()
    fetchProfile()
    showCredentialDialog.value = false
    credentialForm.oldPassword = ''
    credentialForm.newUsername = ''
    credentialForm.newPassword = ''
    ElMessage.success('修改成功')
  } catch (e: any) {
    ElMessage.error(e?.message || '修改失败')
  } finally {
    credentialSaving.value = false
  }
}

const roleLabel = computed(() => {
  if (role.value === 1) return '商家'
  if (role.value === 2) return '管理员'
  return '用户'
})

async function fetchProfile() {
  try {
    const res: any = await request.get('/auth/me')
    const data = res.data
    nickname.value = data.nickname || ''
    avatar.value = data.avatar || ''
    signature.value = data.signature || ''
    phone.value = data.phone || ''
    username.value = data.username || ''
    userId.value = data.id || 0
    role.value = data.role
  } catch {
    ElMessage.error('获取用户信息失败')
  }
}

async function handleSave() {
  if (!nickname.value.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  saving.value = true
  try {
    await request.put('/auth/profile', {
      nickname: nickname.value.trim(),
      avatar: avatar.value,
      signature: signature.value.trim()
    })
    await userStore.fetchMe()
    ElMessage.success('保存成功')
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function selectAvatar(url: string) {
  avatar.value = url
  showAvatarPicker.value = false
}

onMounted(() => {
  if (!userStore.userInfo) {
    router.push('/login')
    return
  }
  fetchProfile()
})
</script>

<template>
  <div class="profile-page">
    <div class="profile-container container">
      <!-- 左侧：头像区域 -->
      <div class="profile-left">
        <div class="avatar-section">
          <div class="avatar-wrapper" @click="showAvatarPicker = true">
            <img v-if="avatar" :src="avatar" class="avatar-img" alt="头像" />
            <div v-else class="avatar-placeholder">
              <el-icon :size="48" color="#ccc"><User /></el-icon>
            </div>
            <div class="avatar-overlay">
              <el-icon :size="20"><Camera /></el-icon>
              <span>更换头像</span>
            </div>
          </div>
          <div class="user-meta">
            <span class="role-badge" :class="['role-' + role]">{{ roleLabel }}</span>
            <span class="username-text">@{{ username }}</span>
            <span class="user-id-text" v-if="userId">UID: {{ userId }}</span>
          </div>
        </div>

        <!-- 头像选择弹窗 -->
        <div v-if="showAvatarPicker" class="avatar-picker">
          <div class="picker-title">选择头像</div>
          <div class="picker-grid">
            <div
              v-for="url in avatarOptions"
              :key="url"
              class="picker-item"
              :class="{ active: avatar === url }"
              @click="selectAvatar(url)"
            >
              <img :src="url" alt="avatar" />
            </div>
          </div>
          <button class="picker-close" @click="showAvatarPicker = false">取消</button>
        </div>
      </div>

      <!-- 右侧：表单区域 -->
      <div class="profile-right">
        <div class="form-card">
          <h2 class="form-title">个人资料</h2>
          <p class="form-desc">管理您的账户信息</p>

          <!-- 昵称 -->
          <div class="form-group">
            <label class="form-label">昵称</label>
            <el-input
              v-model="nickname"
              placeholder="请输入昵称"
              maxlength="20"
              show-word-limit
              class="jd-input"
            />
          </div>

          <!-- 个性签名 -->
          <div class="form-group">
            <label class="form-label">个性签名</label>
            <el-input
              v-model="signature"
              type="textarea"
              :rows="3"
              placeholder="写一句个性签名吧~"
              maxlength="100"
              show-word-limit
              class="jd-input"
            />
          </div>

          <!-- 手机号（只读，仅商家可见） -->
          <div v-if="role === 1" class="form-group">
            <label class="form-label">手机号</label>
            <el-input
              :model-value="phone"
              disabled
              class="jd-input"
            />
            <span class="field-hint">手机号不可修改</span>
          </div>

          <!-- 角色展示 -->
          <div class="form-group">
            <label class="form-label">身份</label>
            <div class="role-display">
              <span class="role-badge large" :class="['role-' + role]">{{ roleLabel }}</span>
            </div>
          </div>

          <!-- 保存按钮 -->
          <div class="form-actions">
            <button class="save-btn" :disabled="saving" @click="handleSave">
              <el-icon v-if="saving" class="is-loading"><Loading /></el-icon>
              {{ saving ? '保存中...' : '保存修改' }}
            </button>
            <button class="cancel-btn" @click="router.back()">取消</button>
            <button class="credential-btn" @click="showCredentialDialog = true">修改账号密码</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 修改账号密码弹窗 -->
    <el-dialog v-model="showCredentialDialog" title="修改账号密码" width="420px" :close-on-click-modal="false">
      <div class="credential-form">
        <div class="form-group">
          <label class="form-label">当前密码 <span class="required">*</span></label>
          <el-input
            v-model="credentialForm.oldPassword"
            type="password"
            placeholder="请输入当前密码以验证身份"
            show-password
            class="jd-input"
          />
        </div>
        <div class="form-group">
          <label class="form-label">新账号</label>
          <el-input
            v-model="credentialForm.newUsername"
            placeholder="留空则不修改"
            maxlength="20"
            class="jd-input"
          />
          <span class="field-hint">修改后可用新账号登录，账号必须唯一</span>
        </div>
        <div class="form-group">
          <label class="form-label">新密码</label>
          <el-input
            v-model="credentialForm.newPassword"
            type="password"
            placeholder="留空则不修改，修改需6-20位"
            show-password
            maxlength="20"
            class="jd-input"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="showCredentialDialog = false">取消</el-button>
        <el-button type="primary" :loading="credentialSaving" @click="handleChangeCredential">
          {{ credentialSaving ? '修改中...' : '确认修改' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: var(--jd-bg);
  padding: 30px 0 60px;
}
.profile-container {
  display: flex;
  gap: 30px;
  max-width: 900px;
}

/* 左侧头像 */
.profile-left {
  flex-shrink: 0;
  width: 280px;
}
.avatar-section {
  background: #fff;
  border-radius: 16px;
  padding: 32px 20px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 16px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 4px solid #f0f0f0;
  transition: border-color 0.3s;
}
.avatar-wrapper:hover {
  border-color: #e1251b;
  transform: scale(1.05);
}
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}
.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}
.user-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}
.role-badge {
  display: inline-block;
  padding: 2px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}
.role-badge.large {
  padding: 4px 16px;
  font-size: 14px;
}
.role-0 { background: #e8f4ff; color: #1677ff; }
.role-1 { background: #fff7e6; color: #fa8c16; }
.role-2 { background: #f6ffed; color: #52c41a; }
.username-text {
  font-size: 13px;
  color: #999;
}
.user-id-text {
  font-size: 12px;
  color: #bbb;
  font-family: monospace;
}

/* 头像选择器 */
.avatar-picker {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-top: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.picker-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 12px;
}
.picker-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-bottom: 12px;
}
.picker-item {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid transparent;
  cursor: pointer;
  transition: all 0.3s;
}
.picker-item:hover {
  transform: scale(1.1);
}
.picker-item.active {
  border-color: #e1251b;
  box-shadow: 0 0 0 2px rgba(225,37,27,0.3);
}
.picker-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.picker-close {
  width: 100%;
  padding: 6px;
  background: #f5f5f5;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: #666;
  transition: background 0.2s;
}
.picker-close:hover {
  background: #eee;
}

/* 右侧表单 */
.profile-right {
  flex: 1;
}
.form-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.form-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px;
}
.form-desc {
  font-size: 13px;
  color: #999;
  margin: 0 0 28px;
}
.form-group {
  margin-bottom: 22px;
}
.form-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}
.field-hint {
  font-size: 12px;
  color: #bbb;
  margin-top: 4px;
  display: block;
}
.jd-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e0e0e0 inset;
  transition: box-shadow 0.3s;
}
.jd-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #ccc inset;
}
.jd-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(225,37,27,0.3) inset;
}
.jd-input :deep(.el-textarea__inner) {
  border-radius: 10px;
  border: 1px solid #e0e0e0;
  transition: border-color 0.3s;
}
.jd-input :deep(.el-textarea__inner:focus) {
  border-color: #e1251b;
}

.role-display {
  display: flex;
  align-items: center;
}

.form-actions {
  display: flex;
  gap: 12px;
  padding-top: 12px;
}
.save-btn {
  background: linear-gradient(135deg, #e1251b, #ff6700);
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 10px 32px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s;
}
.save-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(225,37,27,0.35);
}
.save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.cancel-btn {
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 10px;
  padding: 10px 24px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}
.cancel-btn:hover {
  background: #eee;
}
.credential-btn {
  background: #fff;
  color: #666;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 10px 20px;
  font-size: 14px;
  cursor: pointer;
  margin-left: auto;
  transition: all 0.3s;
}
.credential-btn:hover {
  color: #e1251b;
  border-color: #e1251b;
}
.credential-form .required {
  color: #e1251b;
  margin-left: 2px;
}

@media (max-width: 768px) {
  .profile-container {
    flex-direction: column;
  }
  .profile-left {
    width: 100%;
  }
}
</style>
