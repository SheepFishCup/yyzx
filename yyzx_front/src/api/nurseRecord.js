// 引入封装好的axios  @等价于/src
import http from '@/request/request.js';

//添加护理记录
function addNurseRecord(data){
    return http.post('/nurserecord/addNurseRecord',data)

}
//客户护理记录信息动态查询
function listNurseRecordsVo(data){
    return http.get('/nurserecord/listNurseRecordsVo',{params:data})
}
//移除护理记录
function removeCustomerRecord(data){
    return http.get('/nurserecord/removeCustomerRecord',{params:data})
}
//查询外出记录
function queryOutwardVo(data){
    return http.get('/nurserecord/queryOutwardVo',{params:data})
}
export{
    addNurseRecord,
    listNurseRecordsVo,
    removeCustomerRecord,
	queryOutwardVo
}