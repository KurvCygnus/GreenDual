import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      // 将 /api 开头的请求代理到后端（如果后端路径统一加了前缀）
      // 但当前后端未使用统一前缀，故按路径逐个代理
      '/login': { target: 'http://localhost:18081', changeOrigin: true },
      '/device': { target: 'http://localhost:18081', changeOrigin: true },
      '/lamp': { target: 'http://localhost:18081', changeOrigin: true },
      '/user': { target: 'http://localhost:18081', changeOrigin: true },
      '/ai': { target: 'http://localhost:18081', changeOrigin: true },
      '/campus': { target: 'http://localhost:18081', changeOrigin: true },
    }
  }
})