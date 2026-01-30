import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ExpensesList from '../pages/ExpensesList';
import * as services from '../services';

// Mock the services module
vi.mock('../services', () => ({
  listExpenses: vi.fn(),
  listCategories: vi.fn(),
  deleteExpense: vi.fn(),
}));

describe('ExpensesList Component', () => {
  const mockCategories = [
    { id: 1, name: 'Food & Dining', description: 'Food expenses', color: '#FF5733', icon: '🍔', userId: 1 },
    { id: 2, name: 'Transport', description: 'Transportation', color: '#3498DB', icon: '🚗', userId: 1 },
  ];

  const mockExpenses = [
    {
      id: 1,
      amount: 50.00,
      currency: 'USD',
      date: '2024-01-15',
      categoryId: 1,
      categoryName: 'Food & Dining',
      description: 'Lunch at restaurant',
      tags: ['restaurant', 'lunch'],
      receiptMongoId: 'receipt123',
    },
    {
      id: 2,
      amount: 25.00,
      currency: 'USD',
      date: '2024-01-14',
      categoryId: 2,
      categoryName: 'Transport',
      description: 'Taxi to office',
      tags: ['taxi'],
      receiptMongoId: null,
    },
    {
      id: 3,
      amount: 100.00,
      currency: 'EUR',
      date: '2024-01-13',
      categoryId: 1,
      categoryName: 'Food & Dining',
      description: 'Dinner with clients',
      tags: ['business', 'dinner'],
      receiptMongoId: 'receipt456',
    },
  ];

  const mockPaginatedResponse = {
    content: mockExpenses,
    totalPages: 2,
    totalElements: 13,
    size: 10,
    number: 0,
  };

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(services.listCategories).mockResolvedValue(mockCategories);
    vi.mocked(services.listExpenses).mockResolvedValue(mockPaginatedResponse);
    
    // Mock window.confirm
    global.confirm = vi.fn(() => true);
  });

  describe('Component Rendering', () => {
    it('should render expenses list with header', async () => {
      render(<ExpensesList />);

      expect(screen.getByText('Expenses')).toBeInTheDocument();
      expect(screen.getByText(/manage and track your expenses/i)).toBeInTheDocument();
    });

    it('should render filters section', async () => {
      render(<ExpensesList />);

      expect(screen.getByText('Filters')).toBeInTheDocument();
      expect(screen.getByPlaceholderText(/search description/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/from date/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/to date/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/category/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/currency/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /apply filters/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /clear filters/i })).toBeInTheDocument();
    });

    it('should load and display expenses', async () => {
      render(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      expect(screen.getByText('Lunch at restaurant')).toBeInTheDocument();
      expect(screen.getByText('Taxi to office')).toBeInTheDocument();
      expect(screen.getByText('Dinner with clients')).toBeInTheDocument();
    });

    it('should display loading state', async () => {
      vi.mocked(services.listExpenses).mockImplementation(
        () => new Promise(resolve => setTimeout(resolve, 1000))
      );

      render(<ExpensesList />);

      expect(screen.getByText(/loading expenses/i)).toBeInTheDocument();
    });

    it('should display empty state when no expenses', async () => {
      vi.mocked(services.listExpenses).mockResolvedValue({
        content: [],
        totalPages: 0,
        totalElements: 0,
        size: 10,
        number: 0,
      });

      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByText(/no expenses found/i)).toBeInTheDocument();
      });
    });

    it('should display error message on API failure', async () => {
      const errorMessage = 'Failed to load expenses';
      vi.mocked(services.listExpenses).mockRejectedValue({
        response: { data: { message: errorMessage } },
      });

      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByText(errorMessage)).toBeInTheDocument();
      });
    });
  });

  describe('Expense Display', () => {
    it('should format amounts correctly', async () => {
      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByText('$50.00')).toBeInTheDocument();
      });

      expect(screen.getByText('$25.00')).toBeInTheDocument();
      expect(screen.getByText('€100.00')).toBeInTheDocument();
    });

    it('should format dates correctly', async () => {
      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByText('Jan 15, 2024')).toBeInTheDocument();
      });

      expect(screen.getByText('Jan 14, 2024')).toBeInTheDocument();
      expect(screen.getByText('Jan 13, 2024')).toBeInTheDocument();
    });

    it('should display category names', async () => {
      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getAllByText('Food & Dining')).toHaveLength(2);
      });

      expect(screen.getByText('Transport')).toBeInTheDocument();
    });

    it('should display tags', async () => {
      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByText('restaurant')).toBeInTheDocument();
      });

      expect(screen.getByText('lunch')).toBeInTheDocument();
      expect(screen.getByText('taxi')).toBeInTheDocument();
      expect(screen.getByText('business')).toBeInTheDocument();
      expect(screen.getByText('dinner')).toBeInTheDocument();
    });

    it('should show receipt indicator when receipt exists', async () => {
      render(<ExpensesList />);

      await waitFor(() => {
        const receiptIcons = screen.getAllByTitle(/receipt available/i);
        expect(receiptIcons).toHaveLength(2); // First and third expenses have receipts
      });
    });

    it('should show no receipt indicator when receipt does not exist', async () => {
      render(<ExpensesList />);

      await waitFor(() => {
        const noReceiptIcons = screen.getAllByTitle(/no receipt/i);
        expect(noReceiptIcons).toHaveLength(1); // Second expense has no receipt
      });
    });
  });

  describe('Pagination', () => {
    it('should display pagination controls when multiple pages exist', async () => {
      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByText(/page 1 of 2/i)).toBeInTheDocument();
      });

      expect(screen.getByRole('button', { name: /previous/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /next/i })).toBeInTheDocument();
    });

    it('should disable previous button on first page', async () => {
      render(<ExpensesList />);

      await waitFor(() => {
        const previousButton = screen.getByRole('button', { name: /previous/i });
        expect(previousButton).toBeDisabled();
      });
    });

    it('should enable next button when not on last page', async () => {
      render(<ExpensesList />);

      await waitFor(() => {
        const nextButton = screen.getByRole('button', { name: /next/i });
        expect(nextButton).not.toBeDisabled();
      });
    });

    it('should navigate to next page', async () => {
      const user = userEvent.setup();
      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByText(/page 1 of 2/i)).toBeInTheDocument();
      });

      const nextButton = screen.getByRole('button', { name: /next/i });
      await user.click(nextButton);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledWith(
          expect.anything(),
          1, // page 1 (0-indexed)
          10,
          'date,desc'
        );
      });
    });

    it('should navigate to previous page', async () => {
      const user = userEvent.setup();
      vi.mocked(services.listExpenses).mockResolvedValue({
        ...mockPaginatedResponse,
        number: 1, // Start on page 2
      });

      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByText(/page 2 of 2/i)).toBeInTheDocument();
      });

      const previousButton = screen.getByRole('button', { name: /previous/i });
      await user.click(previousButton);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledWith(
          expect.anything(),
          0, // page 0
          10,
          'date,desc'
        );
      });
    });

    it('should change page size', async () => {
      const user = userEvent.setup();
      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByText(/rows per page/i)).toBeInTheDocument();
      });

      const pageSizeSelect = screen.getByDisplayValue('10');
      await user.selectOptions(pageSizeSelect, '20');

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledWith(
          expect.anything(),
          0, // Reset to page 0
          20, // New page size
          'date,desc'
        );
      });
    });

    it('should display correct results summary', async () => {
      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByText(/showing 1 - 10 of 13 expenses/i)).toBeInTheDocument();
      });
    });

    it('should not display pagination when only one page', async () => {
      vi.mocked(services.listExpenses).mockResolvedValue({
        content: mockExpenses,
        totalPages: 1,
        totalElements: 3,
        size: 10,
        number: 0,
      });

      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.queryByText(/page 1 of 1/i)).not.toBeInTheDocument();
      });
    });
  });

  describe('Filtering', () => {
    it('should apply search filter', async () => {
      const user = userEvent.setup();
      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByPlaceholderText(/search description/i)).toBeInTheDocument();
      });

      const searchInput = screen.getByPlaceholderText(/search description/i);
      await user.type(searchInput, 'lunch');

      const applyButton = screen.getByRole('button', { name: /apply filters/i });
      await user.click(applyButton);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledWith(
          expect.objectContaining({ q: 'lunch' }),
          0,
          10,
          'date,desc'
        );
      });
    });

    it('should apply date range filter', async () => {
      const user = userEvent.setup();
      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByLabelText(/from date/i)).toBeInTheDocument();
      });

      const fromDateInput = screen.getByLabelText(/from date/i);
      await user.type(fromDateInput, '2024-01-01');

      const toDateInput = screen.getByLabelText(/to date/i);
      await user.type(toDateInput, '2024-01-31');

      const applyButton = screen.getByRole('button', { name: /apply filters/i });
      await user.click(applyButton);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledWith(
          expect.objectContaining({
            fromDate: '2024-01-01',
            toDate: '2024-01-31',
          }),
          0,
          10,
          'date,desc'
        );
      });
    });

    it('should apply category filter', async () => {
      const user = userEvent.setup();
      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByLabelText(/category/i)).toBeInTheDocument();
      });

      const categorySelect = screen.getByLabelText(/category/i);
      await user.selectOptions(categorySelect, '1');

      const applyButton = screen.getByRole('button', { name: /apply filters/i });
      await user.click(applyButton);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledWith(
          expect.objectContaining({ categoryId: 1 }),
          0,
          10,
          'date,desc'
        );
      });
    });

    it('should apply amount range filter', async () => {
      const user = userEvent.setup();
      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByLabelText(/min amount/i)).toBeInTheDocument();
      });

      const minAmountInput = screen.getByLabelText(/min amount/i);
      await user.type(minAmountInput, '10');

      const maxAmountInput = screen.getByLabelText(/max amount/i);
      await user.type(maxAmountInput, '100');

      const applyButton = screen.getByRole('button', { name: /apply filters/i });
      await user.click(applyButton);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledWith(
          expect.objectContaining({
            minAmount: 10,
            maxAmount: 100,
          }),
          0,
          10,
          'date,desc'
        );
      });
    });

    it('should apply currency filter', async () => {
      const user = userEvent.setup();
      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByLabelText(/currency/i)).toBeInTheDocument();
      });

      const currencySelect = screen.getByLabelText(/currency/i);
      await user.selectOptions(currencySelect, 'EUR');

      const applyButton = screen.getByRole('button', { name: /apply filters/i });
      await user.click(applyButton);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledWith(
          expect.objectContaining({ currency: 'EUR' }),
          0,
          10,
          'date,desc'
        );
      });
    });

    it('should apply multiple filters together', async () => {
      const user = userEvent.setup();
      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByPlaceholderText(/search description/i)).toBeInTheDocument();
      });

      // Apply multiple filters
      const searchInput = screen.getByPlaceholderText(/search description/i);
      await user.type(searchInput, 'restaurant');

      const categorySelect = screen.getByLabelText(/category/i);
      await user.selectOptions(categorySelect, '1');

      const minAmountInput = screen.getByLabelText(/min amount/i);
      await user.type(minAmountInput, '20');

      const currencySelect = screen.getByLabelText(/currency/i);
      await user.selectOptions(currencySelect, 'USD');

      const applyButton = screen.getByRole('button', { name: /apply filters/i });
      await user.click(applyButton);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledWith(
          expect.objectContaining({
            q: 'restaurant',
            categoryId: 1,
            minAmount: 20,
            currency: 'USD',
          }),
          0,
          10,
          'date,desc'
        );
      });
    });

    it('should clear all filters', async () => {
      const user = userEvent.setup();
      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByPlaceholderText(/search description/i)).toBeInTheDocument();
      });

      // Set some filters
      const searchInput = screen.getByPlaceholderText(/search description/i);
      await user.type(searchInput, 'lunch');

      const categorySelect = screen.getByLabelText(/category/i);
      await user.selectOptions(categorySelect, '1');

      // Clear filters
      const clearButton = screen.getByRole('button', { name: /clear filters/i });
      await user.click(clearButton);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledWith(
          {}, // Empty filters
          0,
          10,
          'date,desc'
        );
      });

      // Inputs should be cleared
      expect((searchInput as HTMLInputElement).value).toBe('');
      expect((categorySelect as HTMLSelectElement).value).toBe('');
    });

    it('should reset to first page when filters change', async () => {
      const user = userEvent.setup();
      vi.mocked(services.listExpenses).mockResolvedValue({
        ...mockPaginatedResponse,
        number: 1, // Start on page 2
      });

      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByText(/page 2 of 2/i)).toBeInTheDocument();
      });

      // Apply a filter
      const searchInput = screen.getByPlaceholderText(/search description/i);
      await user.type(searchInput, 'lunch');

      const applyButton = screen.getByRole('button', { name: /apply filters/i });
      await user.click(applyButton);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledWith(
          expect.objectContaining({ q: 'lunch' }),
          0, // Reset to page 0
          10,
          'date,desc'
        );
      });
    });
  });

  describe('Expense Deletion', () => {
    it('should delete expense when confirmed', async () => {
      const user = userEvent.setup();
      vi.mocked(services.deleteExpense).mockResolvedValue(undefined);

      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByText('Lunch at restaurant')).toBeInTheDocument();
      });

      const deleteButtons = screen.getAllByRole('button', { name: /delete/i });
      await user.click(deleteButtons[0]);

      expect(global.confirm).toHaveBeenCalledWith('Are you sure you want to delete this expense?');

      await waitFor(() => {
        expect(services.deleteExpense).toHaveBeenCalledWith(1);
      });

      // Should refresh the list
      expect(services.listExpenses).toHaveBeenCalledTimes(2);
    });

    it('should not delete expense when cancelled', async () => {
      const user = userEvent.setup();
      global.confirm = vi.fn(() => false); // User clicks "Cancel"

      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByText('Lunch at restaurant')).toBeInTheDocument();
      });

      const deleteButtons = screen.getAllByRole('button', { name: /delete/i });
      await user.click(deleteButtons[0]);

      expect(global.confirm).toHaveBeenCalled();
      expect(services.deleteExpense).not.toHaveBeenCalled();
    });

    it('should handle delete error', async () => {
      const user = userEvent.setup();
      const errorMessage = 'Failed to delete expense';
      vi.mocked(services.deleteExpense).mockRejectedValue({
        response: { data: { message: errorMessage } },
      });

      // Mock window.alert
      global.alert = vi.fn();

      render(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByText('Lunch at restaurant')).toBeInTheDocument();
      });

      const deleteButtons = screen.getAllByRole('button', { name: /delete/i });
      await user.click(deleteButtons[0]);

      await waitFor(() => {
        expect(global.alert).toHaveBeenCalledWith(errorMessage);
      });
    });
  });

  describe('Categories Loading', () => {
    it('should load categories on mount', async () => {
      render(<ExpensesList />);

      await waitFor(() => {
        expect(services.listCategories).toHaveBeenCalled();
      });

      const categorySelect = screen.getByLabelText(/category/i);
      expect(screen.getByRole('option', { name: 'Food & Dining' })).toBeInTheDocument();
      expect(screen.getByRole('option', { name: 'Transport' })).toBeInTheDocument();
    });

    it('should handle categories loading error gracefully', async () => {
      vi.mocked(services.listCategories).mockRejectedValue(new Error('Failed to load'));

      render(<ExpensesList />);

      // Component should still render without categories
      await waitFor(() => {
        expect(screen.getByLabelText(/category/i)).toBeInTheDocument();
      });

      const categorySelect = screen.getByLabelText(/category/i);
      expect(screen.getByRole('option', { name: 'All Categories' })).toBeInTheDocument();
    });
  });
});
