// 引入封装好的axios  @等价于/src
import http from '@/request/request.js';
//用户登录
function login(data){
    return http.post('/admin/login',data)
}
//生成验证码
function generateCaptcha(data){
    return http.get('/admin/generate',{params:data})
}
//用户登录（带验证码）
function loginWithCaptcha(data){
    return http.post('/admin/loginWithCaptcha',data, { useJson: true })
}
//查询系统用户信息
function getUserList(data){
    return http.get('/admin/findUserPage',{params:data})
}
//添加用户
function addUser(data){
    return http.post('/admin/addUser',data)
}
//更新护理项目
function updateUser(data){
    return http.post('/admin/updateUser',data)
}
//删除护理项目
function delUser(data){
    return http.get('/admin/delUser',{params:data})
}
//查询系统用户信息
function findAllUser(data){
    return http.get('/admin/findAllUserPage',{params:data})
}
export{
    login,
    getUserList,
	addUser,
	updateUser,
	delUser,
	findAllUser,
    generateCaptcha,
    loginWithCaptcha
}