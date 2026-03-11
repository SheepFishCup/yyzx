<template>
  <div class="home-layout">
    <!-- 顶部导航栏 -->
    <div class="top-nav">
      <div class="logo-container" @click="goToHome" style="cursor: pointer;">
<!--        <img style="height:40px;" src="@/assets/welcome.png" alt="Logo"/>-->
      </div>

      <div class="nav-right">
        <el-button type="text" @click="goToHome" class="home-btn">
          <el-icon><Home /></el-icon>
          首页
        </el-button>

        <el-dropdown split-button type="default">
          {{user.nickname}}
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="logout">注销</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 首页内容区域 -->
    <div class="main-content">
      <HomeView/>
    </div>
  </div>
</template>

<script>
import { HomeFilled } from '@element-plus/icons-vue'
import { getSessionStorage, setSessionStorage } from '@/utils/common.js'
import HomeView from './Home.vue'

export default {
  name: 'HomeLayout',
  components: {
    HomeFilled,
    HomeView
  },
  data() {
    return {
      user: getSessionStorage('user')
    }
  },
  methods: {
    goToHome() {
      this.$router.push('/home')
    },
    logout() {
      sessionStorage.setItem('token', null)
      setSessionStorage('user', null)
      this.$store.commit('addMenus', [])
      this.$store.commit('clearTab', [])
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.home-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
  overflow-x: hidden;
}

.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo-container {
  display: flex;
  align-items: center;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.home-btn {
  font-size: 16px;
  color: white;
  display: flex;
  align-items: center;
  gap: 5px;
}

.home-btn .el-icon {
  font-size: 18px;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 滚动条样式 */
.main-content::-webkit-scrollbar {
  width: 8px;
}

.main-content::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
}

.main-content::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 4px;
}

.main-content::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}
</style>
