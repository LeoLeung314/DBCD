// src/types/index.ts

// 1. 通用响应结构
export interface ApiResponse<T> {
    code: number;
    message: string;
    data: T;
}

// 2. 产品实体 (Product4746)
export interface Product {
    productId: string;
    productName: string;
    productType: string;
    unit: string;
    remark: string;
    createTime?: string;
}

// 3. 任务表单实体 - 专门用于"新增任务"表单 (字段较少)
export interface TaskForm {
    taskId: string;
    orderId: string;
    productId: string;
    planOutput: number;
    planStartTime: string;
    planEndTime: string;
}

// 4. 任务实体 - 对应数据库表结构 (用于一般的数据传输)
export interface Task {
    taskId: string;
    orderId?: string;
    productId: string;
    planOutput: number;
    planStartTime: string;
    planEndTime: string;
    taskStatus?: string; // 可选
    actualOutput?: number; // 可选
    createTime?: string; // 可选
}

// 5. 任务进度视图 (TaskProgressVo) - 专门用于"列表展示" (包含进度百分比等计算字段)
export interface TaskProgressVo {
    taskId: string;
    productName: string; // 关联查询出来的
    planOutput: number;
    actualDone: number;
    progressPercentage: number;
    taskStatus: string;
    planStartTime: string;
    planEndTime: string;
    finishTime?: string;
}

// 6. DB4AI 预测结果接口
export interface TaskPrediction {
    taskId: string;
    currentProgress: string; // 例如 "45.50%"
    aiPrediction: string;    // "达标" 或 "不达标"
    warningMsg: string;      // 预警信息
}
