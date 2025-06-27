<template>
  <el-dialog
    :title="transaction?.id ? '编辑交易' : '新增交易'"
    :model-value="visible"
    @update:modelValue="val => emits('update:visible', val)"
  >
    <el-form :model="form" label-width="120px">
      <el-form-item label="用户名"><el-input v-model="form.userName" /></el-form-item>
      <el-form-item label="账号"><el-input v-model="form.accountNumber" /></el-form-item>
      <el-form-item label="金额"><el-input-number v-model="form.amount" :min="0.01" /></el-form-item>
      <el-form-item label="币种">
        <el-select v-model="form.currency">
          <el-option label="CNY" value="CNY" />
          <el-option label="USD" value="USD" />
          <el-option label="EUR" value="EUR" />
        </el-select>
      </el-form-item>

      <el-form-item label="交易类型">
        <el-select v-model="form.type">
          <el-option label="存款" value="DEPOSIT" />
          <el-option label="取款" value="WITHDRAWAL" />
          <el-option label="转账" value="TRANSFER" />
          <el-option label="支付" value="PAYMENT" />
        </el-select>
      </el-form-item>

      <el-form-item label="交易渠道">
        <el-select v-model="form.channel">
          <el-option label="柜面" value="COUNTER" />
          <el-option label="ATM" value="ATM" />
          <el-option label="网银" value="ONLINE_BANK" />
          <el-option label="手机银行" value="MOBILE_APP" />
        </el-select>
      </el-form-item>

      <el-form-item label="状态">
        <el-select v-model="form.status">
          <el-option label="成功" value="SUCCESS" />
          <el-option label="失败" value="FAILED" />
          <el-option label="处理中" value="PENDING" />
        </el-select>
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="emits('update:visible', false)">取消</el-button>
      <el-button type="primary" @click="submit">提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue';
import { createTransaction, updateTransaction } from '@/api/transaction';
import { ElMessage } from 'element-plus';

const props = defineProps({
  transaction: Object,
  visible: Boolean
});
const emits = defineEmits(['close', 'update:visible']);

const form = ref({
  userName: '',
  accountNumber: '',
  amount: 0.01,
  currency: 'CNY',
  type: '',
  channel: '',
  status: 'SUCCESS'
});

// 监听传入的 transaction 并更新表单
watch(() => props.transaction, (val) => {
  if (val) {
    form.value = { ...val };
  } else {
    form.value = {
      userName: '',
      accountNumber: '',
      amount: 0.01,
      currency: 'CNY',
      type: '',
      channel: '',
      status: 'SUCCESS'
    };
  }
}, { immediate: true });

function submit() {
  const action = form.value.id
    ? updateTransaction(form.value.id, form.value)
    : createTransaction(form.value);

  action
    .then(res => {
      const result = res.data;
      if (result.code === 0) {
        ElMessage.success('保存成功');
        emits('update:visible', false);
        emits('close');
      } else {
        ElMessage.error(result.msg || '保存失败');
      }
    })
    .catch(err => {
      ElMessage.error(err.message || '网络异常');
    });
}
</script>

