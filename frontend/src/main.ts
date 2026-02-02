import { createApp } from 'vue'
import './style.css'
import App from './App.vue'

// 导入Vue Router
import router from './router'

// 导入Pinia
import { createPinia } from 'pinia'

// 导入Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 导入Element Plus图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 创建应用实例
const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用插件
app.use(router)
app.use(createPinia())
app.use(ElementPlus)

// 挂载应用
app.mount('#app')

