<template>
    <div class="login-bg">
        <div class="login">
			<div class="message">颐养中心</div>
			<div id="darkbannerwrap"></div>
            <form>
                <input name="username" v-model="loginForm.username" placeholder="用户名" type="text">
                <hr class="hr15">
                <input name="password" v-model="loginForm.password" placeholder="密码" type="password">
                <hr class="hr15">
                     <!-- 验证码区域 -->
                <div class="captcha-box">
                <input name="captcha" v-model="loginForm.captcha" placeholder="验证码" type="text">
                <img 
                    :src="captchaImg" 
                    @click="refreshCaptcha" 
                    class="captcha-img" 
                    alt="验证码"
                    title="点击刷新"
                >
                </div>
                <hr class="hr15">
                <input value="登录" @click="login" style="width:100%;" type="button">
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
import { loginWithCaptcha, generateCaptcha } from '../api/userApi.js'
import { setSessionStorage } from '@/utils/common.js'

export default {
    data() {
        return {
            loginForm: {
                username: '',
                password: '',
                captcha: '',
                uuid: '' // 验证码唯一标识，登录时需要传递
            },
            captchaImg: '', // 验证码 base64 图片
            captchaGeneratedTime: 0, // 记录验证码生成时间
            captchaLoading: false // 验证码加载状态
        }
    },
    mounted() {
        this.refreshCaptcha()
    },
    methods: {
        refreshCaptcha() {
            if (this.captchaLoading) return // 防止重复请求
            this.captchaLoading = true

            generateCaptcha({}).then(res => {
                if (res && res.data && res.data.base64) {
                    this.captchaImg = res.data.base64
                    this.loginForm.uuid = res.data.uuid
                    this.loginForm.captcha = ''
                    this.captchaGeneratedTime = Date.now()
                }
            }).finally(() => {
                this.captchaLoading = false
            })
        },
        login() {
            // 校验验证码时效（调试用）
            const captchaAge = Date.now() - this.captchaGeneratedTime

            if (!this.loginForm.username) {
                this.$message.error('请输入用户名')
                return
            }
            if (!this.loginForm.password) {
                this.$message.error('请输入密码')
                return
            }
            if (!this.loginForm.captcha) {
                this.$message.error('请输入验证码')
                return
            }

            const loginData = {
                username: this.loginForm.username,
                password: this.loginForm.password,
                captcha: this.loginForm.captcha,
                uuid: this.loginForm.uuid
            }

            loginWithCaptcha(loginData).then(res => {
                console.log('登录响应:', res)
                if (res.flag) {
                    sessionStorage.setItem('token', res.message)
                    setSessionStorage('user', res.data)
                    this.$store.commit('addMenus', res.data.menuList)
                    // 跳转首页
                    // this.$router.push(res.data.menuList[0].children[0])
                    this.$router.push('/home')
                } else {
                    this.$message.error(res.message)
                    // 登录失败刷新验证码
                    this.refreshCaptcha()
                    this.loginForm.captcha = ''
                }
            }).catch(err => {
                console.error('登录请求失败:', err)
                this.$message.error('登录请求失败')
            })
        }
    }
}
</script>

<style scoped>
/* 原有样式保持不变 */
.login-bg {
    width: 100%;
    height: 100%;
    background: url(../assets/falllogin.jpg) no-repeat center;
    background-size: cover;
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
.login input[type=email]:focus,
.login input[type=file]:focus,
.login input[type=password]:focus,
.login input[type=text]:focus,
select:focus {
    border: 1px solid #44aff0
}
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
/* 验证码区域样式 */
.captcha-box {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.captcha-box input {
    width: 60% !important;
}
.captcha-img {
    width: 35%;
    height: 50px;
    cursor: pointer;
    border: 1px solid #DCDEE0;
    border-radius: 3px;
}
</style>