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
const recordTotal = ref(0)      // 预约记录总数
const recordSelect = ref({
  userId: authStore.user?.id,
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
  if (authStore.user) {
    profileForm.value = {
      nickName: authStore.user.username || '',
      phonenumber: authStore.user.phone || '',
      email: authStore.user.email || '',
      sex: authStore.user.gender || '',
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
    await reservationStore.getReservations()
    let filteredReservations = reservationStore.reservations
    
    // 筛选用户的预约记录
    filteredReservations = filteredReservations.filter(item => item.userId === authStore.user?.id)
    
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
    <el-container>
      <el-header>个人中心</el-header>
      <el-main>
        <!-- 个人信息 -->
        <div class="my-info">
          <div class="info">
            <el-avatar :size="50" :src="authStore.user?.avatar || ''" @click="updateAvatar">
              {{ authStore.user?.username?.charAt(0) || '用' }}
            </el-avatar>
            <div class="name">
              <p style="font-size: 18px;font-weight: bold;">{{ authStore.user?.username || '用户' }}</p>
              <p style="font-size: 14px;">学号：{{ authStore.user?.username || '' }}</p>
            </div>
          </div>
          <el-button color="#000000" link @click="myInfoShow = true">
            <span>个人信息</span>
            <el-icon>
              <ArrowRightBold />
            </el-icon>
          </el-button>
        </div>
        <!-- 选项卡 -->
        <div class="select-card">
          <div class="option" @click="editProfile">
            <span>
              <el-icon color="#409EFF" size="16">
                <User />
              </el-icon>
              编辑资料
            </span>
            <el-icon size="16">
              <ArrowRight />
            </el-icon>
          </div>
          <div class="option" @click="updatePwd">
            <span>
              <el-icon color="#409EFF" size="16">
                <Lock />
              </el-icon>
              修改密码
            </span>
            <el-icon size="16">
              <ArrowRight />
            </el-icon>
          </div>
          <div class="option" @click="recordBtn()">
            <span>
              <el-icon color="#409EFF" size="16">
                <DocumentCopy />
              </el-icon>
              预约记录
            </span>
            <el-icon size="16">
              <ArrowRight />
            </el-icon>
          </div>
          <div class="option" @click="recordBtn('违约')">
            <span>
              <el-icon color="#409EFF" size="16">
                <Warning />
              </el-icon>
              违约记录
            </span>
            <el-icon size="16">
              <ArrowRight />
            </el-icon>
          </div>
        </div>
        <!-- 退出登录 -->
        <el-button class="logout" type="danger" plain @click="exit">退出登录</el-button>
      </el-main>

      <!-- 修改头像 -->
      <el-dialog title="修改头像" v-model="avatarShow" width="90%">
        <div class="update-avatar">
          <el-upload class="avatar-uploader" :http-request="requestUpload" :show-file-list="false"
            :before-upload="beforeUpload">
            <img v-if="avatar" :src="avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon">
              <Plus />
            </el-icon>
          </el-upload>
        </div>
        <template #footer>
          <el-button @click="avatarShow = false">取消</el-button>
          <el-button type="warning" @click="updateAvatarBtn">修改</el-button>
        </template>
      </el-dialog>

      <!-- 个人信息 -->
      <el-dialog title="个人信息" v-model="myInfoShow" width="90%">
        <div class="personal-data">
          <p>
            <span>
              <el-icon>
                <StarFilled />
              </el-icon>
              学号:
            </span>
            {{ authStore.user?.username || '' }}
          </p>
          <p>
            <span>
              <el-icon>
                <Avatar />
              </el-icon>
              昵称:
            </span>
            {{ authStore.user?.username || '' }}
          </p>
          <p>
            <span>
              <el-icon>
                <UserFilled />
              </el-icon>
              性别:
            </span>
            {{ authStore.user?.gender === 'male' ? '男' : authStore.user?.gender === 'female' ? '女' : '未知' }}
          </p>
          <p>
            <span>
              <el-icon>
                <PhoneFilled />
              </el-icon>
              手机号码:
            </span>
            {{ authStore.user?.phone || '' }}
          </p>
          <p>
            <span>
              <el-icon>
                <Message />
              </el-icon>
              邮箱:
            </span>
            {{ authStore.user?.email || '' }}
          </p>
          <p>
            <span>
              <el-icon>
                <SuccessFilled />
              </el-icon>
              账号创建日期:
            </span>
            {{ authStore.user?.createdAt || '' }}
          </p>
        </div>
      </el-dialog>

      <!-- 编辑资料 -->
      <el-dialog title="编辑资料" v-model="editProfileShow" width="90%">
        <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules">
          <el-form-item label="昵称" prop="nickName">
            <el-input v-model="profileForm.nickName" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="号码" prop="phonenumber">
            <el-input v-model="profileForm.phonenumber" placeholder="请输入号码" />
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

      <!-- 修改密码 -->
      <el-dialog title="修改密码" v-model="pwdShow" width="90%">
        <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="auto">
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input type="password" v-model="pwdForm.oldPassword" placeholder="请输入旧密码" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input type="password" v-model="pwdForm.newPassword" placeholder="请输入新密码" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input type="password" v-model="pwdForm.confirmPassword" placeholder="请输入确认密码" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="pwdShow = false">取消</el-button>
          <el-button type="primary" @click="savePwd">修改</el-button>
        </template>
      </el-dialog>

      <!-- 预约记录/违约记录 -->
      <el-drawer v-model="recordShow" :title="recordTitle" size="100%" direction="ltr">
        <div class="record" v-loading="recordLoading">
          <!-- 预约信息 -->
          <div class="info">
            <div v-if="recordTitle === '预约记录'">
              <p>总预约次数</p>
              <p class="text">{{ recordTotal }}</p>
            </div>
            <div v-if="recordTitle === '违约记录'">
              <p>总违约次数</p>
              <p class="text">{{ recordTotal }}</p>
            </div>
          </div>
          <el-divider content-position="center">{{ recordTitle }}详情</el-divider>
          <!-- 预约记录 -->
          <div class="YY" v-if="recordTitle === '预约记录'" v-for="item in recordList" :key="item.id">
            <div>
              <h4 class="title">自习室 {{ item.roomId }}</h4>
              <p>座位：{{ item.seatId }}号</p>
              <p>预约时间：{{ item.reservationInTime }}</p>
              <p>结束时间：{{ item.reservationOutTime }}</p>
            </div>
            <div class="status">
              <el-tag>{{ item.reservationStatus }}</el-tag>
              <el-tag type="success" v-if="item.status === '正常'">{{ item.status }}</el-tag>
              <el-tag type="danger" v-else>{{ item.status }}</el-tag>
            </div>
          </div>
          <!-- 违约记录 -->
          <div class="WY" v-if="recordTitle === '违约记录'" v-for="item in recordList" :key="item.id">
            <h4 class="title">
              <el-tag type="danger">{{ item.status }}</el-tag>
              <span>{{ item.remark || '超时未签到' }}</span>
            </h4>
            <p>违约时间：{{ item.reservationOutTime }}</p>
            <p>自习室：{{ item.roomId }}</p>
            <p>座位：{{ item.seatId }}号</p>
          </div>
          <!-- 暂无记录 -->
          <div v-if="!recordTotal" style="text-align: center;">暂无记录</div>
          <el-divider></el-divider>
          <el-alert :closable="false" v-if="recordTitle === '违约记录'">
            <h4 style="margin-bottom: 10px; color: #555;">违约规则说明：</h4>
            <p style="margin: 5px;">1.预约后 <b style="color: #F56C6C;">2小时未签到</b> 视为违约</p>
            <p style="margin: 5px;">2.累计 <b style="color: #F56C6C;">3次违约</b> 自动禁约7天</p>
            <p style="margin: 5px;">3.禁约期间无法进行自习室预约</p>
            <p style="margin: 5px;">4.可联系管理员解除禁约或清楚违约记录</p>
          </el-alert>
        </div>
      </el-drawer>

    </el-container>
  </div>
</template>

<style lang="scss" scoped>
.profile {
  height: 100%;

  .el-container {
    height: 100%;

    .el-header {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 40px;
      color: #555555;
      font-size: 16px;
      background-color: #ffffff;
    }

    .el-main {
      padding: 5px;

      .my-info {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 25px 20px;
        color: #ffffff;
        background: linear-gradient(135deg, #409EFF, #36cfc9);
        border-radius: 8px;

        .info {
          display: flex;
          align-items: center;
          gap: 10px;
        }
      }

      .select-card {
        display: flex;
        flex-direction: column;
        gap: 2px;
        margin: 10px 0;
        border-radius: 4px;
        overflow: hidden;

        .option {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 15px 15px;
          width: 100%;
          color: #555555;
          background-color: #ffffff;
          cursor: pointer;

          span {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 15px;
          }
        }
      }

      .logout {
        width: 100%;
        height: 40px;
        border-radius: 50px;
      }
    }

    .personal-data {
      p {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 15px 0;
        color: #555555;
        font-size: 12px;
        border-bottom: 1px solid #eeeeee;

        span {
          display: flex;
          align-items: center;
          gap: 5px;
        }
      }
    }

    // 预约记录/违约记录
    .record {
      .info {
        display: flex;
        align-items: center;
        justify-content: space-around;
        padding: 20px;
        color: #909399;
        font-size: 15px;
        text-align: center;
        background-color: #f9f9f9;
        border-radius: 5px;

        .text {
          margin-top: 10px;
          color: #141414;
          font-size: 20px;
          font-weight: bold;
        }
      }

      .YY {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 10px;
        padding: 10px;
        color: #555;
        font-size: 12px;
        border: 1px solid #EBEEF5;
        border-radius: 5px;

        .title {
          font-size: 16px;
          font-weight: bold;
        }

        p {
          margin: 5px 0;
        }

        .status {
          display: flex;
          flex-direction: column;
          gap: 10px;
          width: 60px;
        }
      }

      .WY {
        margin: 10px 0;
        padding: 10px;
        color: #555;
        font-size: 12px;
        background-color: #fef0f0;
        border-radius: 5px;

        .title {
          display: flex;
          align-items: center;
          gap: 10px;
          font-size: 14px;
        }

        p {
          margin: 10px;
        }
      }
    }
  }
}

/* 修改头像 */
.avatar-uploader {
  margin: auto;
  width: 200px;
  height: 200px;
}

.avatar-uploader .el-upload {
  width: 100%;
  height: 100%;
  border: 2px dashed #ccc;
  border-radius: 6px;
  overflow: hidden;
}

.avatar-uploader .el-upload .avatar {
  width: 100%;
  height: 100%;
}
</style>