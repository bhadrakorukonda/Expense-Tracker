import apiClient from './apiClient';
import { Expense, ExpenseCreateDto, ExpenseUpdateDto, PaginatedResponse, ExpenseFilters } from '../types';

/**
 * List expenses with optional filters and pagination
 */
export const listExpenses = async (
  filters: ExpenseFilters = {},
  page: number = 0,
  size: number = 20,
  sort: string = 'date,desc'
): Promise<PaginatedResponse<Expense>> => {
  const params = new URLSearchParams();
  
  // Add pagination parameters
  params.append('page', page.toString());
  params.append('size', size.toString());
  params.append('sort', sort);
  
  // Add filter parameters
  if (filters.fromDate) params.append('fromDate', filters.fromDate);
  if (filters.toDate) params.append('toDate', filters.toDate);
  if (filters.categoryId) params.append('categoryId', filters.categoryId.toString());
  if (filters.minAmount !== undefined) params.append('minAmount', filters.minAmount.toString());
  if (filters.maxAmount !== undefined) params.append('maxAmount', filters.maxAmount.toString());
  if (filters.q) params.append('q', filters.q);
  if (filters.currency) params.append('currency', filters.currency);
  if (filters.tag) params.append('tag', filters.tag);
  
  const response = await apiClient.get<PaginatedResponse<Expense>>(`/expenses?${params.toString()}`);
  return response.data;
};

/**
 * Get a single expense by ID
 */
export const getExpense = async (id: number): Promise<Expense> => {
  const response = await apiClient.get<Expense>(`/expenses/${id}`);
  return response.data;
};

/**
 * Create a new expense
 */
export const createExpense = async (dto: ExpenseCreateDto): Promise<Expense> => {
  const response = await apiClient.post<Expense>('/expenses', dto);
  return response.data;
};

/**
 * Update an existing expense
 */
export const updateExpense = async (id: number, dto: ExpenseUpdateDto): Promise<Expense> => {
  const response = await apiClient.put<Expense>(`/expenses/${id}`, dto);
  return response.data;
};

/**
 * Delete an expense
 */
export const deleteExpense = async (id: number): Promise<void> => {
  await apiClient.delete(`/expenses/${id}`);
};

/**
 * Get total expenses with optional filters
 */
export const getTotalExpenses = async (filters: ExpenseFilters = {}): Promise<number> => {
  const params = new URLSearchParams();
  
  if (filters.fromDate) params.append('fromDate', filters.fromDate);
  if (filters.toDate) params.append('toDate', filters.toDate);
  if (filters.categoryId) params.append('categoryId', filters.categoryId.toString());
  if (filters.minAmount !== undefined) params.append('minAmount', filters.minAmount.toString());
  if (filters.maxAmount !== undefined) params.append('maxAmount', filters.maxAmount.toString());
  if (filters.currency) params.append('currency', filters.currency);
  if (filters.tag) params.append('tag', filters.tag);
  
  const response = await apiClient.get<number>(`/expenses/total?${params.toString()}`);
  return response.data;
};

/**
 * Get total expenses by category
 */
export const getTotalExpensesByCategory = async (
  categoryId: number,
  fromDate?: string,
  toDate?: string
): Promise<number> => {
  const params = new URLSearchParams();
  params.append('categoryId', categoryId.toString());
  
  if (fromDate) params.append('fromDate', fromDate);
  if (toDate) params.append('toDate', toDate);
  
  const response = await apiClient.get<number>(`/expenses/total-by-category?${params.toString()}`);
  return response.data;
};
