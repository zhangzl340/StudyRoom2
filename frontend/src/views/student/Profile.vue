<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowRightBold,
  ArrowRight,
  User,
  Lock,
  DocumentCopy,
  Avatar,
  PhoneFilled,
  Message,
  SuccessFilled,
  StarFilled,
  UserFilled,
  Warning,
  Plus,
  Camera,
} from '@element-plus/icons-vue'
import { useAuthStore } from '../../stores/auth'
import { useRouter } from 'vue-router'
import { useReservationStore } from '../../stores/reservation'

const authStore = useAuthStore()
const router = useRouter()
const reservationStore = useReservationStore()

// 修改头像
const avatar = ref()
const avatarFile = ref()
const avatarShow = ref(false)

// 个人信息
const myInfoShow = ref(false)

// 编辑资料
const editProfileShow = ref(false)
const profileFormRef = ref()
const profileForm = ref({
  nickName: '',
  phonenumber: '',
  email: '',
  sex: '',
})
const profileRules = ref({
  nickName: [
    { required: true, message: "请输入昵称", trigger: "blur" }
  ],
  phonenumber: [
    { required: true, message: '请输入号码', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
  ],
  sex: [
    { required: true, message: '请选择性别', trigger: 'blur' },
  ],
})

// 修改密码
const pwdShow = ref(false)
const pwdFormRef = ref()
const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const pwdRules = ref({
  oldPassword: [
    { required: true, message: "请输入旧密码", trigger: "blur" },
    { min: 5, message: '密码长度不能小于5位', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 5, message: '密码长度不能小于5位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请输入确认密码', trigger: 'blur' },
    { min: 5, message: '密码长度不能小于5位', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.value.newPassword) {
          callback(new Error('新密码与确认密码不一致'))
        } else {
          callback()
        }
      }, trigger: 'blur'
    }
  ]
})

// 预约记录/违约记录
const recordLoading = ref(true) // 预约记录加载
const recordTitle = ref('')     // 预约记录标题
const recordShow = ref(false)   // 预约记录显示
const recordList = ref([])      // 预约记录列表
const recordTotal = ref(0)      // 预约记录/违约记录
const recordSelect = ref({
  userId: authStore.userInfo?.id,
  status: '',
  pageNum: 1,
  pageSize: 5,
})

// 修改头像
const updateAvatar = () => {
  avatarShow.value = true
}

const requestUpload = () => { }

const beforeUpload = (file) => {
  if (file.type.indexOf("image/") === -1) {
    ElMessage.error('文件格式错误，请上传图片类型,如：JPG，PNG后缀的文件。')
  } else {
    avatarFile.value = file
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => {
      avatar.value = reader.result
    }
  }
}

const updateAvatarBtn = () => {
  if (avatarFile.value) {
    // 这里可以调用后端API上传头像
    ElMessage.success('修改成功')
    avatarShow.value = false
  } else {
    ElMessage.error('请选择图片文件')
  }
}

// 编辑资料
const editProfile = () => {
  editProfileShow.value = true
  // 填充表单数据
  if (authStore.userInfo) {
    profileForm.value = {
      nickName: authStore.userInfo.username || '',
      phonenumber: authStore.userInfo.phone || '',
      email: authStore.userInfo.email || '',
      sex: authStore.userInfo.gender || '',
    }
  }
}

const saveProfile = () => {
  // 这里可以调用后端API更新用户信息
  ElMessage.success('保存成功')
  editProfileShow.value = false
}

// 修改密码
const updatePwd = () => {
  pwdShow.value = true
}

const savePwd = () => {
  // 这里可以调用后端API更新密码
  ElMessage.success('修改成功')
  pwdShow.value = false
  // 退出登录
  authStore.logout()
  router.push('/login')
  ElMessage.error('密码已修改，请重新登录')
}

// 预约记录/违约记录
const recordBtn = async (status) => {
  recordLoading.value = true
  recordSelect.value.pageNum = 1
  if (!status) {
    recordTitle.value = '预约记录'
  } else {
    recordTitle.value = '违约记录'
  }
  recordShow.value = true
  recordSelect.value.status = status
  await getRecord()
}

const getRecord = async () => {
  try {
    await reservationStore.fetchReservations()
    let filteredReservations = reservationStore.reservations
    
    // 筛选用户的预约记录
    filteredReservations = filteredReservations.filter(item => item.userId === authStore.userInfo?.id)
    
    // 筛选状态
    if (recordSelect.value.status) {
      filteredReservations = filteredReservations.filter(item => item.status === '违约')
    }
    
    recordList.value = filteredReservations
    recordTotal.value = filteredReservations.length
  } catch (error) {
    ElMessage.error('获取记录失败')
  } finally {
    recordLoading.value = false
  }
}

// 退出登录
const exit = () => {
  ElMessageBox.confirm('是否确定退出登录？', '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning',
  }).then(() => {
    authStore.logout()
    router.push('/login')
    ElMessage.success('退出登录成功')
  }).catch(() => { })
}
</script>

<template>
  <div class="profile">
    <!-- 个人信息卡片 -->
    <div class="profile-card">
      <div class="profile-header">
        <div class="avatar-section">
          <el-avatar :size="80" :src="authStore.userInfo?.avatar || ''" @click="updateAvatar" class="user-avatar">
            {{ authStore.userInfo?.username?.charAt(0) || '用' }}
          </el-avatar>
          <div class="avatar-edit">
            <el-icon class="edit-icon"><Camera /></el-icon>
          </div>
        </div>
        <div class="user-info">
          <h2 class="user-name">{{ authStore.userInfo?.username || '用户' }}</h2>
          <p class="user-id">学号：{{ authStore.userInfo?.username || '' }}</p>
        </div>
      </div>
    </div>
    
    <!-- 功能选项列表 -->
    <div class="function-list">
      <div class="function-item" @click="editProfile">
        <div class="function-icon">
          <el-icon color="#409EFF"><User /></el-icon>
        </div>
        <div class="function-content">
          <div class="function-title">编辑资料</div>
        </div>
        <div class="function-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
      
      <div class="function-item" @click="recordBtn()">
        <div class="function-icon">
          <el-icon color="#67C23A"><DocumentCopy /></el-icon>
        </div>
        <div class="function-content">
          <div class="function-title">预约记录</div>
        </div>
        <div class="function-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
      
      <div class="function-item" @click="recordBtn('违约')">
        <div class="function-icon">
          <el-icon color="#E6A23C"><Warning /></el-icon>
        </div>
        <div class="function-content">
          <div class="function-title">违约记录</div>
        </div>
        <div class="function-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
    </div>
    
    <!-- 退出登录按钮 -->
    <div class="logout-section">
      <el-button type="danger" plain class="logout-button" @click="exit">退出登录</el-button>
    </div>

    <!-- 修改头像弹窗 -->
    <el-dialog title="修改头像" v-model="avatarShow" width="80%">
      <div class="avatar-upload-section">
        <el-upload 
          class="avatar-uploader" 
          :http-request="requestUpload" 
          :show-file-list="false"
          :before-upload="beforeUpload"
        >
          <img v-if="avatar" :src="avatar" class="preview-avatar" />
          <div v-else class="avatar-placeholder">
            <el-icon class="placeholder-icon"><Plus /></el-icon>
            <span>点击上传头像</span>
          </div>
        </el-upload>
      </div>
      <template #footer>
        <el-button @click="avatarShow = false">取消</el-button>
        <el-button type="primary" @click="updateAvatarBtn">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 编辑资料弹窗 -->
    <el-dialog title="编辑资料" v-model="editProfileShow" width="90%">
      <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="80px">
        <el-form-item label="昵称" prop="nickName">
          <el-input v-model="profileForm.nickName" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phonenumber">
          <el-input v-model="profileForm.phonenumber" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="profileForm.sex">
            <el-radio value="male">男</el-radio>
            <el-radio value="female">女</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editProfileShow = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- 预约记录/违约记录抽屉 -->
    <el-drawer v-model="recordShow" :title="recordTitle" size="100%" direction="rtl">
      <div class="record-container" v-loading="recordLoading">
        <!-- 统计信息 -->
        <div class="record-stats">
          <div class="stat-item">
            <div class="stat-value">{{ recordTotal }}</div>
            <div class="stat-label">{{ recordTitle === '预约记录' ? '总预约次数' : '总违约次数' }}</div>
          </div>
        </div>
        
        <!-- 记录详情 -->
        <div class="record-list">
          <!-- 预约记录 -->
          <div class="record-item" v-if="recordTitle === '预约记录'" v-for="item in recordList" :key="item.id">
            <div class="record-header">
              <h4 class="record-room">自习室 {{ item.roomId }}</h4>
              <el-tag :type="item.status === 'pending' ? 'success' : 'danger'">
                {{ item.reservationStatus || '正常' }}
              </el-tag>
            </div>
            <div class="record-details">
              <p>座位：{{ item.seatId }}号</p>
              <p>预约时间：{{ item.reservationInTime || '2026-02-01 14:00' }}</p>
              <p>结束时间：{{ item.reservationOutTime || '2026-02-01 16:00' }}</p>
            </div>
          </div>
          
          <!-- 违约记录 -->
          <div class="record-item warning" v-if="recordTitle === '违约记录'" v-for="item in recordList" :key="item.id">
            <div class="record-header">
              <h4 class="record-room">
                <el-tag type="danger">违约</el-tag>
                <span>{{ item.remark || '超时未签到' }}</span>
              </h4>
            </div>
            <div class="record-details">
              <p>违约时间：{{ item.reservationOutTime || '2026-02-01 16:00' }}</p>
              <p>自习室：{{ item.roomId || '1' }}</p>
              <p>座位：{{ item.seatId || 'A1' }}号</p>
            </div>
          </div>
          
          <!-- 暂无记录 -->
          <div v-if="!recordTotal" class="empty-record">
            <el-empty description="暂无记录" />
          </div>
        </div>
        
        <!-- 违约规则说明 -->
        <div v-if="recordTitle === '违约记录'" class="rule-info">
          <el-alert 
            title="违约规则说明" 
            type="warning" 
            :closable="false"
            show-icon
          >
            <ul>
              <li>1. 预约后 <strong>2小时未签到</strong> 视为违约</li>
              <li>2. 累计 <strong>3次违约</strong> 自动禁约7天</li>
              <li>3. 禁约期间无法进行自习室预约</li>
            </ul>
          </el-alert>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<style lang="scss" scoped>
.profile {
  padding: 15px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

// 个人信息卡片
.profile-card {
  background: linear-gradient(135deg, #409EFF, #36cfc9);
  border-radius: 16px;
  padding: 30px 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

  .profile-header {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;

    .avatar-section {
      position: relative;
      margin-bottom: 15px;

      .user-avatar {
        border: 3px solid rgba(255, 255, 255, 0.5);
        transition: transform 0.3s ease;

        &:hover {
          transform: scale(1.05);
        }
      }

      .avatar-edit {
        position: absolute;
        bottom: 0;
        right: 0;
        width: 30px;
        height: 30px;
        background-color: rgba(0, 0, 0, 0.5);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;

        .edit-icon {
          color: white;
          font-size: 14px;
        }
      }
    }

    .user-info {
      .user-name {
        font-size: 20px;
        font-weight: bold;
        color: white;
        margin: 0 0 5px 0;
      }

      .user-id {
        font-size: 14px;
        color: rgba(255, 255, 255, 0.9);
        margin: 0;
      }
    }
  }
}

// 功能选项列表
.function-list {
  background-color: white;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;

  .function-item {
    display: flex;
    align-items: center;
    padding: 18px 20px;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background-color 0.3s ease;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background-color: #f5f7fa;
    }

    .function-icon {
      margin-right: 15px;
      font-size: 20px;
    }

    .function-content {
      flex: 1;

      .function-title {
        font-size: 16px;
        color: #303133;
      }
    }

    .function-arrow {
      color: #909399;
      font-size: 16px;
    }
  }
}

// 退出登录按钮
.logout-section {
  margin-top: 40px;

  .logout-button {
    width: 100%;
    height: 50px;
    font-size: 16px;
    border-radius: 25px;
    border-width: 2px;
  }
}

// 修改头像弹窗
.avatar-upload-section {
  display: flex;
  justify-content: center;
  padding: 20px;

  .avatar-uploader {
    width: 150px;
    height: 150px;
    margin: 0 auto;

    .preview-avatar {
      width: 100%;
      height: 100%;
      border-radius: 50%;
      object-fit: cover;
    }

    .avatar-placeholder {
      width: 100%;
      height: 100%;
      border: 2px dashed #ccc;
      border-radius: 50%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #909399;

      .placeholder-icon {
        font-size: 30px;
        margin-bottom: 10px;
      }
    }
  }
}

// 记录容器
.record-container {
  padding: 20px;

  .record-stats {
    background-color: #f9f9f9;
    border-radius: 12px;
    padding: 25px;
    margin-bottom: 20px;
    text-align: center;

    .stat-item {
      .stat-value {
        font-size: 32px;
        font-weight: bold;
        color: #409EFF;
        margin-bottom: 5px;
      }

      .stat-label {
        font-size: 14px;
        color: #606266;
      }
    }
  }

  .record-list {
    .record-item {
      background-color: white;
      border-radius: 12px;
      padding: 20px;
      margin-bottom: 15px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

      &.warning {
        background-color: #fef0f0;
        border-left: 4px solid #F56C6C;
      }

      .record-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 15px;

        .record-room {
          font-size: 16px;
          font-weight: bold;
          color: #303133;
          margin: 0;
          display: flex;
          align-items: center;
          gap: 10px;
        }
      }

      .record-details {
        p {
          font-size: 14px;
          color: #606266;
          margin: 8px 0;
        }
      }
    }

    .empty-record {
      margin: 60px 0;
      text-align: center;
    }
  }

  .rule-info {
    margin-top: 20px;
  }
}

// 响应式调整
@media (max-width: 768px) {
  .profile {
    padding: 10px;
  }

  .profile-card {
    padding: 20px 15px;

    .profile-header {
      .avatar-section {
        .user-avatar {
          :deep(.el-avatar) {
            width: 70px !important;
            height: 70px !important;
            font-size: 30px !important;
          }
        }

        .avatar-edit {
          width: 25px;
          height: 25px;

          .edit-icon {
            font-size: 12px;
          }
        }
      }

      .user-info {
        .user-name {
          font-size: 18px;
        }

        .user-id {
          font-size: 13px;
        }
      }
    }
  }

  .function-list {
    .function-item {
      padding: 15px 16px;

      .function-icon {
        margin-right: 12px;
        font-size: 18px;
      }

      .function-content {
        .function-title {
          font-size: 15px;
        }
      }
    }
  }

  .logout-section {
    margin-top: 30px;

    .logout-button {
      height: 45px;
      font-size: 15px;
    }
  }

  .record-container {
    padding: 15px;

    .record-stats {
      padding: 20px;

      .stat-item {
        .stat-value {
          font-size: 28px;
        }

        .stat-label {
          font-size: 13px;
        }
      }
    }

    .record-list {
      .record-item {
        padding: 15px;

        .record-header {
          .record-room {
            font-size: 15px;
          }
        }

        .record-details {
          p {
            font-size: 13px;
          }
        }
      }
    }
  }
}
</style>