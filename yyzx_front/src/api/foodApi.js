// 引入封装好的axios  @等价于/src
import http from '@/request/request.js';


//查询食品项目
function findFood(){
    return http.get('/food/listFood')
}

//图片文件上传接口
function uploadFoodImg(file){
    return http.post('/food/upload', file, {
        transformRequest: [(data) => data]  // 确保数据原样发送
    })
}
//添加食物
function addFood(data){
    return http.post('/food/addFood',data,{ useJson: true })
}
export{
    findFood,
    uploadFoodImg,
    addFood
}