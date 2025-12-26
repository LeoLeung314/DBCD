package com.workshop.task.entity.vo;

import lombok.Data;

/**
 * DB4AI 预测结果视图对象
 * 对应数据库函数 predict_task_status_4746 的返回结果
 */
@Data
public class TaskPredictionVO {
    private String taskId;          // 任务编号
    private String currentProgress; // 当前进度 (例如 "50.00%")
    private String aiPrediction;    // AI预测结果 (例如 "达标" 或 "不达标")
    private String warningMsg;      // 预警信息 (例如 "⚠️ 警告..." 或 "正常")
}
