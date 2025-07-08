// 引入封装好的axios  @等价于/src
import http from '@/request/request.js';

//添加护理项目
function addNurseItem(data){
    return http.post('/nursecontent/addNurseItem',data)
}
//更新护理项目
function updateNurseItem(data){
    return http.post('/nursecontent/updateNurseItem',data)
}
//删除护理项目
function delNurseItem(data){
    return http.get('/nursecontent/delNurseItem',{params:data})
}

//查询护理项目(分页)
function findNurseItem(data){
    return http.get('/nursecontent/findNurseItemPage',{params:data})

}

export{
    addNurseItem,
    findNurseItem,
    updateNurseItem,
    delNurseItem
}