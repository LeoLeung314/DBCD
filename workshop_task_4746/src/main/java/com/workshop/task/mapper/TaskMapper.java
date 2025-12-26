package com.workshop.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.workshop.task.entity.Task4746;
import com.workshop.task.entity.vo.TaskPredictionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TaskMapper extends BaseMapper<Task4746> {

    // 调用数据库中的存储函数 complete_task_4746
    @Select("SELECT complete_task_4746(#{taskId})")
    String callCompleteTask(@Param("taskId") String taskId);

    /**
     * 调用 openGauss DB4AI 函数进行任务状态预测
     * SQL对应: SELECT * FROM predict_task_status_4746('T001')
     */
    @Select("SELECT * FROM predict_task_status_4746(#{taskId})")
    TaskPredictionVO predictTaskStatus(@Param("taskId") String taskId);

}
