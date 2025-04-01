import axios from 'axios';
import {createStore} from 'vuex'
import createPersistedState from 'vuex-persistedstate'
import {message} from "ant-design-vue";

interface User {
    id?: any;
    name?: string;
    points?: number;
    token?: string;
}

let pollingInstance: number | null | undefined = null;

const store = createStore({
    state: {
        user: {} as User
    },
    mutations: {
        setUser(state, user) {
            console.log("store user：", user);
            state.user = user;
        }
    },
    actions: {
        getUser({commit, state,}, token) {
            if (state.user?.token) {
                token = state.user?.token;
            }
            if (token) {
                axios.get('/user/get-current-login')
                    .then(response => {
                        console.log("getUser:", response);
                        if (response.data.success) {
                            commit('setUser', response.data.content);
                        }
                    })
            } else {
                console.error("getUser: token is empty");
                commit('setUser', {});
            }
        },
        // 启动长轮询
        async startPolling({state, commit}) {
            if (!state.user.token) return;

            const poll = async () => {
                await axios.get('/user/long-poll', {
                    timeout: 35000, // 需要大于后端超时时间（30秒）
                }).then(response => {
                    const data = response.data
                    console.log('收到新数据:', data);
                    if (data.success && data.content === 'SUCCESS') {
                        message.success("积分充值成功!");
                        store.dispatch('getUser');
                    }
                    // 递归调用维持长轮询
                    pollingInstance = setTimeout(poll, 0);
                }).catch(error => {
                    if (axios.isCancel(error)) {
                        console.log('请求被取消:', error.message);
                    } else if (error.code === 'ECONNABORTED') {
                        console.log('请求超时，重新发起轮询...');
                        pollingInstance = setTimeout(poll, 0);
                    } else {
                        console.error('轮询异常:', error);
                        // 网络错误时延迟重试
                        pollingInstance = setTimeout(poll, 5000);
                    }
                })
            };
            await poll(); // 启动首次请求
        },

        // 停止轮询
        stopPolling({commit}) {
            if (pollingInstance !== null && pollingInstance !== undefined) {
                clearTimeout(pollingInstance);
            }
        }
    },
    modules: {},
    plugins: [
        createPersistedState({
            storage: window.localStorage, // 使用 sessionStorage
            paths: ['user'], // 仅持久化 user 状态
        })
    ]
});

export default store;
