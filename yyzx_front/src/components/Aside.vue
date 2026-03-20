<template>
    <el-aside id="asideNav" width="200px">
    <div class="logo-name" >
      <p>颐养中心</p>
      <img src="../assets/logo.png" class="logo-png"/>
    </div>
      <el-menu
        active-text-color="#ffd04b"
        background-color="#3CA2E0"
        :default-active="$route.path"
        :router="true"
        unique-opened
        text-color="#fff"
      >
        <!-- 首页菜单项 -->
      <el-menu-item index="/main/dashboard">
        <el-icon><House /></el-icon>
        <span>首页</span>
      </el-menu-item>

      <template v-for="menu in $store.getters.menus" >
        <el-sub-menu :index="menu.menusIndex" > 
          <template #title>
            <el-icon>
              <!-- 动态获取图标，按注册名渲染组件 -->
              <!-- https://cn.vuejs.org/api/built-in-special-elements.html#component -->
              <component :is="menu.icon" />
            </el-icon>
            <span>{{menu.title}}</span>
          </template>
          <template v-for="menu in menu.children">
            <el-menu-item :index="menu.path">
                <el-icon><component :is="menu.icon" /></el-icon>
                {{menu.title}}
            </el-menu-item>
          </template>
        </el-sub-menu>
      </template>
    </el-menu>
  </el-aside>

</template>


<script>
import { House } from '@element-plus/icons-vue'
export default {
  name: 'Aside',
  components: {
    House
  },
  methods: {
   
    },
    
  watch: {
      // 监听浏览器直接输入路由，将此路由添加到tabs  
    "$route.path": {
        handler: function (newVal, oldVal) {
        console.log("route===========>", this.$route) // bed/bedMap
        // 排除登录页和首页
        if (newVal == "/login" || newVal == "/home" || newVal == "/main/dashboard") {
          return;
        }
        //判断当前tab中是否已经存在
        let index = this.$store.getters.tabs.findIndex(function (value, key) {
          return value.path === newVal;
        });
        // 前面已经构建好了
        // console.log("index,newVal===========>", index,newVal);
        if (index != -1) {
          return;
        }
        let menuName = this.$store.getters.getMenuNameByUrl(newVal);
        // console.log("menuName===========>", menuName);
        //手动构建tab
        this.$store.commit("addTab", {
          title: menuName,
          path: newVal
        });
      },
      immediate: true
    }
  },
}
</script>

<style scoped>
#asideNav {
  display: flex;
  flex-direction: column;
}


#asideNav .logo-name {
  width: 100%;
  height: 150px;
  background-color:#3CA2E0;
}
#asideNav .logo-name .logo-png{
  width: 165px;
  height: 165px;
  margin: -30px 0 0 10px;
}
#asideNav .logo-name p {
  font-weight: 800;
  height: 50px;
  line-height: 50px;
  text-align: center;
  font-size: 25px;
  margin: 0px;
  color: #fff;
}

#asideNav .el-menu {
  /* font-weight: 550; */
  flex: 1;
  border-right: none;
}


</style>