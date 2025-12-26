package com.workshop.task.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.workshop.task.entity.Task4746;
import com.workshop.task.entity.TaskExec4746;
import com.workshop.task.entity.vo.TaskPredictionVO;
import com.workshop.task.entity.vo.TaskProgressVo;
import com.workshop.task.mapper.TaskExecMapper;
import com.workshop.task.mapper.TaskProgressMapper;
import com.workshop.task.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/task")
@CrossOrigin(origins = "*",maxAge =  3600)
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskProgressMapper taskProgressMapper;

    @Autowired
    private TaskExecMapper taskExecMapper;

    /**
     * 获取所有任务进度列表
     * 接口路由：GET /api/task/list
     */
    @GetMapping("/list")
    public Map<String, Object> getTaskList() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<TaskProgressVo> taskList = taskProgressMapper.selectList(null);
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", taskList);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 新增任务
     * 接口路由：POST /api/task/add
     */
    @PostMapping("/add")
    public Map<String, Object> addTask(@RequestBody Task4746 task) {
        Map<String, Object> result = new HashMap<>();
        try {
            task.setActualOutput(0);
            if (task.getTaskStatus() == null) {
                task.setTaskStatus("未开始");
            }

            boolean success = taskService.save(task);
            if (success) {
                result.put("code", 200);
                result.put("message", "任务新增成功");
            } else {
                result.put("code", 500);
                result.put("message", "任务新增失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "新增失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 完成任务（调用存储过程）
     * 接口路由：POST /api/task/complete/{taskId}
     */
    @PostMapping("/complete/{taskId}")
    public Map<String, Object> completeTask(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            String response = taskService.completeTask(taskId);
            result.put("code", 200);
            result.put("message", response);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "操作失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 根据任务 ID 获取任务详情
     * 接口路由：GET /api/task/{taskId}
     */
    @GetMapping("/{taskId}")
    public Map<String, Object> getTask(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Task4746 task = taskService.getById(taskId);
            if (task != null) {
                result.put("code", 200);
                result.put("message", "查询成功");
                result.put("data", task);
            } else {
                result.put("code", 404);
                result.put("message", "任务不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新任务信息
     * 接口路由：PUT /api/task/update
     */
    @PutMapping("/update")
    public Map<String, Object> updateTask(@RequestBody Task4746 task) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = taskService.updateById(task);
            if (success) {
                result.put("code", 200);
                result.put("message", "任务更新成功");
            } else {
                result.put("code", 500);
                result.put("message", "任务更新失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除任务
     * 接口路由：DELETE /api/task/{taskId}
     */
    @DeleteMapping("/{taskId}")
    public Map<String, Object> deleteTask(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Task4746 task = taskService.getById(taskId);
            if (task != null && "未开始".equals(task.getTaskStatus())) {
                boolean success = taskService.removeById(taskId);
                if (success) {
                    result.put("code", 200);
                    result.put("message", "任务删除成功");
                } else {
                    result.put("code", 500);
                    result.put("message", "任务删除失败");
                }
            } else {
                result.put("code", 400);
                result.put("message", "只有未开始的任务才能删除");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * DB4AI 功能：预测任务是否达标
     * 接口路由：GET /api/task/predict/{taskId}
     */
    @GetMapping("/predict/{taskId}")
    public Map<String, Object> predictTask(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 先检查任务是否存在
            Task4746 task = taskService.getById(taskId);
            if (task == null) {
                result.put("code", 404);
                result.put("message", "任务不存在");
                return result;
            }

            // 2. 调用 DB4AI 预测
            TaskPredictionVO prediction = taskService.getTaskPrediction(taskId);

            if (prediction != null) {
                result.put("code", 200);
                result.put("message", "AI预测成功");
                result.put("data", prediction);
            } else {
                result.put("code", 500);
                result.put("message", "AI预测未返回数据，请检查数据库模型是否训练");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "预测服务异常: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 获取任务执行明细
     */
    @GetMapping("/exec/list/{taskId}")
    public Map<String, Object> getTaskExecList(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            QueryWrapper<TaskExec4746> query = new QueryWrapper<>();
            query.eq("task_id", taskId).orderByDesc("exec_time"); // 按时间倒序
            List<TaskExec4746> list = taskExecMapper.selectList(query);

            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", list);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }
}
