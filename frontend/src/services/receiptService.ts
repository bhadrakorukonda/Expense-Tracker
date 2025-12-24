import apiClient from './apiClient';
import { Receipt } from '../types';

/**
 * Upload a receipt file
 */
export const uploadReceipt = async (
  file: File,
  notes?: string
): Promise<Receipt> => {
  const formData = new FormData();
  formData.append('file', file);
  if (notes) {
    formData.append('notes', notes);
  }
  
  const response = await apiClient.post<Receipt>('/receipts', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  
  return response.data;
};

/**
 * Download a receipt file
 */
export const downloadReceipt = async (id: string): Promise<Blob> => {
  const response = await apiClient.get(`/receipts/${id}`, {
    responseType: 'blob',
  });
  
  return response.data;
};

/**
 * Get receipt metadata
 */
export const getReceiptMetadata = async (id: string): Promise<Receipt> => {
  const response = await apiClient.get<Receipt>(`/receipts/${id}/metadata`);
  return response.data;
};

/**
 * Update receipt metadata
 */
export const updateReceiptMetadata = async (
  id: string,
  notes: string
): Promise<Receipt> => {
  const response = await apiClient.patch<Receipt>(`/receipts/${id}`, { notes });
  return response.data;
};

/**
 * Delete a receipt
 */
export const deleteReceipt = async (id: string): Promise<void> => {
  await apiClient.delete(`/receipts/${id}`);
};

/**
 * Link receipt to an expense
 */
export const linkReceiptToExpense = async (
  receiptId: string,
  expenseId: number
): Promise<void> => {
  await apiClient.post(`/receipts/${receiptId}/link/${expenseId}`);
};

/**
 * Unlink receipt from an expense
 */
export const unlinkReceiptFromExpense = async (receiptId: string): Promise<void> => {
  await apiClient.post(`/receipts/${receiptId}/unlink`);
};

/**
 * List unassigned receipts for the current user
 */
export const listUnassignedReceipts = async (): Promise<Receipt[]> => {
  const response = await apiClient.get<Receipt[]>('/receipts/unassigned');
  return response.data;
};

/**
 * Helper function to trigger download in browser
 */
export const downloadReceiptFile = async (id: string, fileName: string): Promise<void> => {
  const blob = await downloadReceipt(id);
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
};
