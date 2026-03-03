import axios from "axios";
import qs from 'qs';
import router from '../router';
import { ElMessage } from 'element-plus'; 

// 创建 axios 实例
const instance = axios.create({
    baseURL: 'http://localhost:9999/yyzx',
    timeout: 10000 // 设置超时时间为 10 秒
});

// 存储正在进行的请求（用于防抖）
const pendingRequests = new Map();

// 生成请求唯一标识
function generateRequestKey(config) {
    const { method, url, params, data } = config;
    return [method, url, qs.stringify(params), qs.stringify(data)].join('&');
}

// 添加请求到 pendingRequests
function addPending(config) {
    const key = generateRequestKey(config);
    config.cancelToken = config.cancelToken || new axios.CancelToken((cancel) => {
        if (!pendingRequests.has(key)) {
            pendingRequests.set(key, cancel);
        }
    });
}

// 移除请求
function removePending(config) {
    const key = generateRequestKey(config);
    if (pendingRequests.has(key)) {
        pendingRequests.delete(key);
    }
}

// 清空所有 pending 请求
export function clearAllPending() {
    for (const [key, cancel] of pendingRequests) {
        cancel(`取消所有 pending 中的请求: ${key}`);
    }
    pendingRequests.clear();
}

// Token 校验函数（假设 Token 是 JWT 格式）
function isTokenExpired(token) {
    try {
        const payload = JSON.parse(atob(token.split('.')[1])); // 解析 JWT payload
        return Date.now() >= payload.exp * 1000; // 检查是否过期
    } catch (e) {
        return true; // 解析失败默认认为已过期
    }
}

// 请求拦截器
instance.interceptors.request.use(
    function (config) {
        addPending(config); // 添加请求到 pending 列表

        // Token 校验与携带
        let token = sessionStorage.getItem("token");
        if (token && isTokenExpired(token)) {
            sessionStorage.removeItem("token"); // 删除过期 Token
            router.push("/login"); // 跳转登录页
        } else if (token) {
            config.headers['token'] = token; // 携带有效 Token
        }

        // 定义需要 application/json 格式的 POST 接口
        const jsonQueryParamsPath = [
            // '/customernurseitem/addItemToCustomer',
            // '/backdown/addBackdown'
        ];

        // 设置 POST 请求参数格式
        if (config.method === 'post' && !jsonQueryParamsPath.includes(config.url) && !config.useJson) {
            config.data = qs.stringify(config.data);
        }
        // else if (config.method === 'delete' && config.params) {
        //     // 如果 DELETE 请求使用 params 传递参数，确保格式正确
        //     config.paramsSerializer = function (params) {
        //         return qs.stringify(params);
        //     };
        // }

        return config;
    },
    function (error) {
        return Promise.reject(error);
    }
);

// 响应拦截器
instance.interceptors.response.use(
    function (response) {
        removePending(response.config); // 移除已完成的请求

        // Token 异常处理
        if (!response.data.flag && response.data.data === "token_error") {
            sessionStorage.removeItem("token");
            router.push("/login");
        }

        return response.data;
    },
    function (error) {
        // 处理请求被取消的情况
        if (axios.isCancel(error)) {
            return new Promise(() => {}); // 忽略取消的请求
        }

        removePending(error.config); // 移除失败的请求

        // 错误处理
        if (error.response) {
            const status = error.response.status;
            const message = error.response.data?.message || '请求出错';
            switch (status) {
                case 401:
                    ElMessage.error('未授权，请重新登录');
                    break;
                case 403:
                    ElMessage.error('拒绝访问');
                    break;
                case 404:
                    ElMessage.error('请求资源不存在');
                    break;
                case 500:
                case 502:
                case 503:
                    ElMessage.error('服务器错误，请稍后重试');
                    break;
                default:
                    ElMessage.error(message);
                    break;
            }
        } else if (error.code === 'ECONNABORTED') {
            ElMessage.error('网络请求超时');
        } else if (error.message === 'Network Error') {
            ElMessage.error('网络连接错误');
        } else {
            ElMessage.error(error.message || '未知错误');
        }

        return Promise.reject(error);
    }
);

export default instance;