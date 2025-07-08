// 引入封装好的axios  @等价于/src
import http from '@/request/request.js';


//查询食品项目
function findFood(){
    return http.get('/food/listFood')

}

export{
    findFood
}