<template>
  <div class="dashboard-container">
    <!-- 顶部状态栏：工业数据看板风格 -->
    <div class="status-bar">
      <div class="stat-item">
        <span class="label">执行中任务 (进行中)</span>
        <span class="value">{{ taskList.filter(t => t.taskStatus === '执行中').length }}</span>
      </div>
      <div class="stat-item">
        <span class="label">当前总累计产量</span>
        <span class="value">{{ totalOutput }}</span>
      </div>
  </div>

    <!-- 新增：按期完成率统计卡片 -->
    <!-- 功能操作与统计数据行 -->
<div style="display: flex; align-items: center; padding: 0 20px; margin-bottom: 30px;">
  
  <!-- 左侧：下达新任务按钮（移到这里） -->
  <div style="margin-right: 60px;">
    <el-button type="primary" size="large" @click="openAddDialog" class="glow-button">
      <el-icon style="margin-right: 8px"><Plus /></el-icon> 下达新任务
    </el-button>
  </div>

  <!-- 中间：统计数据 -->
  <div style="display: flex; gap: 60px; flex: 1;">
    
    <div class="stat-item" style="cursor: pointer;" @click="statsDialogVisible = true">
      <span class="label">本月按期完成率 <el-icon><ArrowRight /></el-icon></span>
      <span class="value" style="color: var(--theme-neon); font-size: 28px;">{{ statsData.rate }}%</span>
    </div>

    <div class="stat-item">
      <span class="label">区间总计划产量</span>
      <span class="value" style="font-size: 28px;">{{ statsData.totalPlanned }}</span>
    </div>

    <div class="stat-item">
      <span class="label">区间总完成产量</span>
      <span class="value" style="color: #409EFF; font-size: 28px;">{{ statsData.totalCompleted }}</span>
    </div>

  </div>

  <!-- 右侧：趋势分析按钮 -->
  <div>
    <el-button type="success" size="large" round @click="openTrendDialog">
      <el-icon style="margin-right: 5px"><TrendCharts /></el-icon>
      产量趋势分析
    </el-button>
  </div>

</div>


    <!-- 主表格 -->
    <el-card class="box-card" shadow="never">
      <el-table 
        :data="taskList" 
        style="width: 100%" 
        :default-sort="{ prop: 'progressPercentage', order: 'descending' }"
        row-key="taskId"
      >
        <!-- 数据库属性：任务编号 -->
        <el-table-column prop="taskId" label="任务编号" width="120" sortable>
          <template #default="scope">
            <span class="font-mono" style="font-weight:bold">{{ scope.row.taskId }}</span>
          </template>
        </el-table-column>

        <!-- 数据库属性：产品名称 -->
        <el-table-column prop="productName" label="产品名称" min-width="140" sortable />

        <!-- 数据库属性：任务进度 -->
        <el-table-column label="任务进度监控" min-width="280" sortable prop="progressPercentage">
          <template #default="scope">
            <div style="display:flex; align-items:center;">
              <span class="font-mono" style="width: 50px; text-align:right; margin-right:10px;">
                {{ scope.row.progressPercentage }}%
              </span>
              <el-progress 
                :percentage="scope.row.progressPercentage > 100 ? 100 : scope.row.progressPercentage" 
                :show-text="false"
                :stroke-width="12"
                :color="customColors"
                style="flex:1"
              />
            </div>
          </template>
        </el-table-column>

        <!-- 数据库属性：产量 (实/计) -->
        <el-table-column label="产量 (实完/计划)" width="200" align="center">
            <template #default="scope">
                <span class="font-mono" style="color: var(--theme-neon)">{{ scope.row.actualDone }}</span>
                <span style="margin: 0 8px; color: #555">/</span>
                <span class="font-mono">{{ scope.row.planOutput }}</span>
            </template>
        </el-table-column>

        <!-- 数据库属性：状态 -->
        <el-table-column prop="taskStatus" label="状态" width="120" sortable align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.taskStatus)" effect="dark" class="status-tag">
              {{ scope.row.taskStatus }}
            </el-tag>
          </template>
      </el-table-column>



        <!-- 数据库属性：计划时间 -->
        <el-table-column label="计划周期 (始-终)" width="220">
            <template #default="scope">
              <div class="time-cell">
                <span class="font-mono text-small">始: {{ formatDate(scope.row.planStartTime) }}</span>
                <span class="font-mono text-small">终: {{ formatDate(scope.row.planEndTime) }}</span>
              </div>
            </template>
        </el-table-column>

        <!-- 新增：AI 预测列 -->
        <el-table-column label="智能分析" width="140" align="center">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              :icon="MagicStick" 
              plain
              @click="handlePredict(scope.row.taskId)"
            >
              AI 预测
            </el-button>
          </template>
        </el-table-column>

                <!-- ================== 替换开始 ================== -->
        <el-table-column label="操作" fixed="right" width="160" align="center">
          <template #default="scope">
            
            <!-- 1. 首选快捷按钮 -->
            <el-button 
              v-if="scope.row.taskStatus === '执行中'"
              size="small" type="primary" link 
              @click="openReportDialog(scope.row)"
            >
              <el-icon><Timer /></el-icon> 汇报
            </el-button>
            <el-button 
              v-else-if="scope.row.taskStatus === '未开始'"
              size="small" type="warning" link 
              @click="handleEdit(scope.row)"
            >
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
           

            <!-- 2. 更多操作下拉菜单 -->
            <el-dropdown trigger="click" style="margin-left: 8px;">
              <span class="el-dropdown-link" style="cursor: pointer; color: var(--theme-neon); font-size: 12px; display: flex; align-items: center;">
                更多 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  
                  <!-- 只有“未开始”才能编辑 -->
                  <el-dropdown-item v-if="scope.row.taskStatus === '未开始'" @click="handleEdit(scope.row)">
                    <el-icon><Edit /></el-icon> 修改计划
                  </el-dropdown-item>

                  <!-- 只有“执行中”或“未开始”才能汇报 -->
                  <el-dropdown-item 
                      v-if="scope.row.taskStatus !== '已完成'" 
                      @click="openReportDialog(scope.row)"
                  >
                    <el-icon><Timer /></el-icon> 汇报进度
                  </el-dropdown-item>
                  
                  <!-- 只有未完成的才能点完成 -->
                  <el-dropdown-item 
                      v-if="scope.row.taskStatus !== '已完成'" 
                      @click="handleComplete(scope.row.taskId)"
                  >
                    <el-icon><Check /></el-icon> 一键完成
                  </el-dropdown-item>

                  <!-- 详情随时可以看 -->
                  <el-dropdown-item @click="viewDetails(scope.row.taskId)">
                    <el-icon><Document /></el-icon> 查看详情
                  </el-dropdown-item>

                  <!-- 只有“未开始”才能撤销 (红色警告线) -->
                  <el-dropdown-item 
                    v-if="scope.row.taskStatus === '未开始'" 
                    divided 
                    @click="handleDelete(scope.row.taskId)" 
                    style="color: #f56c6c;"
                  >
                    <el-icon><Delete /></el-icon> 撤销任务
                  </el-dropdown-item>
                  
                </el-dropdown-menu>
              </template>
            </el-dropdown>

          </template>
        </el-table-column>
        <!-- ================== 替换结束 ================== -->

      </el-table>
    </el-card>

    <!-- 弹窗：下达任务 -->
    <el-dialog v-model="dialogVisible" title="下达生产任务" width="40%" class="custom-dialog">
        <el-form :model="taskForm" label-width="100px">
            <el-form-item label="任务编号"><el-input v-model="taskForm.taskId" placeholder="请输入任务ID" /></el-form-item>
            <el-form-item label="订单编号"><el-input v-model="taskForm.orderId" placeholder="关联订单号" /></el-form-item>
            <el-form-item label="选择产品">
                <el-select v-model="taskForm.productId" style="width:100%" placeholder="请选择产品">
                    <el-option v-for="item in products" :key="item.productId" :label="item.productName" :value="item.productId" />
                </el-select>
            </el-form-item>
            <el-form-item label="计划产量"><el-input-number v-model="taskForm.planOutput" :min="1" style="width:100%"/></el-form-item>
            <el-form-item label="计划时间">
                <el-col :span="11"><el-date-picker v-model="taskForm.planStartTime" type="date" placeholder="开始日期" style="width: 100%" /></el-col>
                <el-col :span="2" class="text-center">-</el-col>
                <el-col :span="11"><el-date-picker v-model="taskForm.planEndTime" type="date" placeholder="结束日期" style="width: 100%" /></el-col>
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmit">确认下达</el-button>
        </template>
    </el-dialog>

     <!-- 新增：DB4AI 预测结果弹窗 -->
    <el-dialog
      v-model="predictionVisible"
      title="DB4AI 任务状态预测"
      width="400px"
      class="custom-dialog prediction-dialog"
      align-center
    >
      <div v-if="predictionResult" class="prediction-content">
        <div class="result-item">
          <span class="label">当前任务:</span>
          <span class="value font-mono">{{ predictionResult.taskId }}</span>
        </div>
        <div class="result-item">
          <span class="label">实际进度:</span>
          <span class="value font-mono">{{ predictionResult.currentProgress }}</span>
        </div>
        
        <div class="result-divider"></div>
        
        <div class="result-item highlight">
          <span class="label">AI 预测结果:</span>
          <span 
            class="value big-text"
            :class="{ 'text-danger': predictionResult.aiPrediction === '不达标', 'text-success': predictionResult.aiPrediction === '达标' }"
          >
            {{ predictionResult.aiPrediction }}
          </span>
        </div>
        
        <!-- 预警信息 -->
        <div v-if="predictionResult.warningMsg !== '正常'" class="warning-box">
          <el-icon style="margin-right: 5px; font-size: 16px;"><Warning /></el-icon>
          <span>{{ predictionResult.warningMsg }}</span>
        </div>
        <div v-else class="success-box">
             <el-icon style="margin-right: 5px; font-size: 16px;"><CircleCheck /></el-icon>
             <span>进度正常，请继续保持。</span>
        </div>
      </div>
    </el-dialog>

        <!-- 新增：任务执行详情弹窗 -->
    <el-dialog v-model="detailVisible" title="任务执行流水明细" width="60%" class="custom-dialog">
        <h3 style="color: var(--theme-neon); margin-bottom: 15px;">任务编号: {{ currentDetailTaskId }}</h3>
        
        <el-table :data="detailList" style="width: 100%" height="400">
            <el-table-column prop="execTime" label="执行时间" width="180">
              <template #default="scope">
                <span class="font-mono">{{ formatDateTime(scope.row.execTime) }}</span>
              </template>
            </el-table-column>

            <el-table-column prop="quantityDone" label="完成数量" align="center">
                <template #default="scope">
                    <span style="color: var(--theme-neon); font-weight: bold;">+{{ scope.row.quantityDone }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="operatorId" label="操作员" align="center" />
            <el-table-column prop="remark" label="备注" />
        </el-table>
        
        <template #footer>
            <el-button @click="detailVisible = false">关闭</el-button>
        </template>
    </el-dialog>
    
    <!-- ================== 修正后的编辑弹窗 ================== -->
    <el-dialog v-model="editDialogVisible" title="调整生产计划" width="30%" class="custom-dialog">
        <el-form :model="editForm" label-width="100px">
            <el-form-item label="任务编号">
                <el-input v-model="editForm.taskId" disabled />
            </el-form-item>
            <el-form-item label="计划产量">
                <el-input-number v-model="editForm.planOutput" :min="1" style="width:100%" />
            </el-form-item>
            <el-form-item label="计划结束">
                <el-date-picker 
                    v-model="editForm.planEndTime" 
                    type="datetime" 
                    placeholder="选择截止时间" 
                    value-format="YYYY-MM-DD HH:mm:ss"
                    style="width: 100%" 
                />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="editDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitUpdate">保存修改</el-button>
            </span>
        </template>
    </el-dialog>

        <!-- ================== 补全：汇报进度弹窗 (start) ================== -->
    <el-dialog v-model="reportDialogVisible" title="汇报生产进度" width="30%" class="custom-dialog">
        <el-form :model="reportForm" label-width="100px">
            <el-form-item label="任务编号">
                <el-input v-model="reportForm.taskId" disabled />
            </el-form-item>
            <el-form-item label="本次完成">
                <el-input-number v-model="reportForm.quantityDone" :min="1" style="width:100%" />
            </el-form-item>
            <el-form-item label="操作员ID">
                <el-input v-model="reportForm.operatorId" placeholder="请输入工号" />
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="reportDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitReport">提交汇报</el-button>
            </span>
        </template>
    </el-dialog>
    <!-- ================== 补全：汇报进度弹窗 (end) ================== -->

     <!-- 在最底部的 dialog 区域添加统计弹窗 -->
    <el-dialog v-model="statsDialogVisible" title="按期完成率统计" width="400px" class="custom-dialog">
        <el-form label-width="80px">
            <el-form-item label="统计范围">
                <el-date-picker
                    v-model="statsDateRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始"
                    end-placeholder="结束"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                    @change="fetchStatistics"
                />
            </el-form-item>
        </el-form>
        
        <div style="text-align: center; margin-top: 20px;">
            <el-row :gutter="20">
                <el-col :span="8">
                    <div class="text-small">总任务数</div>
                    <div class="big-text">{{ statsData.total }}</div>
                </el-col>
                <el-col :span="8">
                    <div class="text-small">按期完成</div>
                    <div class="big-text" style="color: var(--theme-neon)">{{ statsData.onTime }}</div>
                </el-col>
                <el-col :span="8">
                    <div class="text-small">完成率</div>
                    <div class="big-text">{{ statsData.rate }}%</div>
                </el-col>
            </el-row>
        </div>
    </el-dialog>

  <!-- 趋势分析弹窗 (ECharts + AI预测) -->
<el-dialog v-model="trendDialogVisible" title="产品产量趋势分析" width="900px" class="custom-dialog">
  
  <!-- 控制栏：产品选择 + 时间维度 -->
  <div style="margin-bottom: 20px; display: flex; gap: 20px; align-items: center;">
    
    <el-select v-model="trendQuery.productId" placeholder="请选择产品" style="width: 250px">
      <el-option
        v-for="item in trendProductList"
        :key="item.product_id"
        :label="item.product_name"
        :value="item.product_id"
      />
    </el-select>

    <el-radio-group v-model="trendQuery.type">
      <el-radio-button label="day">按天统计</el-radio-button>
      <el-radio-button label="week">按周统计</el-radio-button>
    </el-radio-group>

    <div style="flex: 1;"></div>

    <el-tag type="info">
      已选：{{ trendProductList.find(p => p.product_id === trendQuery.productId)?.product_name || '未选择' }}
    </el-tag>
  </div>

  <!-- ECharts 图表容器 -->
  <div ref="trendChartRef" style="width: 100%; height: 400px; background: #1f2833; border-radius: 8px; border: 1px solid #333;"></div>

  <!-- AI 预测区域 -->
  <div style="margin-top: 30px; padding: 20px; background: rgba(64, 158, 255, 0.1); border-radius: 8px; border: 1px solid #409EFF;">
    
    <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 15px;">
      <h3 style="margin: 0; color: #409EFF;">
        <el-icon style="margin-right: 5px; vertical-align: middle;"><MagicStick /></el-icon>
        DB4AI 智能预测
      </h3>
      <el-button type="primary" @click="fetchProductPrediction" :loading="predicting">
        {{ predicting ? '预测中...' : '预测未来批次产量' }}
      </el-button>
    </div>

    <!-- 预测结果展示（深色主题优化版） -->
<div v-if="productPrediction" style="margin-top: 15px;">
  
  <!-- 使用 Grid 布局 -->
  <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
    
    <!-- 产品名称（占满两列） -->
    <div style="grid-column: span 2; padding: 15px; background: rgba(31, 40, 51, 0.8); border: 1px solid #409EFF; border-radius: 6px;">
      <div style="color: #fff; font-size: 12px; margin-bottom: 8px;">产品名称</div>
      <div style="color: #fff; font-weight: bold; font-size: 18px;">{{ productPrediction.product_name }}</div>
    </div>

    <!-- 历史平均产量 -->
    <div style="padding: 15px; background: rgba(31, 40, 51, 0.8); border: 1px solid #333; border-radius: 6px;">
      <div style="color: #fff; font-size: 12px; margin-bottom: 8px;">历史平均产量</div>
      <div style="color: #fff; font-size: 20px; font-weight: bold;">{{ productPrediction.historical_avg }}</div>
    </div>

    <!-- AI预测产量（高亮显示） -->
    <div style="padding: 15px; background: rgba(64, 158, 255, 0.15); border: 2px solid #409EFF; border-radius: 6px; box-shadow: 0 0 15px rgba(64, 158, 255, 0.3);">
      <div style="color: #fff; font-size: 12px; margin-bottom: 8px;">AI预测产量</div>
      <div style="color: #409EFF; font-size: 24px; font-weight: bold; text-shadow: 0 0 10px rgba(64, 158, 255, 0.5);">
        {{ productPrediction.predicted_avg }}
      </div>
    </div>

    <!-- 置信度 -->
    <div style="padding: 15px; background: rgba(31, 40, 51, 0.8); border: 1px solid #333; border-radius: 6px;">
      <div style="color: #fff; font-size: 12px; margin-bottom: 8px;">置信度</div>
      <div style="margin-top: 5px;">
        <el-tag :type="getConfidenceType(productPrediction.confidence_level)" size="large" effect="dark">
          {{ productPrediction.confidence_level }}
        </el-tag>
      </div>
    </div>

    <!-- 生产建议（占满两列） -->
    <div style="grid-column: span 2; padding: 15px; background: rgba(197, 240, 21, 0.1); border: 1px solid var(--theme-neon); border-radius: 6px;">
      <div style="color: #fff; font-size: 12px; margin-bottom: 8px;">生产建议</div>
      <div style="color: var(--theme-neon); font-size: 15px; line-height: 1.6;">
        {{ productPrediction.recommendation }}
      </div>
    </div>

  </div>
</div>


    

  </div>

</el-dialog>

  




  </div>
</template>

<script setup lang="ts">
import type { TaskProgressVo, TaskForm, Product, ApiResponse, TaskPrediction } from '../types'
import { ref, onMounted, computed, nextTick, watch } from 'vue'
import request from '../api/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { 
  MagicStick, Warning, CircleCheck, ArrowDown, Edit, Timer, Delete, Check, Document,
  ArrowRight, TrendCharts, Plus  
} from '@element-plus/icons-vue'



interface TaskExec {
    execId: number;
    execTime: string;
    quantityDone: number;
    operatorId: string;
    remark: string;
}

// 新增响应式变量
const detailVisible = ref(false)
const detailList = ref<TaskExec[]>([])
const currentDetailTaskId = ref('')

// ================== 补全：汇报逻辑 (start) ==================

// 1. 变量定义
const reportDialogVisible = ref(false)
const reportForm = ref({
    taskId: '',
    quantityDone: 0,
    operatorId: 'OP001', // 默认工号，方便测试
    execTime: '' 
})

// 2. 打开弹窗函数 (修复报错的核心)
const openReportDialog = (row: any) => {
    reportForm.value.taskId = row.taskId
    reportForm.value.quantityDone = 0 // 重置为0
    reportDialogVisible.value = true
}

// 3. 提交汇报函数
const submitReport = async () => {
    if (reportForm.value.quantityDone <= 0) {
        ElMessage.warning('汇报数量必须大于0')
        return
    }
    try {
        // 注意：这里调用的接口是 /task/exec/add，确保后端有这个接口
        const res = await request.post('/task/exec/add', {
            ...reportForm.value,
            execTime: new Date() // 补一个当前时间
        })
        
        if (res.data.code === 200) {
            ElMessage.success('进度汇报成功')
            reportDialogVisible.value = false
            loadTasks() // 刷新列表，进度条会自动涨
        } else {
            ElMessage.error(res.data.message || '汇报失败')
        }
    } catch (error) {
        ElMessage.error('网络请求失败')
    }
}
// ================== 补全：汇报逻辑 (end) ==================



// ================== 新增的 编辑逻辑 ==================

// 1. 控制弹窗显示的开关
const editDialogVisible = ref(false)

// 2. 弹窗里的表单数据
const editForm = ref({
    taskId: '',
    planOutput: 0,
    planEndTime: ''
})

// 3. 点击列表上的【编辑】按钮时触发
const handleEdit = (row: any) => {
    // 把这一行的 taskID、产量、时间填入表单
    editForm.value.taskId = row.taskId
    editForm.value.planOutput = row.planOutput
    editForm.value.planEndTime = row.planEndTime
    // 打开弹窗
    editDialogVisible.value = true
}

// 4. 点击弹窗里的【保存调整】按钮时触发
const submitUpdate = async () => {
    try {
        // 发送请求给后端接口
        // 注意：这里的 URL 要和你后端的 Controller 对应
        const res = await request.put('/task/update', editForm.value)
        
        // 判断后端返回的状态码
        if (res.data.code === 200) {
            ElMessage.success('生产计划调整成功！')
            editDialogVisible.value = false // 关掉弹窗
            loadTasks() // ！！！刷新列表，让进度条变化！！！
        } else {
            ElMessage.error(res.data.message || '调整失败')
        }
    } catch (error) {
        console.error(error)
        ElMessage.error('请求发送失败，请检查网络')
    }
}


// 列表数据
const taskList = ref<TaskProgressVo[]>([])
// 产品数据
const products = ref<Product[]>([])

// 弹窗开关
const dialogVisible = ref(false)
const predictionVisible = ref(false)

// 表单数据：使用 TaskForm 类型
const taskForm = ref<TaskForm>({ 
    taskId: '', 
    orderId: '', 
    productId: '', 
    planOutput: 100, 
    planStartTime: '', 
    planEndTime: '' 
})

// 预测结果数据
const predictionResult = ref<TaskPrediction | null>(null)

// 进度条颜色
const customColors = [
  { color: '#f56c6c', percentage: 20 },
  { color: '#e6a23c', percentage: 60 },
  { color: '#c5f015', percentage: 100 }, 
]

const totalOutput = computed(() => {
    return taskList.value.reduce((acc, cur) => acc + (cur.actualDone || 0), 0)
})

const loadTasks = async () => {
    const res = await request.get<ApiResponse<TaskProgressVo[]>>('/task/list')
    if (res.data.code === 200) taskList.value = res.data.data
}
const loadProducts = async () => {
    const res = await request.get<ApiResponse<Product[]>>('/product/list')
    if (res.data.code === 200) products.value = res.data.data
}

// 新增：处理 AI 预测点击事件
const handlePredict = async (taskId: string) => {
    try {
        ElMessage.info('正在请求 openGauss DB4AI 进行预测...')
        const res = await request.get<ApiResponse<TaskPrediction>>(`/task/predict/${taskId}`)
        if (res.data.code === 200) {
            predictionResult.value = res.data.data
            predictionVisible.value = true
        } else {
            ElMessage.error(res.data.message || '预测失败')
        }
    } catch (error) {
        ElMessage.error('调用 AI 服务异常')
    }
}

const handleSubmit = async () => {
    if(!taskForm.value.taskId || !taskForm.value.productId) { ElMessage.warning('请填写完整信息'); return }
    const res = await request.post<ApiResponse<any>>('/task/add', taskForm.value)
    if(res.data.code === 200) { ElMessage.success('任务已下达'); dialogVisible.value = false; loadTasks() }
    else { ElMessage.error(res.data.message) }
}
const openAddDialog = () => { loadProducts(); dialogVisible.value = true }
const handleDelete = (taskId: string) => {
    ElMessageBox.confirm('确定要撤销该未开始的任务吗?', '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    .then(async () => {
        const res = await request.delete<ApiResponse<any>>(`/task/${taskId}`)
        if(res.data.code === 200) { ElMessage.success('已撤销'); loadTasks() }
    })
}
const handleComplete = async (taskId: string) => {
    const res = await request.post<ApiResponse<any>>(`/task/complete/${taskId}`)
    if(res.data.code === 200) { ElMessage.success('任务状态已更新'); loadTasks() }
}
const viewDetails = async (taskId: string) => {
    currentDetailTaskId.value = taskId
    ElMessage.info(`正在加载任务 ${taskId} 的详细数据...`)
    try {
        // 调用刚才写的后端接口
        const res = await request.get<ApiResponse<TaskExec[]>>(`/task/exec/list/${taskId}`)
        if (res.data.code === 200) {
            detailList.value = res.data.data
            detailVisible.value = true // 打开弹窗
        } else {
            ElMessage.error(res.data.message || '加载详情失败')
        }
    } catch (e) {
        ElMessage.error('网络请求异常')
    }
}
const getStatusType = (status: string) => {
    switch(status) { 
        case '已完成': return 'success'; 
        case '执行中': return 'warning'; 
        case '未开始': return 'info'; 
        case '超时未完成': return 'danger'; // 红色高亮，醒目！
        default: return ''; 
    }
}


const formatDateTime = (val: any) => {
    if (!val) return '-';
    const date = new Date(val);
    // 转成 "2025/12/8 20:00:00" 这种格式
    return date.toLocaleString('zh-CN', { hour12: false }); 
}

const formatDate = (dateStr: string) => {
    if(!dateStr) return '-'
    return new Date(dateStr).toLocaleDateString() 
}
onMounted(() => { loadTasks() })

// 1. 变量定义
const statsDialogVisible = ref(false)
const statsDateRange = ref([]) // [startTime, endTime]
const statsData = ref({
    total: 0,
    onTime: 0,
    rate: '0.00',
    totalPlanned: 0,   // 新增：总计划产量
    totalCompleted: 0  // 新增：总完成产量
})

// 新增：趋势分析相关变量
const trendDialogVisible = ref(false)
const trendChartRef = ref(null)
let myChart: any = null
const trendQuery = ref({
  productId: '',
  type: 'day'
})
const trendProductList = ref<any[]>([])

// ========== 🆕 新增：AI 预测相关变量 ==========
const predicting = ref(false)
const productPrediction = ref<any>(null)

// ========== 🆕 新增：AI 预测相关函数 ==========

// 获取产品 AI 预测
const fetchProductPrediction = async () => {
  if (!trendQuery.value.productId) {
    ElMessage.warning('请先选择产品')
    return
  }
  
  predicting.value = true
  try {
    const res = await request.get(`/task/predict/product/${trendQuery.value.productId}`)
    if (res.data.code === 200) {
      productPrediction.value = res.data.data
      ElMessage.success('AI预测完成')
    } else {
      ElMessage.error(res.data.message || '预测失败')
    }
  } catch (error) {
    ElMessage.error('网络异常')
  } finally {
    predicting.value = false
  }
}

// 置信度标签颜色
const getConfidenceType = (level: string) => {
  if (level === '高') return 'success'
  if (level === '中') return 'warning'
  return 'danger'
}

// 2. 调用统计接口
const fetchStatistics = async () => {
    try {
        // 先给默认空字符串
        let startTime = ''
        let endTime = ''

        // 这里的逻辑改严谨一点
        // 确保 statsDateRange.value 存在，且长度为2
        if (statsDateRange.value && Array.isArray(statsDateRange.value) && statsDateRange.value.length === 2) {
            // 强转成 string，因为 el-date-picker 选出来的一定是 string
            startTime = String(statsDateRange.value[0])
            endTime = String(statsDateRange.value[1])
        }
        
        // GET 请求传参
        const res = await request.get('/task/statistics', {
            params: { startTime, endTime }
        })
        
        if (res.data.code === 200) {
            statsData.value = res.data.data
        }
    } catch (e) {
        console.error(e)
    }
}


// 加载趋势产品列表
const loadTrendProducts = async () => {
  try {
    const res = await request.get('/task/products')
    if (res.data.code === 200) {
      trendProductList.value = res.data.data
      if (trendProductList.value && trendProductList.value.length > 0) {
        trendQuery.value.productId = trendProductList.value[0]['product_id']
      }
    }
  } catch (error) {
    console.error('加载产品列表失败', error)
  }
}

// 打开趋势弹窗
const openTrendDialog = () => {
  trendDialogVisible.value = true
  productPrediction.value = null 
  loadTrendProducts().then(() => {
     nextTick(() => {
        loadTrendData()
     })
  })
}

// 加载趋势数据
const loadTrendData = async () => {
  if (!trendQuery.value.productId) return
  
  try {
    const res = await request.get('/task/trend', {
      params: {
        productId: trendQuery.value.productId,
        type: trendQuery.value.type
      }
    })
    
    if (res.data.code === 200) {
      renderChart(res.data.data)
    }
  } catch (error) {
    console.error('加载趋势数据失败', error)
  }
}

// 渲染图表
const renderChart = (data: any[]) => {
  if (!trendChartRef.value) return
  
  if (!myChart) {
    myChart = echarts.init(trendChartRef.value)
  }

  const xData = data.map(item => item.time_point)
  const yData = data.map(item => item.total_output)

  const option = {
    title: { text: '产量变化趋势', left: 'center', textStyle: { color: '#fff' } },
    tooltip: { trigger: 'axis' },
    grid: { top: 40, right: 20, bottom: 30, left: 50 },
    xAxis: { 
      type: 'category', 
      data: xData,
      name: trendQuery.value.type === 'day' ? '日期' : '周次',
      axisLabel: { color: '#ccc' }
    },
    yAxis: { 
      type: 'value', 
      name: '产量',
      splitLine: { lineStyle: { color: '#333' } },
      axisLabel: { color: '#ccc' }
    },
    series: [{
      data: yData,
      type: 'line',
      smooth: true,
      itemStyle: { color: '#409EFF' },
      areaStyle: { opacity: 0.3 }
    }]
  }
  myChart.setOption(option)
}

// 监听条件变化
watch(() => [trendQuery.value.productId, trendQuery.value.type], () => {
  if (trendDialogVisible.value) {
    productPrediction.value = null
    loadTrendData()
  }
})



// 3. 在 onMounted 里也可以默认调一次（比如统计本月）
onMounted(() => {
    loadTasks()
    fetchStatistics() // 默认统计所有
})

</script>

<style scoped>
/* 表格数据行文字改为亮白色 */
:deep(.el-table td.el-table__cell) {
  color: #fff !important;
}


.dashboard-container {
  width: 100%;
}

.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: linear-gradient(90deg, #1f2833 0%, transparent 100%);
  padding: 20px;
  border-left: 5px solid var(--theme-neon);
}

.stat-item {
  display: flex;
  flex-direction: column;
  margin-right: 40px;
}
.stat-item .label {
  font-size: 14px;
  color: #fff;
  margin-bottom: 5px;
}
.stat-item .value {
  font-size: 32px;
  font-weight: bold;
  font-family: 'Roboto Mono', monospace;
  color: #fff;
}

.box-card {
  border-radius: 0; 
}

.status-tag {
  font-weight: bold;
  border-radius: 0; 
}

.time-cell {
  display: flex;
  flex-direction: column;
  font-size: 12px;
  color: #fff;
}

.text-small {
  font-size: 12px;
}
.text-center {
    text-align: center;
}

/* 新增样式：预测弹窗 */
.prediction-content {
  padding: 10px;
}
.result-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  align-items: center;
}
.result-divider {
  height: 1px;
  background-color: #333;
  margin: 15px 0;
}
.big-text {
  font-size: 24px;
  font-weight: 800;
}
.text-danger {
  color: #ff4d4f;
  text-shadow: 0 0 10px rgba(255, 77, 79, 0.5);
}
.text-success {
  color: var(--theme-neon);
  text-shadow: 0 0 10px rgba(197, 240, 21, 0.5);
}
.warning-box {
  margin-top: 20px;
  padding: 12px;
  background-color: rgba(255, 77, 79, 0.1);
  border: 1px solid #ff4d4f;
  color: #ff4d4f;
  display: flex;
  align-items: center;
  border-radius: 4px;
}
.success-box {
  margin-top: 20px;
  padding: 12px;
  background-color: rgba(197, 240, 21, 0.1);
  border: 1px solid var(--theme-neon);
  color: var(--theme-neon);
  display: flex;
  align-items: center;
  border-radius: 4px;
}

/* 强制覆盖 Element Plus 弹窗样式 */
:deep(.custom-dialog.el-dialog) {
    background-color: #1f2833; /* 深色背景 */
    border: 1px solid #333;
    box-shadow: 0 0 20px rgba(0,0,0,0.8);
}

:deep(.custom-dialog .el-dialog__title) {
    color: var(--theme-neon); /* 标题高亮 */
    font-weight: bold;
}

:deep(.custom-dialog .el-dialog__body) {
    color: #fff;
    padding: 20px;
}

/* 覆盖表格在弹窗里的样式 */
:deep(.custom-dialog .el-table) {
    background-color: transparent !important;
    --el-table-tr-bg-color: transparent;
    --el-table-header-bg-color: #0b0c10;
    color: #fff;
}

:deep(.custom-dialog .el-table th),
:deep(.custom-dialog .el-table tr),
:deep(.custom-dialog .el-table td) {
    background-color: transparent !important;
    border-bottom: 1px solid #333 !important;
}

/* 鼠标悬停高亮 */
:deep(.custom-dialog .el-table--enable-row-hover .el-table__body tr:hover > td) {
    background-color: rgba(197, 240, 21, 0.1) !important;
}


</style>