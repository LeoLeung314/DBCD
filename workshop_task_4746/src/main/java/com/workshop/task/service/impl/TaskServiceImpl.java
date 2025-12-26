package com.workshop.task.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.workshop.task.entity.Task4746;
import com.workshop.task.entity.vo.TaskPredictionVO;
import com.workshop.task.mapper.TaskMapper;
import com.workshop.task.service.TaskService;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task4746> implements TaskService {

    // 必须加上 @Override 并写出方法体 {}
    @Override
    public TaskPredictionVO getTaskPrediction(String taskId) {
        // 具体的逻辑写在这里
        return baseMapper.predictTaskStatus(taskId);
    }

    @Override
    public String completeTask(String taskId) {
        return baseMapper.callCompleteTask(taskId);
    }
}
