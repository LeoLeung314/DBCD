package com.workshop.task.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@TableName("task_4746")
public class Task4746 {

    @TableId
    private String taskId;         // 任务编号
    private String orderId;        // 订单编号
    private String productId;      // 产品编号
    private Integer planOutput;    // 计划产量
    private Date planStartTime;    // 计划开始时间
    private Date planEndTime;      // 计划结束时间
    private String taskStatus;     // 任务状态（未开始、执行中、已完成、已取消）
    private Integer actualOutput;  // 实际完成产量
    private Date finishTime;       // 完成时间
    private Date createTime;       // 创建时间
}
