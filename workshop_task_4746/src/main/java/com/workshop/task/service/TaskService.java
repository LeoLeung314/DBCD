package com.workshop.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.workshop.task.entity.Task4746;
import com.workshop.task.entity.vo.TaskPredictionVO;


public interface TaskService extends IService<Task4746> {

    TaskPredictionVO getTaskPrediction(String taskId);

    String completeTask(String taskId);
}
