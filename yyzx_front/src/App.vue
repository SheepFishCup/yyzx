<template>
  <router-view/>
</template>

<script>
import websocketClient from '@/utils/websocket';
export default {
    name: 'App',
    components: {},
    // App.vue 中修改
    mounted() {
        const userInfo = JSON.parse(sessionStorage.getItem('user') || '{}');
        const userId = userInfo.id;

        if (userId) {
            websocketClient.connect(userId);
            // 统一注册通知回调
            websocketClient.onNotification((title, content, type) => {
                console.log('🔔 收到通知:', title, content, type)
                
                // 使用 Element UI 的通知组件
                this.$notify({
                    title: title,
                    message: content,
                    type: type,
                    duration: 3000,
                    position: 'top-right'
                });
            });
        }
    },
  beforeDestroy() {
    // 组件销毁时断开连接
    websocketClient.disconnect();
  }
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #000;
  height: 100%;
}
html,
body,
div,
span,
h1,
h2,
h3,
h4,
h5,
h6,
ul,
ol,
li,
p{
    margin: 0;
    padding: 0;
}

html,body,#app{
    height: 100%;
    width: 100%;
    font-family: "微软雅黑";
}

ul,ol{
    list-style: none;
}
a{
    text-decoration: none;
}

</style>
