import apiClient from './apiClient';
import { Category } from '../types';

/**
 * List all categories for the current user
 */
export const listCategories = async (
  page?: number,
  size?: number,
  sort: string = 'name,asc'
): Promise<Category[]> => {
  if (page !== undefined && size !== undefined) {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sort', sort);
    
    const response = await apiClient.get<Category[]>(`/categories?${params.toString()}`);
    return response.data;
  }
  
  const response = await apiClient.get<Category[]>('/categories');
  return response.data;
};

/**
 * Get a single category by ID
 */
export const getCategory = async (id: number): Promise<Category> => {
  const response = await apiClient.get<Category>(`/categories/${id}`);
  return response.data;
};

/**
 * Create a new category
 */
export const createCategory = async (name: string): Promise<Category> => {
  const response = await apiClient.post<Category>('/categories', { name });
  return response.data;
};

/**
 * Update an existing category
 */
export const updateCategory = async (id: number, name: string): Promise<Category> => {
  const response = await apiClient.put<Category>(`/categories/${id}`, { name });
  return response.data;
};

/**
 * Delete a category
 */
export const deleteCategory = async (id: number): Promise<void> => {
  await apiClient.delete(`/categories/${id}`);
};

/**
 * Search categories by name
 */
export const searchCategories = async (
  query: string,
  page?: number,
  size?: number
): Promise<Category[]> => {
  const params = new URLSearchParams();
  params.append('q', query);
  
  if (page !== undefined) params.append('page', page.toString());
  if (size !== undefined) params.append('size', size.toString());
  
  const response = await apiClient.get<Category[]>(`/categories/search?${params.toString()}`);
  return response.data;
};
