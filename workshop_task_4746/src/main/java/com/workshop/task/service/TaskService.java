package com.workshop.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.workshop.task.entity.Task4746;
import com.workshop.task.entity.vo.TaskPredictionVO;

// 注意这里是 interface，不是 class
public interface TaskService extends IService<Task4746> {

    // 只需要这一行，分号结尾
    TaskPredictionVO getTaskPrediction(String taskId);

    // 如果有其他方法（比如 completeTask），也只写定义
    String completeTask(String taskId);
}
