import { createStore } from "vuex"
import { getSessionStorage, setSessionStorage } from "../utils/common.js"

export default createStore({
    state: {
        tabs: [
            
        ],
        // 获取菜单列表
        // menus:getSessionStorage('menusList') || [],
        // 获取菜单列表
        menus:getSessionStorage('menuList') || [],
    },
    getters: {
        // 获取所有的按钮
        tabs(state) {
            return state.tabs;
        },

        // 获取所有的菜单
        menus(state) {
            return state.menus;
        },
        
        getMenuNameByUrl(state) {
            return (url) => {
              const menu = state.menus.find(menu => menu.url === url);
              return menu ? menu.name : '未知菜单';
            }
          }
    },
    // 修改state状态
    mutations: { 
        addMenus(state, param) {
            state.menus = param;
            // setSessionStorage('menusList', param);
            setSessionStorage('menuList', param);
        },
        // 删除指定索引位置的第一个标签
        deleteTabByIndex(state, index) {
            state.tabs.splice(index, 1);
        },

        // 重置标签页
        clearTab(state, param) {
            state.tabs = param;
        },

        // 添加标签页
        addTab(state, payload) {
            let path = payload.path;

            if (path) {
                // 检查当前的Tabs中是否已经存在相同的标签页
                let result = state.tabs.filter((item) => {
                    return item.path === path;
                });
                // 判断是否存在，如果不存在就需要加入到tab中
                if (result.length == 0) {
                    if (state.tabs.length == 10) {
                        // 删除第二个标签页
                        state.tabs.splice(1, 1);
                    }
                    state.tabs.push(payload);
                }
                // 再将新的标签页添加到tabs中
                // state.tabs.push(payload);
            }
        },
    },
    // 用于异步请求
    actions: {
        
    },
    modules: {
        
    },
})
