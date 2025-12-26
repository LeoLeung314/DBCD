package com.workshop.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.workshop.task.entity.vo.TaskProgressVo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskProgressMapper extends BaseMapper<TaskProgressVo> {
    // 用于查询视图 v_task_progress_4746 中的数据
}
