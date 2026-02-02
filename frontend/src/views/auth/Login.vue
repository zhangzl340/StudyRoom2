<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const loginForm = ref({ username: '', password: '', })
const loginFormRef = ref(null)
const isMobile = ref(false)

const loginRules = {
  username: [
    { required: true, trigger: "blur", message: "请输入您的账号" }
  ],
  password: [
    { required: true, trigger: "blur", message: "请输入您的密码" }
  ],
}

// 检测设备类型
const checkDevice = () => {
  isMobile.value = window.innerWidth <= 768
}

// 处理登录
const handleLogin = async () => {
  if (loginFormRef.value) {
    loginFormRef.value.validate(async (valid) => {
      if (valid) {
        loading.value = true
        try {
          await authStore.login(loginForm.value.username, loginForm.value.password)
          ElMessage.success('登录成功')
          router.replace('/student/dashboard')
        } catch (error) {
          ElMessage.error(error.message || '登录失败')
        } finally {
          loading.value = false
        }
      }
    })
  }
}

// 监听窗口大小变化
onMounted(() => {
  checkDevice()
  window.addEventListener('resize', checkDevice)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkDevice)
})
</script>

<template>
  <div class="login-container" :class="{ 'mobile-view': isMobile }">
    <!-- 左侧装饰区（桌面端） -->
    <div class="login-left" v-if="!isMobile">
      <div class="left-content">
        <h1 class="welcome-title">欢迎回来</h1>
        <p class="welcome-subtitle">高校智能自习室管理系统</p>
        <div class="features">
          <div class="feature-item">
            <div class="feature-icon">📚</div>
            <div class="feature-text">
              <h3>智能预约</h3>
              <p>快速预约心仪自习座位</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">⏰</div>
            <div class="feature-text">
              <h3>时间管理</h3>
              <p>合理安排学习时间</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">📊</div>
            <div class="feature-text">
              <h3>学习统计</h3>
              <p>记录学习时长与进度</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单区 -->
    <div class="login-right">
      <div class="login-form-wrapper">
        <!-- 移动端标题 -->
        <div class="mobile-header" v-if="isMobile">
          <h1 class="app-title">自习室预约</h1>
          <p class="app-subtitle">高校智能管理系统</p>
        </div>

        <el-form 
          class="login-form" 
          ref="loginFormRef" 
          :model="loginForm" 
          :rules="loginRules"
          :size="isMobile ? 'large' : 'default'"
        >
          <!-- 桌面端标题 -->
          <div class="desktop-header" v-if="!isMobile">
            <h2 class="form-title">用户登录</h2>
            <p class="form-subtitle">请使用学号和密码登录</p>
          </div>

          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              type="text" 
              placeholder="学号/账号" 
              :prefix-icon="User"
              :clearable="!isMobile"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="密码" 
              :prefix-icon="Lock"
              :show-password="!isMobile"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button 
              :loading="loading" 
              type="primary" 
              :class="{ 'mobile-btn': isMobile }"
              @click="handleLogin"
            >
              <span v-if="!loading">登 录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
          
          <div class="form-actions">
            <div class="remember-me" v-if="!isMobile">
              <el-checkbox>记住我</el-checkbox>
            </div>
            <div class="forgot-password">
              <el-link type="primary" :underline="false" size="small">
                忘记密码？
              </el-link>
            </div>
          </div>
          
          <div class="register-section">
            <span class="register-tip">还没有账号？</span>
            <el-link 
              type="primary" 
              :underline="false"
              @click="router.push('/register')"
            >
              立即注册
            </el-link>
          </div>

          <!-- 移动端快捷方式 -->
          <div class="mobile-quick-links" v-if="isMobile">
            <div class="quick-link">
              <el-link type="info" :underline="false">
                游客浏览
              </el-link>
            </div>
            <div class="quick-link">
              <el-link type="info" :underline="false">
                常见问题
              </el-link>
            </div>
          </div>
        </el-form>

        <!-- 桌面端底部信息 -->
        <div class="desktop-footer" v-if="!isMobile">
          <div class="footer-links">
            <el-link type="info" :underline="false" size="small">关于我们</el-link>
            <el-link type="info" :underline="false" size="small">使用条款</el-link>
            <el-link type="info" :underline="false" size="small">隐私政策</el-link>
            <el-link type="info" :underline="false" size="small">帮助中心</el-link>
          </div>
        </div>
      </div>

      <!-- 移动端底部 -->
      <div class="mobile-footer" v-if="isMobile">
        <div class="footer-content">
          <p class="footer-text">高校智能自习室系统 © 2026</p>
          <p class="footer-contact">技术支持：it-support@university.edu</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  
  &.mobile-view {
    flex-direction: column;
    background: #f0f2f5;
  }
}

/* 左侧装饰区（桌面端） */
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100" preserveAspectRatio="none"><path d="M0,0 L100,0 L100,100 Z" fill="rgba(255,255,255,0.1)"/></svg>');
    background-size: cover;
  }
}

.left-content {
  position: relative;
  z-index: 1;
  max-width: 500px;
  width: 100%;
}

.welcome-title {
  font-size: 2.8rem;
  font-weight: 700;
  margin-bottom: 1rem;
  line-height: 1.2;
}

.welcome-subtitle {
  font-size: 1.2rem;
  opacity: 0.9;
  margin-bottom: 3rem;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 1.2rem;
  opacity: 0.9;
  transition: opacity 0.3s ease;
  
  &:hover {
    opacity: 1;
  }
}

.feature-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.feature-text {
  h3 {
    font-size: 1.3rem;
    font-weight: 600;
    margin-bottom: 0.3rem;
  }
  
  p {
    opacity: 0.8;
    font-size: 0.95rem;
  }
}

/* 右侧登录区 */
.login-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
  
  .mobile-view & {
    flex: none;
    width: 100%;
    padding: 20px 15px;
  }
}

.login-form-wrapper {
  width: 100%;
  max-width: 400px;
  
  .mobile-view & {
    max-width: 100%;
  }
}

/* 移动端标题 */
.mobile-header {
  text-align: center;
  margin-bottom: 2rem;
}

.app-title {
  font-size: 1.8rem;
  color: #2c3e50;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.app-subtitle {
  color: #7f8c8d;
  font-size: 0.95rem;
}

/* 桌面端标题 */
.desktop-header {
  margin-bottom: 2.5rem;
}

.form-title {
  font-size: 1.8rem;
  color: #2c3e50;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.form-subtitle {
  color: #7f8c8d;
  font-size: 0.95rem;
}

/* 登录表单 */
.login-form {
  background: white;
  padding: 2.5rem;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
  
  .mobile-view & {
    padding: 1.5rem;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  }
}

:deep(.el-form-item) {
  margin-bottom: 1.5rem;
  
  .mobile-view & {
    margin-bottom: 1.2rem;
  }
}

:deep(.el-input) {
  .el-input__wrapper {
    border-radius: 10px;
    padding: 0.8rem 1rem;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }
    
    .mobile-view & {
      padding: 0.9rem 1rem;
      border-radius: 12px;
    }
  }
  
  &.is-focus .el-input__wrapper {
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
  }
}

:deep(.el-button) {
  width: 100%;
  padding: 1rem;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
  }
  
  &:active {
    transform: translateY(0);
  }
  
  &.mobile-btn {
    padding: 1.1rem;
    font-size: 1.05rem;
    border-radius: 12px;
  }
}

/* 表单操作 */
.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  
  .mobile-view & {
    margin-bottom: 1rem;
  }
}

.remember-me {
  :deep(.el-checkbox) {
    .el-checkbox__label {
      color: #7f8c8d;
      font-size: 0.9rem;
    }
  }
}

/* 注册区域 */
.register-section {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid #eee;
  
  .register-tip {
    color: #7f8c8d;
    font-size: 0.9rem;
  }
  
  .mobile-view & {
    padding-top: 1rem;
  }
}

/* 移动端快捷链接 */
.mobile-quick-links {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #eee;
}

.quick-link {
  :deep(.el-link) {
    font-size: 0.9rem;
  }
}

/* 桌面端底部 */
.desktop-footer {
  margin-top: 2rem;
  text-align: center;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  
  :deep(.el-link) {
    font-size: 0.85rem;
  }
}

/* 移动端底部 */
.mobile-footer {
  margin-top: 2rem;
  text-align: center;
  padding: 1.5rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  width: 100%;
}

.footer-content {
  .footer-text {
    color: #7f8c8d;
    font-size: 0.85rem;
    margin-bottom: 0.5rem;
  }
  
  .footer-contact {
    color: #95a5a6;
    font-size: 0.8rem;
  }
}

/* 响应式断点 */
@media (max-width: 1200px) {
  .welcome-title {
    font-size: 2.4rem;
  }
}

@media (max-width: 992px) {
  .login-left {
    padding: 30px;
  }
  
  .welcome-title {
    font-size: 2rem;
  }
  
  .feature-text h3 {
    font-size: 1.1rem;
  }
}

@media (max-width: 768px) {
  .login-container {
    &.mobile-view {
      min-height: 100vh;
      overflow-y: auto;
    }
  }
  
  .login-right {
    min-height: calc(100vh - 60px);
  }
}

@media (max-width: 480px) {
  .login-form {
    padding: 1.2rem;
  }
  
  .app-title {
    font-size: 1.6rem;
  }
  
  :deep(.el-button).mobile-btn {
    padding: 1rem;
    font-size: 1rem;
  }
}

@media (max-width: 360px) {
  .mobile-quick-links {
    flex-direction: column;
    gap: 0.8rem;
    align-items: center;
  }
  
  .register-section {
    flex-direction: column;
    gap: 0.3rem;
  }
}

/* 打印样式 */
@media print {
  .login-container {
    background: white !important;
  }
  
  .login-left,
  .mobile-footer,
  .form-actions,
  .register-section,
  .mobile-quick-links,
  .desktop-footer {
    display: none !important;
  }
  
  .login-right {
    width: 100%;
    padding: 0;
  }
  
  .login-form-wrapper {
    max-width: 100%;
    box-shadow: none !important;
  }
  
  .login-form {
    box-shadow: none !important;
    border: 1px solid #ddd;
  }
}
</style>