<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone, Message } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const registerForm = ref({ username: '', password: '', confirmPassword: '', phone: '', email: '', })
const registerFormRef = ref(null)
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
}

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
            email: registerForm.value.email
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
  <div class="register">
    <el-form class="register-form" ref="registerFormRef" :model="registerForm" :rules="registerRules">
      <h2 class="title">自习室预约平台</h2>
      <h3 class="subtitle">用户注册</h3>
      <el-form-item prop="username">
        <el-input v-model="registerForm.username" type="text" placeholder="学号" :prefix-icon="User" />
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="registerForm.password" type="password" placeholder="密码" :prefix-icon="Lock" />
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" :prefix-icon="Lock" />
      </el-form-item>
      <el-form-item prop="phone">
        <el-input v-model="registerForm.phone" type="text" placeholder="手机号" :prefix-icon="Phone" />
      </el-form-item>
      <el-form-item prop="email">
        <el-input v-model="registerForm.email" type="text" placeholder="邮箱" :prefix-icon="Message" />
      </el-form-item>
      <el-form-item>
        <el-button :loading="loading" type="primary" style="width:100%;" @click="handleRegister">
          <span v-if="!loading">注 册</span>
          <span v-else>注 册 中...</span>
        </el-button>
      </el-form-item>
      <el-form-item class="login-link">
        <span>已有账号？</span>
        <a href="#" @click.prevent="router.push('/login')">立即登录</a>
      </el-form-item>
    </el-form>
    <!--  底部  -->
    <div class="el-register-footer">
      <span>Copyright © 2026 高校智能自习室系统 版权所有。</span>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.register {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-image: url('https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20study%20room%20register%20background&image_size=landscape_16_9');
  background-size: cover;
  background-position: center;

  .register-form {
    margin: 20px;
    padding: 25px 25px 5px 25px;
    background: #ffffff90;
    width: 100%;
    max-width: 400px;
    border-radius: 6px;
    z-index: 1;

    .title {
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 0 10px 0;
      color: #707070;
      font-size: 22px;
    }

    .subtitle {
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 0 20px 0;
      color: #909399;
      font-size: 16px;
    }

    .el-input {
      height: 38px;
      border-radius: 50px;
    }

    .el-button {
      margin-top: 10px;
      height: 35px;
      border-radius: 50px;
    }

    .login-link {
      margin-top: 15px;
      text-align: center;
      font-size: 14px;

      a {
        color: #409EFF;
        text-decoration: none;

        &:hover {
          text-decoration: underline;
        }
      }
    }
  }

  .el-register-footer {
    height: 40px;
    line-height: 40px;
    position: fixed;
    bottom: 0;
    width: 100%;
    text-align: center;
    color: #fff;
    font-family: Arial;
    font-size: 12px;
    letter-spacing: 1px;
    background: rgba(0, 0, 0, 0.5);
    padding: 0 10px;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-form {
    margin: 0 20px;
    padding: 20px;
  }

  .title {
    font-size: 20px !important;
  }

  .subtitle {
    font-size: 14px !important;
  }
}
</style>