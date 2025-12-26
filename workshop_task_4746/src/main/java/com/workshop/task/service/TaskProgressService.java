package com.workshop.task.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.workshop.task.entity.vo.TaskProgressVo;
import com.workshop.task.mapper.TaskProgressMapper;
import org.springframework.stereotype.Service;

@Service
public class TaskProgressService extends ServiceImpl<TaskProgressMapper, TaskProgressVo> {
    // 用于查询任务进度视图的业务操作
}
