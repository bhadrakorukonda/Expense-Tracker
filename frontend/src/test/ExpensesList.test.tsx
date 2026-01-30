import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import ExpensesList from '../pages/ExpensesList';
import * as services from '../services';
import { mockExpenses, mockCategories, mockPaginatedExpenses } from './mockData';

// Mock the services module
vi.mock('../services', () => ({
  listExpenses: vi.fn(),
  listCategories: vi.fn(),
  deleteExpense: vi.fn(),
}));

// Helper to render with Router
const renderWithRouter = (component: React.ReactElement) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe('ExpensesList', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Default mock implementations
    vi.mocked(services.listExpenses).mockResolvedValue(mockPaginatedExpenses);
    vi.mocked(services.listCategories).mockResolvedValue(mockCategories);
  });

  describe('Component Rendering', () => {
    it('should render expenses list page header', async () => {
      renderWithRouter(<ExpensesList />);

      expect(screen.getByRole('heading', { name: /expenses/i })).toBeInTheDocument();
      expect(screen.getByText(/manage and track your expenses/i)).toBeInTheDocument();
    });

    it('should render filters section', async () => {
      renderWithRouter(<ExpensesList />);

      expect(screen.getByRole('heading', { name: /filters/i })).toBeInTheDocument();
      expect(screen.getByLabelText(/search/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/from date/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/to date/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/category/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/currency/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/min amount/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/max amount/i)).toBeInTheDocument();
    });

    it('should load and display expenses', async () => {
      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      expect(screen.getByText('Lunch at restaurant')).toBeInTheDocument();
      expect(screen.getByText('Taxi fare')).toBeInTheDocument();
      expect(screen.getByText('Movie tickets')).toBeInTheDocument();
    });

    it('should load and display categories in filter', async () => {
      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listCategories).toHaveBeenCalled();
      });

      const categorySelect = screen.getByLabelText(/category/i);
      const options = within(categorySelect).getAllByRole('option');

      expect(options).toHaveLength(4); // "All Categories" + 3 categories
      expect(screen.getByRole('option', { name: /food & dining/i })).toBeInTheDocument();
      expect(screen.getByRole('option', { name: /transportation/i })).toBeInTheDocument();
      expect(screen.getByRole('option', { name: /entertainment/i })).toBeInTheDocument();
    });

    it('should display loading state', async () => {
      vi.mocked(services.listExpenses).mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve(mockPaginatedExpenses), 100))
      );

      renderWithRouter(<ExpensesList />);

      expect(screen.getByText(/loading expenses/i)).toBeInTheDocument();

      await waitFor(() => {
        expect(screen.queryByText(/loading expenses/i)).not.toBeInTheDocument();
      });
    });

    it('should display error message on fetch failure', async () => {
      const errorMessage = 'Failed to load expenses';
      vi.mocked(services.listExpenses).mockRejectedValue({
        response: { data: { message: errorMessage } },
      });

      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByText(errorMessage)).toBeInTheDocument();
      });
    });

    it('should display empty state when no expenses', async () => {
      vi.mocked(services.listExpenses).mockResolvedValue({
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 10,
        number: 0,
        first: true,
        last: true,
        empty: true,
      });

      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(screen.getByText(/no expenses found/i)).toBeInTheDocument();
      });
    });
  });

  describe('Filtering', () => {
    it('should apply search filter', async () => {
      const user = userEvent.setup();
      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      const searchInput = screen.getByLabelText(/search/i);
      await user.type(searchInput, 'restaurant');

      const applyButton = screen.getByRole('button', { name: /apply filters/i });
      await user.click(applyButton);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledWith(
          expect.objectContaining({ q: 'restaurant' }),
          0,
          10,
          'date,desc'
        );
      });
    });

    it('should apply date range filter', async () => {
      const user = userEvent.setup();
      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      const fromDateInput = screen.getByLabelText(/from date/i);
      const toDateInput = screen.getByLabelText(/to date/i);

      await user.type(fromDateInput, '2026-01-01');
      await user.type(toDateInput, '2026-01-31');

      const applyButton = screen.getByRole('button', { name: /apply filters/i });
      await user.click(applyButton);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledWith(
          expect.objectContaining({
            fromDate: '2026-01-01',
            toDate: '2026-01-31',
          }),
          0,
          10,
          'date,desc'
        );
      });
    });

    it('should apply category filter', async () => {
      const user = userEvent.setup();
      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
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
      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      const minAmountInput = screen.getByLabelText(/min amount/i);
      const maxAmountInput = screen.getByLabelText(/max amount/i);

      await user.type(minAmountInput, '10');
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
      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
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
      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      // Apply multiple filters
      await user.type(screen.getByLabelText(/search/i), 'lunch');
      await user.selectOptions(screen.getByLabelText(/category/i), '1');
      await user.type(screen.getByLabelText(/min amount/i), '20');
      await user.selectOptions(screen.getByLabelText(/currency/i), 'USD');

      const applyButton = screen.getByRole('button', { name: /apply filters/i });
      await user.click(applyButton);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledWith(
          expect.objectContaining({
            q: 'lunch',
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

    it('should reset to first page when filters change', async () => {
      const user = userEvent.setup();
      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      // Go to page 2 (if pagination exists)
      const nextPageButton = screen.queryByRole('button', { name: /next/i });
      if (nextPageButton && !nextPageButton.hasAttribute('disabled')) {
        await user.click(nextPageButton);
      }

      // Apply filter
      await user.type(screen.getByLabelText(/search/i), 'test');
      const applyButton = screen.getByRole('button', { name: /apply filters/i });
      await user.click(applyButton);

      // Should call with page 0
      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenLastCalledWith(
          expect.anything(),
          0, // page reset to 0
          10,
          'date,desc'
        );
      });
    });

    it('should clear all filters', async () => {
      const user = userEvent.setup();
      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      // Apply filters
      await user.type(screen.getByLabelText(/search/i), 'test');
      await user.type(screen.getByLabelText(/min amount/i), '10');

      const clearButton = screen.getByRole('button', { name: /clear filters/i });
      await user.click(clearButton);

      // Inputs should be cleared
      expect(screen.getByLabelText(/search/i)).toHaveValue('');
      expect(screen.getByLabelText(/min amount/i)).toHaveValue(null);

      // Should call API with empty filters
      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledWith(
          {},
          0,
          10,
          'date,desc'
        );
      });
    });
  });

  describe('Pagination', () => {
    it('should display pagination controls', async () => {
      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      expect(screen.getByText(/showing 1-3 of 3/i)).toBeInTheDocument();
    });

    it('should navigate to next page', async () => {
      const user = userEvent.setup();
      const multiPageData = {
        ...mockPaginatedExpenses,
        totalPages: 3,
        totalElements: 25,
        last: false,
      };
      vi.mocked(services.listExpenses).mockResolvedValue(multiPageData);

      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      const nextButton = screen.getByRole('button', { name: /next/i });
      expect(nextButton).not.toBeDisabled();

      await user.click(nextButton);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledWith(
          {},
          1, // page 1
          10,
          'date,desc'
        );
      });
    });

    it('should navigate to previous page', async () => {
      const user = userEvent.setup();
      const page2Data = {
        ...mockPaginatedExpenses,
        number: 1,
        totalPages: 3,
        first: false,
        last: false,
      };
      vi.mocked(services.listExpenses).mockResolvedValue(page2Data);

      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      const prevButton = screen.getByRole('button', { name: /previous/i });
      expect(prevButton).not.toBeDisabled();

      await user.click(prevButton);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledWith(
          {},
          0, // page 0
          10,
          'date,desc'
        );
      });
    });

    it('should disable previous button on first page', async () => {
      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      const prevButton = screen.getByRole('button', { name: /previous/i });
      expect(prevButton).toBeDisabled();
    });

    it('should disable next button on last page', async () => {
      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      const nextButton = screen.getByRole('button', { name: /next/i });
      expect(nextButton).toBeDisabled();
    });

    it('should change page size', async () => {
      const user = userEvent.setup();
      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      const pageSizeSelect = screen.getByLabelText(/items per page/i);
      await user.selectOptions(pageSizeSelect, '25');

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledWith(
          {},
          0,
          25, // new page size
          'date,desc'
        );
      });
    });
  });

  describe('Expense Display', () => {
    it('should format amounts correctly', async () => {
      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      // Check formatted amounts (depends on implementation)
      expect(screen.getByText(/\$45\.50/)).toBeInTheDocument();
      expect(screen.getByText(/\$25\.00/)).toBeInTheDocument();
      expect(screen.getByText(/\$15\.99/)).toBeInTheDocument();
    });

    it('should format dates correctly', async () => {
      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      // Check formatted dates
      expect(screen.getByText(/Jan 25, 2026/i)).toBeInTheDocument();
      expect(screen.getByText(/Jan 24, 2026/i)).toBeInTheDocument();
      expect(screen.getByText(/Jan 23, 2026/i)).toBeInTheDocument();
    });

    it('should display category names', async () => {
      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      expect(screen.getByText('Food & Dining')).toBeInTheDocument();
      expect(screen.getByText('Transportation')).toBeInTheDocument();
      expect(screen.getByText('Entertainment')).toBeInTheDocument();
    });

    it('should display tags', async () => {
      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      expect(screen.getByText('restaurant')).toBeInTheDocument();
      expect(screen.getByText('lunch')).toBeInTheDocument();
      expect(screen.getByText('taxi')).toBeInTheDocument();
      expect(screen.getByText('movie')).toBeInTheDocument();
    });
  });

  describe('Expense Deletion', () => {
    it('should delete expense with confirmation', async () => {
      const user = userEvent.setup();
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
      vi.mocked(services.deleteExpense).mockResolvedValue(undefined);

      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      const deleteButtons = screen.getAllByRole('button', { name: /delete/i });
      await user.click(deleteButtons[0]);

      expect(confirmSpy).toHaveBeenCalledWith(
        'Are you sure you want to delete this expense?'
      );
      expect(services.deleteExpense).toHaveBeenCalledWith(1);

      // Should refresh list
      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledTimes(2);
      });

      confirmSpy.mockRestore();
    });

    it('should not delete expense if confirmation is cancelled', async () => {
      const user = userEvent.setup();
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false);

      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      const deleteButtons = screen.getAllByRole('button', { name: /delete/i });
      await user.click(deleteButtons[0]);

      expect(confirmSpy).toHaveBeenCalled();
      expect(services.deleteExpense).not.toHaveBeenCalled();

      confirmSpy.mockRestore();
    });

    it('should show error message on delete failure', async () => {
      const user = userEvent.setup();
      const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
      const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {});
      
      const errorMessage = 'Failed to delete expense';
      vi.mocked(services.deleteExpense).mockRejectedValue({
        response: { data: { message: errorMessage } },
      });

      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalled();
      });

      const deleteButtons = screen.getAllByRole('button', { name: /delete/i });
      await user.click(deleteButtons[0]);

      await waitFor(() => {
        expect(alertSpy).toHaveBeenCalledWith(errorMessage);
      });

      confirmSpy.mockRestore();
      alertSpy.mockRestore();
    });
  });

  describe('Refetch Behavior', () => {
    it('should refetch expenses when page changes', async () => {
      const user = userEvent.setup();
      const multiPageData = {
        ...mockPaginatedExpenses,
        totalPages: 2,
        last: false,
      };
      vi.mocked(services.listExpenses).mockResolvedValue(multiPageData);

      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledTimes(1);
      });

      const nextButton = screen.getByRole('button', { name: /next/i });
      await user.click(nextButton);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledTimes(2);
      });
    });

    it('should refetch expenses when filters change', async () => {
      const user = userEvent.setup();
      renderWithRouter(<ExpensesList />);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledTimes(1);
      });

      await user.type(screen.getByLabelText(/search/i), 'test');
      const applyButton = screen.getByRole('button', { name: /apply filters/i });
      await user.click(applyButton);

      await waitFor(() => {
        expect(services.listExpenses).toHaveBeenCalledTimes(2);
      });
    });
  });
});
