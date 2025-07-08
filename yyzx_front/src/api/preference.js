// 引入封装好的axios  @等价于/src
import http from '@/request/request.js';

//添加护理项目
function addPreference(data){
    return http.post('/customerpreference/addCustomerpreference',data)
}
//更新护理项目
function updatePreference(data){
    return http.post('/customerpreference/updateCustomerpreference',data)
}
//删除护理项目
function delPreference(data){
    return http.get('/customerpreference/removeCustomerpreference',{params:data})
}

//查询护理项目(分页)
function findPreference(data){
    return http.get('/customerpreference/listCustomerpreference',{params:data})

}

export{
    addPreference,
    findPreference,
    updatePreference,
    delPreference
}