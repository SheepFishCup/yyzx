// 引入封装好的axios  @等价于/src
import http from '@/request/request.js';

//获取床位示意图视图数据
function findCwsyBedVo(data){
    return http.get('/room/findCwsyBedVo',{params:data})
}
//根据房间查询床位信息
function findBedByRoom(data){
    return http.get('/bed/findBed',{params:data})
}
//查询床位详情信息-分页
function findBedDetailsList(data){
    return http.get('/beddetails/listBedDetailsVoPage',{params:data})
}
//更新床位详情信息-床位开始使用时间
function updateDetailsStartDate(data){
    return http.post('beddetails/updateBedDetails',data)
}
//床位调换
function exchangeBed(data){
    return http.post('beddetails/exchangeBed',data)
}

//删除床位详情
function delBedDetails(data){
    return http.get('/beddetails/delBedDetails',{params:data})
}
export{
    findCwsyBedVo,
    findBedByRoom,
    findBedDetailsList,
    updateDetailsStartDate,
    exchangeBed,
    delBedDetails
}