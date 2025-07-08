// 引入封装好的axios  @等价于/src
import http from '@/request/request.js';


function addMeal(data){
    return http.post('/meal/addMeal',data)
}

function updateMeal(data){
    return http.post('/meal/updateMeal',data)
}
function delMeal(data){
    return http.get('/meal/removeMeal',{params:data})
}


function findMeal (data){
    return http.get('/meal/listMealPage',{params:data})
}

export{
    addMeal,
    updateMeal,
    delMeal,
    findMeal
}