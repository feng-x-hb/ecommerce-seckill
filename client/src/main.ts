import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { useUserStore } from './stores/user'

const app = createApp(App)

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 刷新页面后：如果有 token，自动获取用户信息
const userStore = useUserStore()
if (userStore.token) {
  userStore.fetchMe().catch(() => {
    // token 无效时自动清空（401 拦截器会跳登录页）
  })
}

app.mount('#app')
