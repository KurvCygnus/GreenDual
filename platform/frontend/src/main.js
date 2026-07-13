import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import App from './App.vue'
import router from './router'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// ========== 全局错误边界 ==========

// 1. Vue 渲染/计算/watch 异常
app.config.errorHandler = (err, instance, info) => {
  console.error('[Vue 全局错误]', err, info)
  ElMessage.error({
    message: '页面渲染异常，请尝试刷新',
    duration: 5000,
  })
}

// 2. 未捕获的运行时错误（资源加载失败等）
window.addEventListener('error', (event) => {
  console.error('[运行时错误]', event.error || event.message)
  // 避免跨域脚本错误（Script error.）弹消息
  if (event.message && event.message !== 'Script error.') {
    ElMessage.error({
      message: '页面发生异常，请刷新后重试',
      duration: 4000,
    })
  }
})

// 3. 未处理的 Promise rejection
window.addEventListener('unhandledrejection', (event) => {
  console.error('[未处理的 Promise 异常]', event.reason)
  ElMessage.error({
    message: '请求异常，请检查网络后重试',
    duration: 4000,
  })
})

app.mount('#app')
