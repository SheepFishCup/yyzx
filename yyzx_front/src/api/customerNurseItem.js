// 引入封装好的axios  @等价于/src
import http from '@/request/request.js';

//为顾客单个/批量添加护理项目
function addItemToCustomer(data){
    return http.post('/customernurseitem/addItemToCustomer',data,{ useJson: true })
}
//移除客户护理级别级联移除用户当前级别的护理项目
function removeCustomerLevelAndItem(data){
    return http.get('/customernurseitem/removeCustomerLevelAndItem',{params:data})
}
//查询某个顾客的护理项目列表-分页
function listCustomerItem(data){
    return http.get('/customernurseitem/listCustomerItem',{params:data})
}
//客户续费
function enewNurseItem(data){
    return http.post('/customernurseitem/enewNurseItem',data)
}
//判断用户是否已经配置了某个指定项目
function isIncludesItemCustomer(data){
    return http.get('/customernurseitem/isIncludesItemCustomer',{params:data})
}
//移除客户护理项目
function removeCustomerItem(data){
    return http.get('/customernurseitem/removeCustomerItem',{params:data})

}
export{
    addItemToCustomer,
    removeCustomerLevelAndItem,
    listCustomerItem,
    enewNurseItem,
    isIncludesItemCustomer,
    removeCustomerItem
}