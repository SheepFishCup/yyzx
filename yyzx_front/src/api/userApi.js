// 引入封装好的axios  @等价于/src
import http from '@/request/request.js';
//用户登录
function login(data){
    return http.get('/user/login',{params:data})
}
//查询系统用户信息
function getUserList(data){
    return http.get('/user/findUserPage',{params:data})
}
//添加用户
function addUser(data){
    return http.post('/user/addUser',data)
}
//更新护理项目
function updateUser(data){
    return http.post('/user/updateUser',data)
}
//删除护理项目
function delUser(data){
    return http.get('/user/delUser',{params:data})
}
//查询系统用户信息
function findAllUser(data){
    return http.get('/user/findAllUserPage',{params:data})
}
export{
    login,
    getUserList,
	addUser,
	updateUser,
	delUser,
	findAllUser
}