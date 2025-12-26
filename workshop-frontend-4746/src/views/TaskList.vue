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
      <div class="action-area">
        <el-button type="primary" size="large" @click="openAddDialog">
          + 下达生产任务
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

        <el-table-column label="操作" fixed="right" width="220" align="right">
          <template #default="scope">
            <el-button-group>
               <el-button 
                size="small" 
                type="danger" 
                plain
                v-if="scope.row.taskStatus === '未开始'"
                @click="handleDelete(scope.row.taskId)">撤销</el-button>
            
              <el-button 
                  size="small" 
                  type="primary" 
                  v-if="scope.row.taskStatus !== '已完成'"
                  @click="handleComplete(scope.row.taskId)">一键完成</el-button>

               <el-button size="small" plain @click="viewDetails(scope.row.taskId)">详情</el-button>
            </el-button-group>
          </template>
        </el-table-column>
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


  </div>
</template>

<script setup lang="ts">
import type { TaskProgressVo, TaskForm, Product, ApiResponse, TaskPrediction } from '../types'
import { ref, onMounted, computed } from 'vue'
import request from '../api/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, Warning, CircleCheck } from '@element-plus/icons-vue' // 新增图标引入

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


</script>

<style scoped>
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
  color: #888;
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
  color: #888;
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
