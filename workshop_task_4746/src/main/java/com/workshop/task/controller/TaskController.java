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

    /**
     * 汇报生产进度 (插入执行记录)
     * 接口路由：POST /api/task/exec/add
     */
    @PostMapping("/exec/add")
    public Map<String, Object> addTaskExec(@RequestBody TaskExec4746 taskExec) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 插入执行记录
            int rows = taskExecMapper.insert(taskExec);

            if (rows > 0) {
                // 2. 同时更新主任务状态（可选：如果任务还没开始，自动变为执行中）
                Task4746 mainTask = taskService.getById(taskExec.getTaskId());
                if (mainTask != null && "未开始".equals(mainTask.getTaskStatus())) {
                    mainTask.setTaskStatus("执行中");
                    taskService.updateById(mainTask);
                }

                result.put("code", 200);
                result.put("message", "进度汇报成功");
            } else {
                result.put("code", 500);
                result.put("message", "汇报失败，数据库未更新");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "汇报异常: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 统计按期完成率及产量数据
     * 接口路由：GET /api/task/statistics
     */
    @GetMapping("/statistics")
    public Map<String, Object> getTaskStatistics(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 构建查询条件
            QueryWrapper<Task4746> query = new QueryWrapper<>();
            if (startTime != null && !startTime.isEmpty()) {
                query.ge("plan_end_time", startTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                query.le("plan_end_time", endTime);
            }

            List<Task4746> tasks = taskService.list(query);

            // 2. 统计数据
            int total = tasks.size();
            int onTimeCompleted = 0;

            // 新增：定义产量累加变量
            long totalPlanOutput = 0;   // 总计划产量
            long totalActualOutput = 0; // 总完成产量

            for (Task4746 t : tasks) {
                // 累加产量 (注意判空，防止空指针)
                if (t.getPlanOutput() != null) {
                    totalPlanOutput += t.getPlanOutput();
                }
                if (t.getActualOutput() != null) {
                    totalActualOutput += t.getActualOutput();
                }

                // 原有的按期完成判断逻辑
                if ("已完成".equals(t.getTaskStatus()) && t.getFinishTime() != null) {
                    if (!t.getFinishTime().after(t.getPlanEndTime())) {
                        onTimeCompleted++;
                    }
                }
            }

            // 3. 计算完成率
            double rate = total == 0 ? 0.0 : (double) onTimeCompleted / total * 100;

            Map<String, Object> data = new HashMap<>();
            data.put("total", total);
            data.put("onTime", onTimeCompleted);
            data.put("rate", String.format("%.2f", rate));

            // 新增：将产量数据放入返回结果
            data.put("totalPlanned", totalPlanOutput);
            data.put("totalCompleted", totalActualOutput);

            result.put("code", 200);
            result.put("message", "统计成功");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "统计失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取已有任务的产品列表
     */
    @GetMapping("/products")
    public Map<String, Object> getProductList() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> products = taskProgressMapper.selectDistinctProducts();
            result.put("code", 200);
            result.put("data", products);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 获取产量趋势数据
     * type: "day" 或 "week"
     */
    @GetMapping("/trend")
    public Map<String, Object> getOutputTrend(
            @RequestParam String productId,
            @RequestParam String type) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 根据类型决定数据库的时间格式化字符串
            String dateFormat = "day".equals(type) ? "YYYY-MM-DD" : "YYYY-IW"; // IW表示ISO周

            List<Map<String, Object>> trendData = taskProgressMapper.selectProductOutputTrend(productId, dateFormat);

            result.put("code", 200);
            result.put("data", trendData);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return result;
    }


    /**
     * DB4AI 预测产品未来批次平均产量（新增功能）
     * 接口路由：GET /api/task/predict/product/P005
     * @param productId 产品ID
     */
    @GetMapping("/predict/product/{productId}")
    public Map<String, Object> predictProductOutput(@PathVariable String productId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> prediction = taskProgressMapper.predictProductOutput(productId);

            result.put("code", 200);
            result.put("data", prediction);
            result.put("message", "DB4AI预测成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "预测失败: " + e.getMessage());
        }
        return result;
    }


}
