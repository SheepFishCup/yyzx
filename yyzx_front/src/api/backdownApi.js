// 引入封装好的axios  @等价于/src
import http from '@/request/request.js';
//查询退住记录
function queryBackdownVo(data){
    return http.get('/backdown/listBackdownVo',{params:data})
}
//添加退住登记
function addBackdown(data){
    return http.post('/backdown/addBackdown',data)

}
//审批外出申请
function examineBackdown(data){
    return http.post('/backdown/examineBackdown',data)
}
//撤回外出申请
function delBackdown (data) {
    return http.get('/backdown/delBackdown',{params:data})
}
export{
    addBackdown,
    examineBackdown,
    delBackdown,
	queryBackdownVo
}