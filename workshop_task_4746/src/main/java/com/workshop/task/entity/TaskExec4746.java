package com.workshop.task.entity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("task_exec_4746")
public class TaskExec4746 {
    @TableId
    private Long execId;
    private String taskId;
    private Date execTime;
    private Integer quantityDone;
    private String operatorId;
    private String remark;
}
