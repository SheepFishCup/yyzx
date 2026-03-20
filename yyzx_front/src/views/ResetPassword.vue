<template>
    <div class="reset-container">
        <div class="reset-card">
            <h2>重置密码</h2>
            
            <div v-if="!tokenValid" class="invalid-token">
                <el-result
                    icon="error"
                    title="链接已失效"
                    sub-title="该重置链接已过期或无效，请重新申请"
                >
                    <template #extra>
                        <el-button type="primary" @click="goToLogin">返回登录</el-button>
                    </template>
                </el-result>
            </div>
            
            <el-form 
                v-else 
                ref="resetPasswordForm"
                :model="form" 
                :rules="rules" 
                label-width="80px"
                size="large"
            >
                <el-form-item label="新密码" prop="newPassword">
                    <el-input 
                        v-model="form.newPassword" 
                        type="password"
                        placeholder="6-20位字母数字组合"
                        show-password
                        prefix-icon="Lock"
                    />
                </el-form-item>
                
                <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input 
                        v-model="form.confirmPassword" 
                        type="password"
                        placeholder="请再次输入新密码"
                        show-password
                        prefix-icon="Lock"
                    />
                </el-form-item>
                
                <el-button 
                    type="primary" 
                    @click="handleReset"
                    :loading="resetting"
                    class="submit-btn"
                >
                    确认重置
                </el-button>
            </el-form>
        </div>
    </div>
</template>

<script>
import { UserverifyResetToken, UserresetPassword } from '@/api/userApi.js'

export default {
    data() {
        return {
            token: '',
            tokenValid: false,
            resetting: false,
            form: {
                newPassword: '',
                confirmPassword: ''
            },
            rules: {
                newPassword: [
                    { required: true, message: '请输入新密码', trigger: 'blur' },
                    { min: 6, max: 20, message: '密码长度必须为 6-20 位', trigger: 'blur' },
                    { 
                        pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,20}$/, 
                        message: '密码必须包含字母和数字', 
                        trigger: 'blur' 
                    }
                ],
                confirmPassword: [
                    { required: true, message: '请再次输入密码', trigger: 'blur' },
                    { validator: (rule, value, callback) => {
                        if (value !== this.form.newPassword) {
                            callback(new Error('两次输入的密码不一致'))
                        } else {
                            callback()
                        }
                    }, trigger: 'blur' }
                ]
            }
        }
    },
    created() {
        this.token = this.$route.query.token
        console.log('重置令牌:', this.token)
        
        if (!this.token) {
            this.tokenValid = false
            return
        }
        
        // 统一使用 userApi 接口
        UserverifyResetToken(this.token).then(res => {
            this.tokenValid = res.flag
        }).catch(() => {
            this.tokenValid = false
        })
    },
    methods: {
        handleReset() {
            this.$refs.resetPasswordForm.validate(valid => {
                if (!valid) return
                
                this.resetting = true
                
                // 统一使用 userApi 接口
                UserresetPassword({
                    token: this.token,
                    newPassword: this.form.newPassword,
                    confirmPassword: this.form.confirmPassword
                }).then(res => {
                    if (res.flag) {
                        this.$message.success(res.message || '密码重置成功，请重新登录')
                        setTimeout(() => {
                            this.$router.push('/login')
                        }, 2000)
                    } else {
                        this.$message.error(res.message || '密码重置失败')
                    }
                }).catch(err => {
                    console.error('密码重置失败:', err)
                    this.$message.error('操作失败，请稍后重试')
                }).finally(() => {
                    this.resetting = false
                })
            })
        },
        goToLogin() {
            this.$router.push('/login')
        }
    }
}
</script>

<style scoped>
.reset-container {
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: url('../assets/falllogin.jpg') no-repeat center center fixed;
    background-size: cover;
    position: relative;
}

.reset-container::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.3);
}

.reset-card {
    position: relative;
    width: 450px;
    padding: 40px;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 20px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    z-index: 10;
}

.reset-card h2 {
    text-align: center;
    margin-bottom: 30px;
    color: #333;
    font-size: 24px;
}

.submit-btn {
    width: 100%;
    margin-top: 20px;
    height: 45px;
    font-size: 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
}

.invalid-token {
    padding: 20px 0;
}

:deep(.el-form-item) {
    margin-bottom: 25px;
}
</style>