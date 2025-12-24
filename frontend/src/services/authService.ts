import apiClient from './apiClient';
import { LoginRequest, LoginResponse } from '../types';

/**
 * Authenticate user and get JWT token
 */
export const login = async (credentials: LoginRequest): Promise<LoginResponse> => {
  const response = await apiClient.post<LoginResponse>('/auth/login', credentials);
  
  // Store token and user info in localStorage
  localStorage.setItem('jwt_token', response.data.token);
  localStorage.setItem('user_info', JSON.stringify({
    userId: response.data.userId,
    email: response.data.email,
    name: response.data.name,
  }));
  
  return response.data;
};

/**
 * Logout user and clear stored data
 */
export const logout = (): void => {
  localStorage.removeItem('jwt_token');
  localStorage.removeItem('user_info');
};

/**
 * Check if user is authenticated
 */
export const isAuthenticated = (): boolean => {
  return !!localStorage.getItem('jwt_token');
};

/**
 * Get stored user info
 */
export const getUserInfo = (): { userId: number; email: string; name: string } | null => {
  const userInfo = localStorage.getItem('user_info');
  return userInfo ? JSON.parse(userInfo) : null;
};
