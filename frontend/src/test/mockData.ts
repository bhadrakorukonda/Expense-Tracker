import { Category, Expense, ExpenseCreateDto } from '../types';

export const mockCategories: Category[] = [
  {
    id: 1,
    name: 'Food & Dining',
    description: 'Food and dining expenses',
    color: '#FF5733',
    icon: '🍔',
  },
  {
    id: 2,
    name: 'Transportation',
    description: 'Transportation expenses',
    color: '#3498DB',
    icon: '🚗',
  },
  {
    id: 3,
    name: 'Entertainment',
    description: 'Entertainment expenses',
    color: '#9B59B6',
    icon: '🎬',
  },
];

export const mockExpenses: Expense[] = [
  {
    id: 1,
    amount: 45.50,
    currency: 'USD',
    date: '2026-01-25',
    description: 'Lunch at restaurant',
    categoryId: 1,
    categoryName: 'Food & Dining',
    tags: ['restaurant', 'lunch'],
    userId: 1,
    createdAt: '2026-01-25T12:00:00Z',
    updatedAt: '2026-01-25T12:00:00Z',
  },
  {
    id: 2,
    amount: 25.00,
    currency: 'USD',
    date: '2026-01-24',
    description: 'Taxi fare',
    categoryId: 2,
    categoryName: 'Transportation',
    tags: ['taxi'],
    userId: 1,
    createdAt: '2026-01-24T10:00:00Z',
    updatedAt: '2026-01-24T10:00:00Z',
  },
  {
    id: 3,
    amount: 15.99,
    currency: 'USD',
    date: '2026-01-23',
    description: 'Movie tickets',
    categoryId: 3,
    categoryName: 'Entertainment',
    tags: ['movie', 'weekend'],
    userId: 1,
    createdAt: '2026-01-23T18:00:00Z',
    updatedAt: '2026-01-23T18:00:00Z',
  },
];

export const mockPaginatedExpenses = {
  content: mockExpenses,
  totalElements: 3,
  totalPages: 1,
  size: 10,
  number: 0,
  first: true,
  last: true,
  empty: false,
};

export const mockExpenseCreateDto: ExpenseCreateDto = {
  amount: 45.50,
  currency: 'USD',
  date: '2026-01-25',
  description: 'Lunch at restaurant',
  categoryId: 1,
  tags: ['restaurant', 'lunch'],
};

export const mockCreatedExpense: Expense = {
  id: 4,
  amount: 45.50,
  currency: 'USD',
  date: '2026-01-25',
  description: 'Lunch at restaurant',
  categoryId: 1,
  categoryName: 'Food & Dining',
  tags: ['restaurant', 'lunch'],
  userId: 1,
  createdAt: '2026-01-25T12:00:00Z',
  updatedAt: '2026-01-25T12:00:00Z',
};
