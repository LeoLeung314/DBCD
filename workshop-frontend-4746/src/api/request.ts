import axios from 'axios';

const request = axios.create({
    baseURL: '/api', // 对应 vite.config.ts 中的代理
    timeout: 5000
});

export default request;