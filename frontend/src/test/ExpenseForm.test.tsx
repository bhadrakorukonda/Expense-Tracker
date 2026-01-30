import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import ExpenseForm from '../components/ExpenseForm';
import * as services from '../services';
import { mockCategories, mockCreatedExpense } from './mockData';

// Mock the services module
vi.mock('../services', () => ({
  listCategories: vi.fn(),
  createExpense: vi.fn(),
  uploadReceipt: vi.fn(),
}));

describe('ExpenseForm', () => {
  const mockOnSuccess = vi.fn();
  const mockOnCancel = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    // Default mock implementation
    vi.mocked(services.listCategories).mockResolvedValue(mockCategories);
  });

  describe('Component Rendering', () => {
    it('should render all form fields', async () => {
      render(<ExpenseForm />);

      // Wait for categories to load
      await waitFor(() => {
        expect(screen.getByLabelText(/amount/i)).toBeInTheDocument();
      });

      expect(screen.getByLabelText(/amount/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/currency/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/date/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/category/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/description/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/tags/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/receipt/i)).toBeInTheDocument();
    });

    it('should load and display categories', async () => {
      render(<ExpenseForm />);

      await waitFor(() => {
        expect(services.listCategories).toHaveBeenCalledTimes(1);
      });

      const categorySelect = screen.getByLabelText(/category/i) as HTMLSelectElement;
      
      await waitFor(() => {
        expect(categorySelect.options.length).toBeGreaterThan(1);
      });

      expect(screen.getByText('Food & Dining')).toBeInTheDocument();
      expect(screen.getByText('Transportation')).toBeInTheDocument();
      expect(screen.getByText('Entertainment')).toBeInTheDocument();
    });

    it('should show loading state for categories', async () => {
      vi.mocked(services.listCategories).mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve(mockCategories), 100))
      );

      render(<ExpenseForm />);

      expect(screen.getByText(/loading categories/i)).toBeInTheDocument();

      await waitFor(() => {
        expect(screen.queryByText(/loading categories/i)).not.toBeInTheDocument();
      });
    });

    it('should render submit button with correct text', () => {
      render(<ExpenseForm />);
      expect(screen.getByRole('button', { name: /create expense/i })).toBeInTheDocument();
    });

    it('should render cancel button when onCancel prop is provided', () => {
      render(<ExpenseForm onCancel={mockOnCancel} />);
      expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument();
    });
  });

  describe('Form Validation', () => {
    it('should show required error for amount when submitted empty', async () => {
      const user = userEvent.setup();
      render(<ExpenseForm />);

      const submitButton = screen.getByRole('button', { name: /create expense/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/amount is required/i)).toBeInTheDocument();
      });
    });

    it('should show error when amount is zero', async () => {
      const user = userEvent.setup();
      render(<ExpenseForm />);

      const amountInput = screen.getByLabelText(/amount/i);
      await user.clear(amountInput);
      await user.type(amountInput, '0');

      const submitButton = screen.getByRole('button', { name: /create expense/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/amount must be greater than 0/i)).toBeInTheDocument();
      });
    });

    it('should show error when amount is negative', async () => {
      const user = userEvent.setup();
      render(<ExpenseForm />);

      const amountInput = screen.getByLabelText(/amount/i);
      await user.clear(amountInput);
      await user.type(amountInput, '-10');

      const submitButton = screen.getByRole('button', { name: /create expense/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/amount must be greater than 0/i)).toBeInTheDocument();
      });
    });

    it('should show error when description exceeds 1000 characters', async () => {
      const user = userEvent.setup();
      render(<ExpenseForm />);

      const descriptionInput = screen.getByLabelText(/description/i);
      const longText = 'a'.repeat(1001);
      await user.type(descriptionInput, longText);

      const submitButton = screen.getByRole('button', { name: /create expense/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/description cannot exceed 1000 characters/i)).toBeInTheDocument();
      });
    });

    it('should show required error for date when submitted empty', async () => {
      const user = userEvent.setup();
      render(<ExpenseForm />);

      const dateInput = screen.getByLabelText(/date/i);
      await user.clear(dateInput);

      const submitButton = screen.getByRole('button', { name: /create expense/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/date is required/i)).toBeInTheDocument();
      });
    });
  });

  describe('Form Submission', () => {
    it('should submit form with valid data', async () => {
      const user = userEvent.setup();
      vi.mocked(services.createExpense).mockResolvedValue(mockCreatedExpense);

      render(<ExpenseForm onSuccess={mockOnSuccess} />);

      // Wait for categories to load
      await waitFor(() => {
        expect(services.listCategories).toHaveBeenCalled();
      });

      // Fill form
      const amountInput = screen.getByLabelText(/amount/i);
      await user.clear(amountInput);
      await user.type(amountInput, '45.50');

      const dateInput = screen.getByLabelText(/date/i);
      await user.clear(dateInput);
      await user.type(dateInput, '2026-01-25');

      const categorySelect = screen.getByLabelText(/category/i);
      await user.selectOptions(categorySelect, '1');

      const descriptionInput = screen.getByLabelText(/description/i);
      await user.type(descriptionInput, 'Lunch at restaurant');

      const tagsInput = screen.getByLabelText(/tags/i);
      await user.type(tagsInput, 'restaurant, lunch');

      // Submit form
      const submitButton = screen.getByRole('button', { name: /create expense/i });
      await user.click(submitButton);

      // Verify API call
      await waitFor(() => {
        expect(services.createExpense).toHaveBeenCalledWith(
          expect.objectContaining({
            amount: 45.50,
            currency: 'USD',
            date: '2026-01-25',
            categoryId: 1,
            description: 'Lunch at restaurant',
            tags: ['restaurant', 'lunch'],
          })
        );
      });

      // Verify success callback
      expect(mockOnSuccess).toHaveBeenCalledWith(mockCreatedExpense);
    });

    it('should parse tags correctly from comma-separated input', async () => {
      const user = userEvent.setup();
      vi.mocked(services.createExpense).mockResolvedValue(mockCreatedExpense);

      render(<ExpenseForm />);

      await waitFor(() => {
        expect(services.listCategories).toHaveBeenCalled();
      });

      const amountInput = screen.getByLabelText(/amount/i);
      await user.type(amountInput, '45.50');

      const tagsInput = screen.getByLabelText(/tags/i);
      await user.type(tagsInput, 'tag1, tag2,  tag3  , tag4');

      const submitButton = screen.getByRole('button', { name: /create expense/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(services.createExpense).toHaveBeenCalledWith(
          expect.objectContaining({
            tags: ['tag1', 'tag2', 'tag3', 'tag4'],
          })
        );
      });
    });

    it('should handle empty tags', async () => {
      const user = userEvent.setup();
      vi.mocked(services.createExpense).mockResolvedValue(mockCreatedExpense);

      render(<ExpenseForm />);

      await waitFor(() => {
        expect(services.listCategories).toHaveBeenCalled();
      });

      const amountInput = screen.getByLabelText(/amount/i);
      await user.type(amountInput, '45.50');

      const submitButton = screen.getByRole('button', { name: /create expense/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(services.createExpense).toHaveBeenCalledWith(
          expect.objectContaining({
            tags: undefined,
          })
        );
      });
    });

    it('should disable submit button during submission', async () => {
      const user = userEvent.setup();
      vi.mocked(services.createExpense).mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve(mockCreatedExpense), 100))
      );

      render(<ExpenseForm />);

      await waitFor(() => {
        expect(services.listCategories).toHaveBeenCalled();
      });

      const amountInput = screen.getByLabelText(/amount/i);
      await user.type(amountInput, '45.50');

      const submitButton = screen.getByRole('button', { name: /create expense/i });
      await user.click(submitButton);

      // Button should be disabled during submission
      expect(submitButton).toBeDisabled();
      expect(screen.getByText(/creating\.\.\./i)).toBeInTheDocument();

      // Wait for submission to complete
      await waitFor(() => {
        expect(submitButton).not.toBeDisabled();
      });
    });

    it('should reset form after successful submission', async () => {
      const user = userEvent.setup();
      vi.mocked(services.createExpense).mockResolvedValue(mockCreatedExpense);

      render(<ExpenseForm onSuccess={mockOnSuccess} />);

      await waitFor(() => {
        expect(services.listCategories).toHaveBeenCalled();
      });

      const amountInput = screen.getByLabelText(/amount/i) as HTMLInputElement;
      const descriptionInput = screen.getByLabelText(/description/i) as HTMLTextAreaElement;
      
      await user.type(amountInput, '45.50');
      await user.type(descriptionInput, 'Test description');

      const submitButton = screen.getByRole('button', { name: /create expense/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(mockOnSuccess).toHaveBeenCalled();
      });

      // Form should be reset
      await waitFor(() => {
        expect(amountInput.value).toBe('0');
        expect(descriptionInput.value).toBe('');
      });
    });
  });

  describe('Error Handling', () => {
    it('should display error message on API failure', async () => {
      const user = userEvent.setup();
      const errorMessage = 'Failed to create expense';
      vi.mocked(services.createExpense).mockRejectedValue(new Error(errorMessage));

      render(<ExpenseForm />);

      await waitFor(() => {
        expect(services.listCategories).toHaveBeenCalled();
      });

      const amountInput = screen.getByLabelText(/amount/i);
      await user.type(amountInput, '45.50');

      const submitButton = screen.getByRole('button', { name: /create expense/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/failed to create expense/i)).toBeInTheDocument();
      });
    });

    it('should display validation errors from backend', async () => {
      const user = userEvent.setup();
      const validationError = {
        response: {
          data: {
            validationErrors: [
              { field: 'amount', message: 'Amount must be positive' },
              { field: 'date', message: 'Date cannot be in the future' },
            ],
          },
        },
      };
      vi.mocked(services.createExpense).mockRejectedValue(validationError);

      render(<ExpenseForm />);

      await waitFor(() => {
        expect(services.listCategories).toHaveBeenCalled();
      });

      const amountInput = screen.getByLabelText(/amount/i);
      await user.type(amountInput, '45.50');

      const submitButton = screen.getByRole('button', { name: /create expense/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/validation failed/i)).toBeInTheDocument();
        expect(screen.getByText(/amount: Amount must be positive/i)).toBeInTheDocument();
      });
    });

    it('should handle receipt upload failure', async () => {
      const user = userEvent.setup();
      vi.mocked(services.uploadReceipt).mockRejectedValue(new Error('Upload failed'));
      vi.mocked(services.createExpense).mockResolvedValue(mockCreatedExpense);

      render(<ExpenseForm />);

      await waitFor(() => {
        expect(services.listCategories).toHaveBeenCalled();
      });

      const amountInput = screen.getByLabelText(/amount/i);
      await user.type(amountInput, '45.50');

      const file = new File(['receipt'], 'receipt.png', { type: 'image/png' });
      const fileInput = screen.getByLabelText(/receipt/i);
      await user.upload(fileInput, file);

      const submitButton = screen.getByRole('button', { name: /create expense/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/failed to upload receipt/i)).toBeInTheDocument();
      });

      // Expense should not be created
      expect(services.createExpense).not.toHaveBeenCalled();
    });
  });

  describe('Receipt Upload', () => {
    it('should upload receipt before creating expense', async () => {
      const user = userEvent.setup();
      const mockReceipt = { id: 'receipt-123' };
      vi.mocked(services.uploadReceipt).mockResolvedValue(mockReceipt as any);
      vi.mocked(services.createExpense).mockResolvedValue(mockCreatedExpense);

      render(<ExpenseForm />);

      await waitFor(() => {
        expect(services.listCategories).toHaveBeenCalled();
      });

      const amountInput = screen.getByLabelText(/amount/i);
      await user.type(amountInput, '45.50');

      const file = new File(['receipt'], 'receipt.png', { type: 'image/png' });
      const fileInput = screen.getByLabelText(/receipt/i);
      await user.upload(fileInput, file);

      const submitButton = screen.getByRole('button', { name: /create expense/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(services.uploadReceipt).toHaveBeenCalledWith(file);
      });

      await waitFor(() => {
        expect(services.createExpense).toHaveBeenCalledWith(
          expect.objectContaining({
            receiptMongoId: 'receipt-123',
          })
        );
      });
    });

    it('should show uploading receipt status', async () => {
      const user = userEvent.setup();
      vi.mocked(services.uploadReceipt).mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve({ id: 'receipt-123' } as any), 100))
      );
      vi.mocked(services.createExpense).mockResolvedValue(mockCreatedExpense);

      render(<ExpenseForm />);

      await waitFor(() => {
        expect(services.listCategories).toHaveBeenCalled();
      });

      const amountInput = screen.getByLabelText(/amount/i);
      await user.type(amountInput, '45.50');

      const file = new File(['receipt'], 'receipt.png', { type: 'image/png' });
      const fileInput = screen.getByLabelText(/receipt/i);
      await user.upload(fileInput, file);

      const submitButton = screen.getByRole('button', { name: /create expense/i });
      await user.click(submitButton);

      // Should show uploading status
      await waitFor(() => {
        expect(screen.getByText(/uploading receipt/i)).toBeInTheDocument();
      });

      // Wait for upload to complete
      await waitFor(() => {
        expect(screen.queryByText(/uploading receipt/i)).not.toBeInTheDocument();
      });
    });
  });

  describe('Cancel Button', () => {
    it('should call onCancel when cancel button is clicked', async () => {
      const user = userEvent.setup();
      render(<ExpenseForm onCancel={mockOnCancel} />);

      const cancelButton = screen.getByRole('button', { name: /cancel/i });
      await user.click(cancelButton);

      expect(mockOnCancel).toHaveBeenCalledTimes(1);
    });

    it('should disable cancel button during submission', async () => {
      const user = userEvent.setup();
      vi.mocked(services.createExpense).mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve(mockCreatedExpense), 100))
      );

      render(<ExpenseForm onCancel={mockOnCancel} />);

      await waitFor(() => {
        expect(services.listCategories).toHaveBeenCalled();
      });

      const amountInput = screen.getByLabelText(/amount/i);
      await user.type(amountInput, '45.50');

      const submitButton = screen.getByRole('button', { name: /create expense/i });
      const cancelButton = screen.getByRole('button', { name: /cancel/i });
      
      await user.click(submitButton);

      // Cancel button should be disabled during submission
      expect(cancelButton).toBeDisabled();

      await waitFor(() => {
        expect(cancelButton).not.toBeDisabled();
      });
    });
  });
});
