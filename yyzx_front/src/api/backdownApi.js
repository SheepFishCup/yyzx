// 引入封装好的axios  @等价于/src
import http from '@/request/request.js';
//查询退住记录
function queryBackdownVo(data){
    return http.post('/backdown/listBackdownVo',data)
}
//添加退住登记
function addBackdown(data){
    return http.post('/backdown/addBackdown',data,{ useJson: true })

}
//审批退住申请
function examineBackdown(data){
    return http.post('/backdown/examineBackdown',data)
}
//撤回退住申请
function delBackdown (data) {
    // return http.get('/backdown/delBackdown',{params:data})
    return http.delete('/backdown',{params:data})
}
export{
    addBackdown,
    examineBackdown,
    delBackdown,
	queryBackdownVo
}