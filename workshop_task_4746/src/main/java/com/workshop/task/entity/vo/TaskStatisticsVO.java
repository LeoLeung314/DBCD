package com.workshop.task.entity.vo;
import lombok.Data;
import java.util.Date;

@Data
public class TaskStatisticsVO {
    private String taskId;
    private Integer planOutput;
    private Date planEndTime;
    private Date finishTime;
    private String taskStatus;

    // 关键字段：截止时间前的累计产量
    private Long outputBeforeDeadline;
    // 关键字段：当前总产量
    private Long totalOutput;
}

