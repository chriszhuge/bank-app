<template>
  <el-card>
    <el-button type="primary" @click="openCreate">新增交易</el-button>
    <el-table :data="transactions" v-loading="loading">
      <el-table-column prop="userName" label="用户名" />
      <el-table-column prop="accountNumber" label="账号" />
      <el-table-column prop="type" label="类型">
        <template #default="scope">{{ typeLabel(scope.row.type) }}</template>
      </el-table-column>
      <el-table-column prop="channel" label="渠道">
        <template #default="scope">{{ channelLabel(scope.row.channel) }}</template>
      </el-table-column>
      <el-table-column prop="amount" label="金额" />
      <el-table-column prop="currency" label="币种" />
      <el-table-column prop="status" label="状态">
        <template #default="scope">{{ statusLabel(scope.row.status) }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间">
        <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="修改时间">
        <template #default="scope">{{ formatDate(scope.row.updatedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button @click="edit(scope.row)">编辑</el-button>
          <el-button type="danger" @click="remove(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      background
      :total="total"
      v-model:current-page="page"
      v-model:page-size="size"
      @current-change="load"
    />

    <!-- ✅ 使用 v-model:visible 控制弹窗显示 -->
    <TransactionForm
      v-if="formVisible"
      v-model:visible="formVisible"
      :transaction="current"
      @close="onFormClose"
    />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getTransactions, deleteTransaction } from '@/api/transaction';
import TransactionForm from './TransactionForm.vue';

const transactions = ref([]);
const page = ref(1);
const size = ref(10);
const total = ref(100);
const loading = ref(false);
const formVisible = ref(false);
const current = ref(null);

function load() {
  loading.value = true;
  getTransactions(page.value, size.value).then(res => {
    transactions.value = res.data.data || [];
    total.value = res.data.total || 0;
    loading.value = false;
  });
}

function remove(id) {
  deleteTransaction(id).then(() => load());
}

function edit(row) {
  current.value = row;
  formVisible.value = true;
}

function openCreate() {
  current.value = null;
  formVisible.value = true;
}

function onFormClose() {
  load();
}

function typeLabel(val) {
  return {
    DEPOSIT: '存款',
    WITHDRAWAL: '取款',
    TRANSFER: '转账',
    PAYMENT: '支付'
  }[val] || val;
}

function channelLabel(val) {
  return {
    COUNTER: '柜面',
    ATM: 'ATM',
    ONLINE_BANK: '网银',
    MOBILE_APP: '手机银行'
  }[val] || val;
}

function statusLabel(val) {
  return {
    SUCCESS: '成功',
    FAILED: '失败',
    PENDING: '处理中'
  }[val] || val;
}

function formatDate(val) {
  return val ? new Date(val).toLocaleString() : '';
}

onMounted(() => load());
</script>

