// 引入封装好的axios  @等价于/src
import http from '@/request/request.js';

//查询床位列表
function listRoom(){
    return http.get('/room/listRoom')
}
export{
    listRoom
}