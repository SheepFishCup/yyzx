<!-- TabNav.vue - 修改后 -->
<template>
  <div class="container_tab">
    <ul class="tab_nav_box">
      <li v-for="(item, index) in $store.getters.tabs"
          :key="item.path"
          :class="{ active: $route.path === item.path }" >
        <router-link :to="item.path">{{item.title}}</router-link>
        <!-- 修改：根据 affix 属性判断是否显示关闭按钮 -->
        <el-icon v-if="!item.affix">
          <CloseBold @click="onCloseTabIndex(index)"/>
        </el-icon>
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  name: "TabNav",
  methods: {
    /**
     * 点击了 tab 选项卡
     */
     onCloseTabIndex(index) {
      const tabs = this.$store.getters.tabs;
      const currentPath = this.$route.path;

      // 关键：在删除前保存要关闭的 tab 信息
      const closedTab = tabs[index];

      // 如果是固定标签，不允许关闭
      if (closedTab.affix) {
        return
      }

      // 提交删除
      this.$store.commit("deleteTabByIndex", index);

      // 如果关闭的是当前页面的 tab，需要跳转到其他页面
      if (closedTab.path === currentPath) {
        // 使用 $nextTick 等待 store 更新
        this.$nextTick(() => {
          const newTabs = this.$store.getters.tabs;
          if (newTabs.length > 0) {
            // 还有 tab，跳转到最后一个
            const lastTab = newTabs[newTabs.length - 1];
            this.$router.push(lastTab.path);
          } else {
            // 没有 tab 了，跳转到默认首页
            this.$router.push('/main/dashboard');
          }
        });
      }
    }
  },
}
</script>

<style scoped>
.container_tab {
  padding-left: 20px;
  border-bottom: 1px solid #d8dce5;
}


.tab_nav_box {
  display: flex;
  align-items: center;
  padding: 2px 0px;
}

.tab_nav_box li {
  height: 28px;
  line-height: 28px;
  padding: 1px;
  display: flex;
  align-items: center;
  margin-right: 8px;
  border: 1px solid #cccccc;
  border-radius: 5%;
  opacity: 0.9;
}
.tab_nav_box li:hover{
  opacity: 1;
}

.tab_nav_box li a {
  padding-left: 10px;
  padding-right: 10px;
  display: inline-block;
  color: #000000;
}


.tab_nav_box li:nth-child(n+2) a {
  padding-right: 15px;
}

.tab_nav_box li i:hover {
  color: #9a8282;
}

.tab_nav_box li.active {
  font-size: 15px;
  background-color: #0b67b8;
  color: white;
}

.tab_nav_box li.active a {
  color: #ffffff;
}
</style>