import { createRouter, createWebHistory } from 'vue-router';
import TransactionList from '@/views/TransactionList.vue';

const routes = [
  { path: '/', component: TransactionList }
];

export default createRouter({
  history: createWebHistory(),
  routes
});