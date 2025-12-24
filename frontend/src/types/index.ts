export interface User {
  id: number;
  email: string;
  name: string;
  createdAt: string;
  updatedAt: string;
}

export interface Category {
  id: number;
  name: string;
  userId: number;
  createdAt: string;
}

export interface Expense {
  id: number;
  userId: number;
  userName?: string;
  categoryId?: number;
  categoryName?: string;
  amount: number;
  currency: string;
  date: string;
  description?: string;
  receiptMongoId?: string;
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

export interface ExpenseCreateDto {
  categoryId?: number;
  amount: number;
  currency: string;
  date: string;
  description?: string;
  receiptMongoId?: string;
  tags?: string[];
}

export interface ExpenseUpdateDto {
  categoryId?: number;
  amount?: number;
  currency?: string;
  date?: string;
  description?: string;
  receiptMongoId?: string;
  tags?: string[];
}

export interface Receipt {
  id: string;
  userId: number;
  expenseId?: number;
  fileName: string;
  mimeType: string;
  fileSize: number;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  userId: number;
  email: string;
  name: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface ExpenseFilters {
  fromDate?: string;
  toDate?: string;
  categoryId?: number;
  minAmount?: number;
  maxAmount?: number;
  q?: string;
  currency?: string;
  tag?: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: ValidationError[];
}

export interface ValidationError {
  field: string;
  message: string;
}
