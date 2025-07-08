
// 获取当前的一个时间 js
export function getCurDate() {
    // 获取年月日
    var now = new Date();
    var year = now.getFullYear();
    var month = now.getMonth() + 1;
    var day = now.getDate();
    // 月小于10补0
    month = month < 10 ? "0" + month : month;
    // 日小于10补0
    day = day < 10 ? "0" + day : day;
    return year + "-" + month + "-" + day;
}

// 向sessionStorage中存储一个json对象
export function setSessionStorage(key, value) {
    sessionStorage.setItem(key, JSON.stringify(value));
}


// 从sessionStorage中获取一个json对象
export function getSessionStorage(key) {
    var str = sessionStorage.getItem(key);
    if (str == '' || str == null || str == 'null' || str == undefined) {
        return null;
    }else {
        return JSON.parse(str);
    }
}

// 从sessionStorage中删除一个json对象
export function removeSessionStorage(key) {
    sessionStorage.removeItem(key);
}

// 向localStorage中存储一个json对象
export function setlocalStorage(key, value) {
    localStorage.setItem(key, JSON.stringify(value));
}

// 从localStorage中获取一个json对象
export function getlocalStorage(key) {
    var str = localStorage.getItem(key);
    if (str == '' || str == null || str == 'null' || str == undefined) {
        return null;
    }else {
        return JSON.parse(str);
    }
}

// 从localStorage中删一个json对象
export function removelocalStorage(key) {
    localStorage.removeItem(key);
}