import request from './axios';

export function getTransactions(page = 1, size = 10) {
  return request.get('/transactions', { params: { page, size } });
}

export function createTransaction(data) {
  return request.post('/transactions', data);
}

export function updateTransaction(id, data) {
  return request.put(`/transactions/${id}`, data);
}

export function deleteTransaction(id) {
  return request.delete(`/transactions/${id}`);
}