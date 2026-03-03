<template>
    <div class="login-bg">
        <div class="login">
			<div class="message">颐养中心</div>
			<div id="darkbannerwrap"></div>
            <form >
                <input name="username"  v-model="loginForm.username" placeholder="用户名" type="text" >
                <hr class="hr15">
                <input name="password"  v-model="loginForm.password" placeholder="密码" type="password">
                <hr class="hr15">
                <input value="登录"  @click="login"  style="width:100%;" type="button">
                <hr class="hr20">
                <el-row justify="end">
                    <el-button circle 
                        style="background-color: var(--el-color-danger-light-8); 
                        border-color: var(--el-color-danger-light-7);"
                    >
                        <el-icon><Edit /></el-icon>
                    </el-button>
                </el-row>
            </form>
	    </div>
  </div>
</template>


<script>
import { login } from '../api/userApi.js'
import { setSessionStorage } from '@/utils/common.js'

export default {
    data() {
        return {
            loginForm: {
                username: '',
                password: ''
            }
        }
    },
    methods: {
        login() {
            login(this.loginForm).then(res => {
                // console.log('this.loginForm==========>',this.loginForm)
                console.log('res==========>',res)
                if (res.flag) {
                    // 将token保存到sessionStorage中
                    sessionStorage.setItem('token', res.message)
                    // 用户的信息存入sessionStorage中 json
                    setSessionStorage('user', res.data)
                    // 将菜单列表存到vuex
                    this.$store.commit('addMenus', res.data.menuList)
                    // 路由到第一个子菜单 /bed/BedMap.vue
                    this.$router.push(res.data.menuList[0].children[0])
                } else {
                    this.$message.error(res.message)
                }
            })
        }
    }
}
</script>

<style scoped>

    .login-bg {
        width: 100%;
        height: 100%;
        background: url(../assets/falllogin.jpg) no-repeat center;
        background-size: cover;
        /* 裁剪掉超出元素框的内容，并且不会显示滚动条 */
        overflow: hidden
    }
    .login {
        margin: 200px auto 0 auto;
        min-height: 420px;
        max-width: 420px;
        padding: 40px;
        background-color: #fff;
        border-radius: 4px;
        box-sizing: border-box;
    }
    .login .message {
        margin: 10px 0 0 -58px;
        padding: 18px 10px 18px 60px;
        background: #44aff0;
        position: relative;
        color: #fff;
        font-size: 20px
    }

    .login #darkbannerwrap {
        width: 18px;
        height: 10px;
        margin: 0 0 20px -58px;
        position: relative
    }
	
     /* 满足input的样式才设定该样式 */
    .login input[type=password],
    .login input[type=text],
    select {
        border: 1px solid #DCDEE0;
        vertical-align: middle;
        border-radius: 3px;
        height: 50px;
        padding: 0 16px;
        font-size: 14px;
        color: #555;
        outline: 0;
        width: 100%;
        box-sizing: border-box
    }
	/* 鼠标点击或者tab键获得焦点时，有一个蓝色的框 */
    .login input[type=email]:focus,
    .login input[type=file]:focus,
    .login input[type=password]:focus,
    .login input[type=text]:focus,
    select:focus {
        border: 1px solid #44aff0
    }
	
   /* 按钮样式 */
    .login input[type=button],
    .login input[type=submit] {
        display: inline-block;
        vertical-align: middle;
        padding: 12px 24px;
        margin: 0;
        font-size: 18px;
        line-height: 24px;
        text-align: center;
        white-space: nowrap;
        vertical-align: middle;
        cursor: pointer;
        color: #fff;
        background-color: #44aff0;
        border-radius: 3px;
        border: none;
        -webkit-appearance: none;
        outline: 0;
        width: 100%
    }

    .login hr {
        background: #fff;
    }

    .login hr.hr15 {
        height: 15px;
        border: none;
        margin: 0;
        padding: 0;
        width: 100%
    }

    .login hr.hr20 {
        height: 20px;
        border: none;
        margin: 0;
        padding: 0;
        width: 100%
    }

</style>