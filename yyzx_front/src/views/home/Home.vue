<template>
  <div class="home-container">
    <!-- 顶部欢迎区域 -->
    <div class="welcome-section">
      <h1 class="welcome-title">欢迎光临黄桷垭老年人服务中心！</h1>
      <p class="welcome-subtitle">用心服务 · 关爱老人 · 温馨家园</p>
    </div>

    <!-- 轮播图区域 -->
    <div class="carousel-section">
      <el-carousel :interval="4000" type="card" height="400px">
        <el-carousel-item v-for="(item, index) in carouselImages" :key="index">
        <div class="carousel-content">
          <img 
            :src="item.image" 
            :alt="item.title" 
            class="carousel-image"
            @error="handleImageError($event, item)"
          />
          <div class="carousel-info">
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
          </div>
        </div>
      </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 快捷导航区域 -->
    <div class="navigation-section">
      <h2 class="section-title">快捷服务</h2>
      <div class="nav-grid">
        <div
            class="nav-card"
            v-for="(nav, index) in navigationItems"
            :key="index"
            @click="navigateTo(nav.path)"
        >
          <div class="nav-icon">
            <el-icon :size="48">
              <component :is="nav.icon" />
            </el-icon>
          </div>
          <h3 class="nav-title">{{ nav.title }}</h3>
          <p class="nav-description">{{ nav.description }}</p>
        </div>
      </div>
    </div>

    <!-- 中心介绍区域 -->
    <div class="introduction-section">
      <h2 class="section-title">关于我们</h2>
      <div class="intro-content">
        <div class="intro-text">
          <p>
            夕阳红养老服务中心致力于为老年人提供专业、温馨、贴心的养老服务。
            我们拥有专业的护理团队、完善的医疗设施、舒适的居住环境，让每一位老人都能安享晚年。
          </p>
          <p>
            中心提供日常生活照料、康复护理、文化娱乐、营养膳食等全方位服务，
            根据每位老人的身体状况和生活习惯，制定个性化的照护方案。
          </p>
          <div class="features">
            <div class="feature-item">
              <el-icon><User /></el-icon>
              <span>专业护理团队</span>
            </div>
            <div class="feature-item">
              <el-icon><House /></el-icon>
              <span>舒适居住环境</span>
            </div>
            <div class="feature-item">
              <el-icon><Food /></el-icon>
              <span>营养均衡膳食</span>
            </div>
            <div class="feature-item">
              <el-icon><VideoCamera /></el-icon>
              <span>24 小时监控</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {
  User,
  House,
  Food,
  VideoCamera,
  Star,
  Service,
  Monitor,
  Clock
} from '@element-plus/icons-vue'

export default {
  name: 'Home',
  components: {
    User,
    House,
    Food,
    VideoCamera,
    Star,
    Service,
    Monitor,
    Clock
  },
  data() {
    return {
      // 轮播图数据
      carouselImages: [
        {
          image: 'https://images.unsplash.com/photo-1576765608535-5f04d1e3f289?w=600&h=300&fit=crop&q=75&auto=format',
          title: '温馨居住环境',
          description: '宽敞明亮的房间，温馨舒适的家居布置'
        },
        {
          image: 'https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=600&h=300&fit=crop&q=75&auto=format',
          title: '专业护理服务',
          description: '专业护理团队 24 小时贴心照料'
        },
        {
          image: 'https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=600&h=300&fit=crop&q=75&auto=format',
          title: '医疗健康保障',
          description: '定期健康检查，医疗保障完善'
        },
        {
          image: 'https://images.unsplash.com/photo-1552664730-d307ca884978?w=600&h=300&fit=crop&q=75&auto=format',
          title: '丰富文化活动',
          description: '多彩的文娱活动，充实的晚年生活'
        }
      ],

      // 导航项数据
      navigationItems: [
        {
          title: '床位管理',
          description: '查看和分配床位',
          icon: 'House',
          path: '/bed/bedMap'
        },
        {
          title: '老人入住',
          description: '办理入住手续',
          icon: 'User',
          path: '/customer/checkIn'
        },
        {
          title: '护理服务',
          description: '日常护理记录',
          icon: 'Service',
          path: '/nurse/nurseRecords'
        },
        {
          title: '健康管理',
          description: '健康状况监测',
          icon: 'Monitor',
          path: '/health/dailyNurse'
        },
        {
          title: '营养膳食',
          description: '食谱和订餐管理',
          icon: 'Food',
          path: '/customer/meal'
        },
        {
          title: '外出管理',
          description: '外出登记管理',
          icon: 'Clock',
          path: '/customer/outRecords'
        },
        {
          title: '护理等级',
          description: '护理等级设置',
          icon: 'Star',
          path: '/nurse/nurseLevel'
        },
        {
          title: '人员管理',
          description: '员工信息管理',
          icon: 'User',
          path: '/user/listUser'
        }
      ]
    }
  },
  methods: {
    navigateTo(path) {
      this.$router.push(path)
    },
    // 图片加载失败处理
    handleImageError(event, item) {
      // 设置默认占位图
      event.target.src = '/images/placeholder.png'
      console.warn(`图片加载失败：${item.title}`)
    }
  }
}
</script>

<style scoped>
.home-container {
  /* padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: calc(100vh - 100px); */
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 160px);
}

/* 欢迎区域 */
.welcome-section {
  /* text-align: center;
  padding: 30px 20px;
  color: white; */
  text-align: center;
  padding: 30px 20px;
  color: #333;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  margin-bottom: 20px;
}

.welcome-title {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 10px;
  color: white;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.welcome-subtitle {
  font-size: 18px;
  color: white;
  opacity: 0.9;
}

/* 轮播图区域 */
.carousel-section {
  max-width: 1200px;
  margin: 30px auto;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.carousel-content {
  position: relative;
  height: 400px;
  overflow: hidden;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
  color: white;
}

.carousel-info h3 {
  font-size: 24px;
  margin-bottom: 10px;
}

.carousel-info p {
  font-size: 16px;
  opacity: 0.9;
}

/* 导航区域 */
.navigation-section {
  max-width: 1200px;
  margin: 40px auto;
  padding: 30px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.section-title {
  text-align: center;
  font-size: 28px;
  color: #333;
  margin-bottom: 30px;
  font-weight: bold;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.nav-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30px 20px;
  border-radius: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.nav-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
}

.nav-icon {
  margin-bottom: 15px;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 60px;
}

.nav-title {
  font-size: 20px;
  margin-bottom: 10px;
  font-weight: bold;
}

.nav-description {
  font-size: 14px;
  opacity: 0.9;
}

/* 介绍区域 */
.introduction-section {
  max-width: 1200px;
  margin: 40px auto;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.intro-content {
  display: flex;
  justify-content: center;
}

.intro-text {
  max-width: 800px;
  line-height: 2;
  color: #555;
  font-size: 16px;
}

.intro-text p {
  margin-bottom: 20px;
  text-indent: 2em;
}

.features {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  margin-top: 30px;
  padding-top: 30px;
  border-top: 2px solid #eee;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 10px 20px;
  color: #667eea;
  font-size: 16px;
  font-weight: bold;
}

.feature-item .el-icon {
  margin-bottom: 10px;
  font-size: 32px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .welcome-title {
    font-size: 24px;
  }

  .welcome-subtitle {
    font-size: 16px;
  }

  .carousel-section {
    margin: 20px auto;
  }

  .carousel-content {
    height: 250px;
  }

  .carousel-info h3 {
    font-size: 18px;
  }

  .carousel-info p {
    font-size: 14px;
  }

  .nav-grid {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 15px;
  }

  .nav-card {
    padding: 20px 15px;
  }

  .nav-icon {
    height: 50px;
  }

  .nav-title {
    font-size: 16px;
  }

  .nav-description {
    font-size: 12px;
  }

  .introduction-section {
    padding: 20px;
  }

  .section-title {
    font-size: 22px;
  }

  .intro-text {
    font-size: 14px;
  }

  .features {
    flex-direction: column;
    align-items: center;
  }
}
</style>
