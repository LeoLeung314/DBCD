<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>📦 产品列表</span>
          <el-button type="primary" @click="dialogVisible = true">新增产品</el-button>
        </div>
      </template>
      
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="productId" label="产品编号" width="120" />
        <el-table-column prop="productName" label="产品名称" width="180" />
        <el-table-column prop="productType" label="类型" width="120" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="remark" label="备注" />
        <el-table-column prop="createTime" label="创建时间" />
      </el-table>
    </el-card>

    <!-- 新增产品弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增产品" width="30%">
      <el-form :model="form" label-width="80px">
        <el-form-item label="产品编号">
          <el-input v-model="form.productId" placeholder="例如: P001" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="form.productName" />
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="form.productType" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" placeholder="例如: 个/箱" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAdd">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import type { Product, ApiResponse } from '../types'
import { ref, onMounted } from 'vue'
import request from '../api/request'
import { ElMessage } from 'element-plus'

const tableData = ref<Product[]>([])
const dialogVisible = ref(false)
const form = ref<Product>({
    productId: '',
    productName: '',
    productType: '',
    unit: '',
    remark: ''
})

const loadData = async () => {
    const res = await request.get<ApiResponse<Product[]>>('/product/list')
    if(res.data.code === 200) {
        tableData.value = res.data.data
    }
}

const handleAdd = async () => {
    if(!form.value.productId || !form.value.productName) {
        ElMessage.warning('请填写必填项')
        return
    }
    const res = await request.post<ApiResponse<any>>('/product/add', form.value)
    if(res.data.code === 200) {
        ElMessage.success('添加成功')
        dialogVisible.value = false
        loadData()
        // 重置表单
        form.value = { productId: '', productName: '', productType: '', unit: '', remark: '' }
    } else {
        ElMessage.error(res.data.message)
    }
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 表格数据行文字改为亮白色 */
:deep(.el-table td.el-table__cell) {
  color: #fff !important;
}
</style>