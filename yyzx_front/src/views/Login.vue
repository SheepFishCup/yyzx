<template>
    <div class="login-bg">
        <div class="login">
            <div class="message">颐养中心</div>
            <form>
                <el-input 
                    v-model="loginForm.username" 
                    placeholder="用户名"
                    prefix-icon="User"
                    class="custom-input"
                />
                <hr class="hr15">
                <el-input 
                    v-model="loginForm.password" 
                    type="password"
                    placeholder="密码"
                    show-password
                    prefix-icon="Lock"
                    class="custom-input"
                    @keyup.enter="login"
                />
                <hr class="hr15">
                
                <!-- 验证码区域 -->
                <div class="captcha-box">
                    <el-input 
                        v-model="loginForm.captcha" 
                        placeholder="验证码"
                        class="custom-input captcha-input"
                    />
                    <img 
                        :src="captchaImg" 
                        @click="refreshCaptcha" 
                        class="captcha-img" 
                        alt="验证码"
                        title="点击刷新"
                    />
                </div>
                <hr class="hr15">
                
                <el-button 
                    type="primary" 
                    @click="login"
                    :loading="loginLoading"
                    class="login-btn"
                >
                    登录
                </el-button>
                <hr class="hr20">
                
                <div class="extra-links">
                    <el-link type="primary" @click="showForgotPassword">忘记密码？</el-link>
                </div>
            </form>
        </div>
        
        <!-- 忘记密码弹窗 -->
        <el-dialog 
            v-model="forgotPasswordDialogVisible" 
            title="忘记密码"
            width="400px"
            class="forgot-dialog"
        >
            <el-form 
                ref="forgotPasswordForm"
                :model="forgotPasswordFormData" 
                :rules="forgotPasswordRules"
                label-width="80px"
            >
                <el-form-item label="邮箱" prop="email">
                    <el-input 
                        v-model="forgotPasswordFormData.email" 
                        placeholder="请输入注册邮箱"
                        class="custom-input"
                    />
                </el-form-item>
                
                <el-form-item label="验证码" prop="captcha">
                    <div class="captcha-box">
                        <el-input 
                            v-model="forgotPasswordFormData.captcha" 
                            placeholder="验证码"
                            class="custom-input captcha-input"
                        />
                        <img 
                            :src="forgotPasswordCaptchaImg" 
                            @click="refreshForgotPasswordCaptcha" 
                            class="captcha-img" 
                            alt="验证码"
                            title="点击刷新"
                        />
                    </div>
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="forgotPasswordDialogVisible = false">取消</el-button>
                <el-button 
                    type="primary" 
                    @click="handleForgotPassword"
                    :loading="forgotPasswordLoading"
                >
                    发送重置链接
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script>
import { loginWithCaptcha, generateCaptcha, UserforgotPassword } from '../api/userApi.js'
import { setSessionStorage } from '@/utils/common.js'

export default {
    data() {
        return {
            loginLoading: false,
            loginForm: {
                username: '',
                password: '',
                captcha: '',
                uuid: ''
            },
            captchaImg: '',
            captchaGeneratedTime: 0,
            captchaLoading: false,
            
            // 忘记密码相关
            forgotPasswordDialogVisible: false,
            forgotPasswordFormData: {
                email: '',
                captcha: '',
                uuid: ''
            },
            forgotPasswordRules: {
                email: [
                    { required: true, message: '请输入邮箱', trigger: 'blur' },
                    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
                ],
                captcha: [
                    { required: true, message: '请输入验证码', trigger: 'blur' },
                    { min: 4, max: 4, message: '验证码为 4 位', trigger: 'blur' }
                ]
            },
            forgotPasswordCaptchaImg: '',
            forgotPasswordLoading: false
        }
    },
    mounted() {
        this.refreshCaptcha()
    },
    methods: {
        refreshCaptcha() {
            if (this.captchaLoading) return
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
        refreshForgotPasswordCaptcha() {
            generateCaptcha({}).then(res => {
                if (res && res.data && res.data.base64) {
                    this.forgotPasswordCaptchaImg = res.data.base64
                    this.forgotPasswordFormData.uuid = res.data.uuid
                    this.forgotPasswordFormData.captcha = ''
                }
            })
        },
        login() {
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
            
            this.loginLoading = true
            const loginData = {
                username: this.loginForm.username,
                password: this.loginForm.password,
                captcha: this.loginForm.captcha,
                uuid: this.loginForm.uuid
            }

            loginWithCaptcha(loginData).then(res => {
                if (res.flag) {
                    sessionStorage.setItem('token', res.message)
                    setSessionStorage('user', res.data)
                    this.$store.commit('addMenus', res.data.menuList)
                    this.$router.push('/home')
                } else {
                    this.$message.error(res.message)
                    this.refreshCaptcha()
                    this.loginForm.captcha = ''
                }
            }).catch(err => {
                console.error('登录请求失败:', err)
                this.$message.error('登录请求失败')
            }).finally(() => {
                this.loginLoading = false
            })
        },
        showForgotPassword() {
            this.forgotPasswordDialogVisible = true
            this.refreshForgotPasswordCaptcha()
        },
        handleForgotPassword() {
            this.$refs.forgotPasswordForm.validate(valid => {
                if (!valid) return
                
                this.forgotPasswordLoading = true
                UserforgotPassword({
                    email: this.forgotPasswordFormData.email,
                    captcha: this.forgotPasswordFormData.captcha,
                    uuid: this.forgotPasswordFormData.uuid
                }).then(res => {
                    if (res.flag) {
                        this.$message.success('重置链接已发送到您的邮箱')
                        this.forgotPasswordDialogVisible = false
                    } else {
                        this.$message.error(res.message)
                        this.refreshForgotPasswordCaptcha()
                    }
                }).catch(err => {
                    console.error('忘记密码请求失败:', err)
                    this.$message.error('操作失败，请稍后重试')
                }).finally(() => {
                    this.forgotPasswordLoading = false
                })
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
    overflow: hidden;
}

.login {
    margin: 200px auto 0 auto;
    min-height: 420px;
    max-width: 420px;
    padding: 40px;
    background-color: #fff;
    border-radius: 4px;
    box-sizing: border-box;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.login .message {
    margin: 10px 0 20px -58px;
    padding: 18px 10px 18px 60px;
    background: #44aff0;
    position: relative;
    color: #fff;
    font-size: 20px
}

/* 统一所有输入框样式 */
.custom-input {
    width: 100%;
    box-sizing: border-box;
}

/* 统一输入框容器样式 */
.custom-input :deep(.el-input__wrapper) {
    border: 1px solid #DCDEE0;
    border-radius: 3px;
    height: 50px;
    box-shadow: none !important;
    padding: 0 12px 0 12px !important;
    transition: border-color 0.3s;
    box-sizing: border-box;
}

.custom-input:hover :deep(.el-input__wrapper) {
    border-color: #44aff0;
}

.custom-input:focus-within :deep(.el-input__wrapper) {
    border-color: #44aff0;
}

/* 输入框内部文字样式 */
.custom-input :deep(.el-input__inner) {
    height: 48px;
    line-height: 48px;
    font-size: 14px;
    color: #555;
    padding-left: 32px !important;
}

/* 前缀图标样式 */
.custom-input :deep(.el-input__prefix) {
    left: 8px;
}

/* 后缀图标样式（显示密码按钮）*/
.custom-input :deep(.el-input__suffix) {
    right: 8px;
}

/* 验证码容器样式 - 核心修复 */
.captcha-box {
    display: flex;
    align-items: center;
    gap: 5%;
    width: 100%;
}

/* 验码输入框特殊样式 */
.captcha-input {
    width: 60% !important;
}

.captcha-input :deep(.el-input__wrapper) {
    padding: 0 8px 0 8px !important;
}

.captcha-input :deep(.el-input__inner) {
    padding-left: 12px !important;
}

/* 验证码图片样式 */
.captcha-img {
    width: 35%;
    height: 50px;
    cursor: pointer;
    border: 1px solid #DCDEE0;
    border-radius: 3px;
}

.login-btn {
    display: inline-block;
    width: 100%;
    height: 50px;
    font-size: 18px;
    background-color: #44aff0;
    border: none;
    border-radius: 3px;
    color: #fff;
}

.login-btn:hover {
    background-color: #3a9fe0;
}

.extra-links {
    text-align: right;
    margin-top: 10px;
}

.login hr {
    background: #fff;
    border: none;
    height: 15px;
}

.login .hr15 {
    height: 15px;
    border: none;
    margin: 0;
    padding: 0;
    width: 100%
}

.login .hr20 {
    height: 20px;
}

.forgot-dialog :deep(.el-form-item) {
    margin-bottom: 20px;
}

.forgot-dialog :deep(.el-input__wrapper) {
    border: 1px solid #DCDEE0;
    border-radius: 3px;
    box-shadow: none !important;
    padding: 0 12px 0 12px !important;
}

.forgot-dialog :deep(.el-input__inner) {
    padding-left: 12px !important;
}

.forgot-dialog :deep(.el-input__wrapper:hover),
.forgot-dialog :deep(.el-input__wrapper:focus-within) {
    border-color: #44aff0;
}
</style>