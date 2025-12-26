import request from './request'; // 引入你封装好的 axios 实例
import type { ApiResponse, TaskPrediction, Task } from '../types';

// 获取任务列表
export const getTaskList = () => {
    return request.get<ApiResponse<Task[]>>('/api/task/list');
};

// 新增：调用 DB4AI 预测
export const predictTaskStatus = (taskId: string) => {
    return request.get<ApiResponse<TaskPrediction>>(`/api/task/predict/${taskId}`);
};

// 完成任务
export const completeTask = (taskId: string) => {
    return request.post<ApiResponse<any>>(`/api/task/complete/${taskId}`); // 假设你用POST
};
