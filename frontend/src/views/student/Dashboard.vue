<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

// 违约情况
const defaultStatus = ref('正常') // 违约状态
const defaultNumber = ref(0) // 违约次数
const startTime = ref('')    // 禁约开始时间
const endTime = ref('')      // 禁约结束时间
const remainingTime = ref('')// 禁约剩余时间
const banTime = ref(7)       // 禁约时间/天 
const remark = ref('')       // 备注
const cancelBtn = ref(false) // 禁约按钮

// 模拟获取用户信息和预约状态
const getUserInfo = () => {
  // 这里可以调用后端API获取用户信息
  if (!authStore.user) {
    authStore.getUserInfo()
  }
}

// 模拟获取预约状态
const getReservationStatus = () => {
  // 这里可以调用后端API获取预约状态
  // 暂时使用模拟数据
  defaultNumber.value = 1
  defaultStatus.value = '正常'
}

// 计算并更新剩余时间
const updateRemainingTime = () => {
  if (endTime.value) {
    const now = new Date()
    const end = new Date(endTime.value)
    const diff = end.getTime() - now.getTime()
    if (diff > 0) {
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
      const seconds = Math.floor((diff % (1000 * 60)) / 1000)
      remainingTime.value = `${days}天${hours}小时${minutes}分${seconds}秒`
    } else {
      remainingTime.value = '0天0小时0分0秒'
    }
    // 是否可以解除禁约
    if (defaultStatus.value == '禁约') {
      cancelBtn.value = new Date() > new Date(endTime.value)
    }
  }
}

// 解除禁约
const cancelBan = () => {
  if (new Date() > new Date(endTime.value)) {
    // 这里可以调用后端API解除禁约
    defaultStatus.value = '正常'
    cancelBtn.value = false
    ElMessage.success('预约状态已恢复正常')
  }
}

// 启动定时器更新剩余时间
let timer = null
onMounted(() => {
  getUserInfo()
  getReservationStatus()
  timer = setInterval(() => { updateRemainingTime() }, 1000)
})

// 清理定时器
onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<template>
  <div class="dashboard">
    <el-container>
      <el-header>自习室预约平台</el-header>
      <el-main>
        <!-- 我的信息 -->
        <div class="my-info">
          <div class="info">
            <el-avatar :size="50" :src="authStore.user?.avatar || ''">
              {{ authStore.user?.username?.charAt(0) || '用' }}
            </el-avatar>
            <div class="name">
              <p style="font-size: 18px;font-weight: bold;">你好，{{ authStore.user?.username || '用户' }}</p>
              <p style="font-size: 14px;">学号：{{ authStore.user?.username || '' }}</p>
            </div>
          </div>
          <div class="status">
            <div class="box">
              <span :style="{ color: defaultNumber >= 3 ? '#F56C6C' : '#ffffff' }">{{ defaultNumber }}</span>
              <span>违约次数</span>
            </div>
            <div class="box">
              <span :style="{ color: defaultStatus == '正常' ? '#ffffff' : '#F56C6C' }">{{ defaultStatus }}</span>
              <span>预约状态</span>
            </div>
          </div>
          <el-alert v-if="defaultStatus == '禁约'" type="error" :closable="false">
            <p><b>开始时间：</b>{{ startTime }}</p>
            <p><b>结束时间：</b>{{ endTime }}</p>
            <p><b>剩余时间：</b>{{ remainingTime }}</p>
            <p><b>违约原因：</b>{{ remark }}</p>
          </el-alert>
          <el-button v-if="cancelBtn" type="warning" @click="cancelBan">解 除 禁 约</el-button>
        </div>
        
        <!-- 轮播图 -->
        <el-carousel>
          <el-carousel-item>
            <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20study%20room%20with%20comfortable%20seats%20and%20good%20lighting&image_size=landscape_16_9" alt="自习室环境">
          </el-carousel-item>
          <el-carousel-item>
            <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=students%20studying%20in%20a%20modern%20library&image_size=landscape_16_9" alt="学习氛围">
          </el-carousel-item>
          <el-carousel-item>
            <img src="https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=smart%20campus%20study%20room%20reservation%20system&image_size=landscape_16_9" alt="智能预约系统">
          </el-carousel-item>
        </el-carousel>

        <!-- 功能入口 -->
        <div class="function-entry">
          <div class="entry-item" @click="router.push('/student/rooms')">
            <el-icon size="30">
              <i class="el-icon-building"></i>
            </el-icon>
            <span>自习室列表</span>
          </div>
          <div class="entry-item" @click="router.push('/student/reservations')">
            <el-icon size="30">
              <i class="el-icon-tickets"></i>
            </el-icon>
            <span>我的预约</span>
          </div>
          <div class="entry-item" @click="router.push('/student/checkin')">
            <el-icon size="30">
              <i class="el-icon-check"></i>
            </el-icon>
            <span>签到签退</span>
          </div>
          <div class="entry-item" @click="router.push('/student/profile')">
            <el-icon size="30">
              <i class="el-icon-user"></i>
            </el-icon>
            <span>个人中心</span>
          </div>
        </div>

        <!-- 通知公告 -->
        <div class="notice">
          <h3>通知公告</h3>
          <el-card>
            <el-timeline>
              <el-timeline-item type="primary" timestamp="2026-02-02">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>系统更新通知</span>
                    </div>
                  </template>
                  <p>自习室预约系统已完成更新，新增了座位预约功能，欢迎使用。</p>
                </el-card>
              </el-timeline-item>
              <el-timeline-item type="success" timestamp="2026-02-01">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>自习室开放时间调整</span>
                    </div>
                  </template>
                  <p>自2026年2月1日起，自习室开放时间调整为08:00-22:00，请同学们知悉。</p>
                </el-card>
              </el-timeline-item>
              <el-timeline-item type="warning" timestamp="2026-01-31">
                <el-card>
                  <template #header>
                    <div class="card-header">
                      <span>期末考试周预约提醒</span>
                    </div>
                  </template>
                  <p>期末考试周即将到来，自习室预约紧张，请同学们提前预约座位。</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<style lang="scss" scoped>
.dashboard {
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
        padding: 20px;
        color: #ffffff;
        background: linear-gradient(135deg, #409EFF, #36cfc9);
        border-radius: 10px;
        box-shadow: 0 4px 5px 0 #00000040;

        .info {
          display: flex;
          align-items: center;
          gap: 10px;
        }

        .status {
          display: flex;
          align-items: center;
          justify-content: space-around;
          margin-top: 10px;
          padding: 0 20px;
          font-size: 12px;

          .box {
            display: flex;
            align-items: center;
            justify-content: center;
            flex-direction: column;

            span:nth-child(1) {
              font-size: 20px;
              font-weight: bold;
            }
          }
        }

        .el-alert {
          margin-top: 10px;

          p {
            font-size: 12px;
          }
        }

        .el-button {
          margin-top: 10px;
          width: 100%;
        }
      }

      .el-carousel {
        margin: 10px 0;
        height: 160px;
        border-radius: 10px;
        box-shadow: 0 4px 5px 0 #00000040;

        img {
          width: 100%;
          height: 100%;
          border-radius: 10px;
          object-fit: cover;
        }
      }

      .function-entry {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 10px;
        margin: 10px 0;

        .entry-item {
          display: flex;
          align-items: center;
          justify-content: center;
          flex-direction: column;
          padding: 20px;
          background-color: #ffffff;
          border-radius: 10px;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
          cursor: pointer;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
          }

          span {
            margin-top: 10px;
            font-size: 12px;
          }
        }
      }

      .notice {
        margin-top: 10px;

        h3 {
          margin: 0 0 10px 0;
          font-size: 14px;
          color: #333333;
        }

        .card-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }
      }
    }
  }
}
</style>