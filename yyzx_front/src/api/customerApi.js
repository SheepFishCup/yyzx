// 引入封装好的axios  @等价于/src
import http from '@/request/request.js';

// 入住登记-入住
function rzdjCustomer(data){
    return http.post('/customer/addCustomer',data)
}
//入住登记-客户信息列表
function getKhxxList(data){
    return http.get('/customer/listKhxxPage',{params:data})
}
//删除客户
function delCustomer(data){
    return http.get('/customer/removeCustomer',{params:data})
}
//编辑客户
function editCustomer(data){
    return http.post('/customer/editCustomer',data)

}
export{
    rzdjCustomer,
    getKhxxList,
    delCustomer,
    editCustomer
}