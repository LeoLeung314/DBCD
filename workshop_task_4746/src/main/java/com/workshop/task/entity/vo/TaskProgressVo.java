package com.workshop.task.entity.vo;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@TableName("v_task_progress_4746")
public class TaskProgressVo {

    @TableId
    private String taskId;              // 任务编号

    private String productName;         // 产品名称
    private Integer planOutput;         // 计划产量
    private Integer actualDone;         // 实际完成产量
    private BigDecimal progressPercentage; // 进度百分比
    private String taskStatus;          // 任务状态
    private Date planStartTime;         // 计划开始时间
    private Date planEndTime;           // 计划结束时间
    private Date finishTime;            // 完成时间

}
