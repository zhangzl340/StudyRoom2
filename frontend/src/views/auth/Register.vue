<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone, Message } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import axios from 'axios'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const registerForm = ref({ username: '', password: '', confirmPassword: '', phone: '', email: '', collegeId: '' })
const collegeOptions = ref([])
const registerFormRef = ref(null)
const isMobile = ref(false)

// 检测设备类型
const checkDevice = () => {
  isMobile.value = window.innerWidth <= 768
}

// 加载学院列表
const loadColleges = async () => {
  try {
    const response = await axios.get('/api/college/list')
    if (response.data.success) {
      collegeOptions.value = response.data.data.map(college => ({
        value: college.id,
        label: college.name
      }))
    }
  } catch (error) {
    console.error('获取学院列表失败:', error)
  }
}

const registerRules = {
  username: [
    { required: true, trigger: "blur", message: "请输入您的账号" }
  ],
  password: [
    { required: true, trigger: "blur", message: "请输入您的密码" },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, trigger: "blur", message: "请确认您的密码" },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.value.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }, trigger: 'blur'
    }
  ],
  phone: [
    { required: true, trigger: "blur", message: "请输入您的手机号" }
  ],
  email: [
    { required: true, trigger: "blur", message: "请输入您的邮箱" },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  collegeId: [
    { required: true, trigger: "blur", message: "请选择您的学院" }
  ],
}

// 初始化
onMounted(async () => {
  checkDevice()
  window.addEventListener('resize', checkDevice)
  await loadColleges()
})

// 清理事件监听
onUnmounted(() => {
  window.removeEventListener('resize', checkDevice)
})

// 处理注册
const handleRegister = async () => {
  if (registerFormRef.value) {
    registerFormRef.value.validate(async (valid) => {
      if (valid) {
        loading.value = true
        try {
          await authStore.register({
            username: registerForm.value.username,
            password: registerForm.value.password,
            phone: registerForm.value.phone,
            email: registerForm.value.email,
            collegeId: registerForm.value.collegeId,
            role: 'student',
            avatar:"/upload/images/2026-02-10/a3866b97-096f-4868-bfa5-b2e0381bd31f.jpg" // 默认头像
          })
          ElMessage.success('注册成功')
          // 跳转到登录页
          router.replace('/login')
        } catch (error) {
          ElMessage.error(error.message || '注册失败')
        } finally {
          loading.value = false
        }
      }
    })
  }
}
</script>

<template>
  <div class="register-container" :class="{ 'mobile-view': isMobile }">
    <!-- 左侧装饰区（桌面端） -->
    <div class="register-left" v-if="!isMobile">
      <div class="left-content">
        <h1 class="welcome-title">欢迎加入</h1>
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

    <!-- 右侧注册表单区 -->
    <div class="register-right">
      <div class="register-form-wrapper">
        <!-- 移动端标题 -->
        <div class="mobile-header" v-if="isMobile">
          <h1 class="app-title">自习室预约</h1>
          <p class="app-subtitle">高校智能管理系统</p>
        </div>

        <el-form 
          class="register-form" 
          ref="registerFormRef" 
          :model="registerForm" 
          :rules="registerRules"
          :size="isMobile ? 'large' : 'default'"
        >
          <!-- 桌面端标题 -->
          <div class="desktop-header" v-if="!isMobile">
            <h2 class="form-title">用户注册</h2>
            <p class="form-subtitle">请填写以下信息完成注册</p>
          </div>

          <el-form-item prop="username">
            <el-input 
              v-model="registerForm.username" 
              type="text" 
              placeholder="学号" 
              :prefix-icon="User"
              :clearable="!isMobile"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="registerForm.password" 
              type="password" 
              placeholder="密码" 
              :prefix-icon="Lock"
              :show-password="!isMobile"
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="registerForm.confirmPassword" 
              type="password" 
              placeholder="确认密码" 
              :prefix-icon="Lock"
              :show-password="!isMobile"
            />
          </el-form-item>
          <el-form-item prop="phone">
            <el-input 
              v-model="registerForm.phone" 
              type="text" 
              placeholder="手机号" 
              :prefix-icon="Phone"
              :clearable="!isMobile"
            />
          </el-form-item>
          <el-form-item prop="email">
            <el-input 
              v-model="registerForm.email" 
              type="text" 
              placeholder="邮箱" 
              :prefix-icon="Message"
              :clearable="!isMobile"
            />
          </el-form-item>
          <el-form-item prop="collegeId">
            <el-select 
              v-model="registerForm.collegeId" 
              placeholder="请选择学院" 
              style="width: 100%"
            >
              <el-option 
                v-for="option in collegeOptions" 
                :key="option.value" 
                :label="option.label" 
                :value="option.value" 
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button 
              :loading="loading" 
              type="primary" 
              :class="{ 'mobile-btn': isMobile }"
              @click="handleRegister"
            >
              <span v-if="!loading">注 册</span>
              <span v-else>注册中...</span>
            </el-button>
          </el-form-item>
          
          <div class="login-section">
            <span class="login-tip">已有账号？</span>
            <el-link 
              type="primary" 
              :underline="false"
              @click="router.push('/login')"
            >
              立即登录
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
.register-container {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  
  &.mobile-view {
    flex-direction: column;
    background: #f0f2f5;
  }
}

/* 左侧装饰区（桌面端） */
.register-left {
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

/* 右侧注册区 */
.register-right {
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

.register-form-wrapper {
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

/* 注册表单 */
.register-form {
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

:deep(.el-select) {
  .el-select__wrapper {
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
  
  &.is-focus .el-select__wrapper {
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

/* 登录区域 */
.login-section {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid #eee;
  
  .login-tip {
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
  .register-left {
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
  .register-container {
    &.mobile-view {
      min-height: 100vh;
      overflow-y: auto;
    }
  }
  
  .register-right {
    min-height: calc(100vh - 60px);
  }
}

@media (max-width: 480px) {
  .register-form {
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
  
  .login-section {
    flex-direction: column;
    gap: 0.3rem;
  }
}

/* 打印样式 */
@media print {
  .register-container {
    background: white !important;
  }
  
  .register-left,
  .mobile-footer,
  .login-section,
  .mobile-quick-links,
  .desktop-footer {
    display: none !important;
  }
  
  .register-right {
    width: 100%;
    padding: 0;
  }
  
  .register-form-wrapper {
    max-width: 100%;
    box-shadow: none !important;
  }
  
  .register-form {
    box-shadow: none !important;
    border: 1px solid #ddd;
  }
}
</style>