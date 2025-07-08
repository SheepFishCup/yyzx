// 引入封装好的axios  @等价于/src
import http from '@/request/request.js';

//添加护理级别
function addNurseLevel(data){
    return http.post('/nurselevel/addNurseLevel',data)
}
//更新护理级别
function updateNurseLevel(data){
    return http.post('/nurselevel/updateNurseLevel',data)
}

//护理项目的配置
function addItemToLevel(data){
    return http.post('/nurselevel/addItemToLevel',data)
}

//删除护理级别
function removeNurseLevel(data){
    return http.get('/nurselevel/removeNurseLevel',{params:data})
}

//查询护理级别列表
function listNurseLevel(data){
    return http.get('/nurselevel/listNurseLevel',{params:data})

}
//根据护理级别查询护理项目-启用的
function listNurseItemByLevel(data){
    return http.get('/nurselevel/listNurseItemByLevel',{params:data})

}
//删除护理级别中的护理项目 removeNurseLevelItem
function removeNurseLevelItem(data){
    return http.get('/nurselevel/removeNurseLevelItem',{params:data})
}

export{
    addNurseLevel,
    updateNurseLevel,
    addItemToLevel,
    removeNurseLevel,
    listNurseLevel,
    listNurseItemByLevel,
    removeNurseLevelItem
}