import { useEffect, useState } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { ExpenseCreateDto, Category } from '../types';
import { createExpense, listCategories, uploadReceipt } from '../services';
import { format } from 'date-fns';

interface ExpenseFormProps {
  onSuccess?: (expense: any) => void;
  onCancel?: () => void;
}

interface ExpenseFormData {
  amount: number;
  currency: string;
  date: string;
  categoryId: number | null;
  description: string;
  tags: string;
  receiptFile?: FileList;
}

export default function ExpenseForm({ onSuccess, onCancel }: ExpenseFormProps) {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loadingCategories, setLoadingCategories] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [uploadingReceipt, setUploadingReceipt] = useState(false);

  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
    reset,
  } = useForm<ExpenseFormData>({
    defaultValues: {
      amount: 0,
      currency: 'USD',
      date: format(new Date(), 'yyyy-MM-dd'),
      categoryId: null,
      description: '',
      tags: '',
    },
  });

  // Load categories on mount
  useEffect(() => {
    const fetchCategories = async () => {
      setLoadingCategories(true);
      try {
        const data = await listCategories();
        setCategories(data);
      } catch (error) {
        console.error('Failed to load categories:', error);
      } finally {
        setLoadingCategories(false);
      }
    };

    fetchCategories();
  }, []);

  const onSubmit = async (data: ExpenseFormData) => {
    setSubmitting(true);
    setFormError(null);

    try {
      let receiptMongoId: string | undefined;

      // Upload receipt first if provided
      if (data.receiptFile && data.receiptFile.length > 0) {
        setUploadingReceipt(true);
        try {
          const receipt = await uploadReceipt(data.receiptFile[0]);
          receiptMongoId = receipt.id;
        } catch (error: any) {
          setFormError('Failed to upload receipt. Please try again.');
          setSubmitting(false);
          setUploadingReceipt(false);
          return;
        } finally {
          setUploadingReceipt(false);
        }
      }

      // Parse tags from comma-separated string
      const tagsArray = data.tags
        ? data.tags.split(',').map(tag => tag.trim()).filter(tag => tag.length > 0)
        : [];

      // Create expense DTO
      const expenseDto: ExpenseCreateDto = {
        amount: data.amount,
        currency: data.currency,
        date: data.date,
        categoryId: data.categoryId || undefined,
        description: data.description || undefined,
        tags: tagsArray.length > 0 ? tagsArray : undefined,
        receiptMongoId,
      };

      // Create expense
      const expense = await createExpense(expenseDto);

      // Reset form and notify success
      reset();
      if (onSuccess) {
        onSuccess(expense);
      }
    } catch (error: any) {
      // Handle validation errors from backend
      if (error.response?.data?.validationErrors) {
        const validationErrors = error.response.data.validationErrors
          .map((err: any) => `${err.field}: ${err.message}`)
          .join(', ');
        setFormError(`Validation failed: ${validationErrors}`);
      } else if (error.response?.data?.message) {
        setFormError(error.response.data.message);
      } else {
        setFormError('Failed to create expense. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      {/* Form-level error */}
      {formError && (
        <div className="bg-red-50 border border-red-400 text-red-800 px-4 py-3 rounded">
          {formError}
        </div>
      )}

      {/* Amount */}
      <div>
        <label htmlFor="amount" className="block text-sm font-medium text-gray-700 mb-2">
          Amount <span className="text-red-500">*</span>
        </label>
        <input
          type="number"
          step="0.01"
          id="amount"
          {...register('amount', {
            required: 'Amount is required',
            min: { value: 0.01, message: 'Amount must be greater than 0' },
            max: { value: 999999999.99, message: 'Amount is too large' },
          })}
          className={`w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
            errors.amount ? 'border-red-500' : 'border-gray-300'
          }`}
          placeholder="0.00"
        />
        {errors.amount && (
          <p className="mt-1 text-sm text-red-600">{errors.amount.message}</p>
        )}
      </div>

      {/* Currency */}
      <div>
        <label htmlFor="currency" className="block text-sm font-medium text-gray-700 mb-2">
          Currency <span className="text-red-500">*</span>
        </label>
        <select
          id="currency"
          {...register('currency', { required: 'Currency is required' })}
          className={`w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
            errors.currency ? 'border-red-500' : 'border-gray-300'
          }`}
        >
          <option value="USD">USD - US Dollar</option>
          <option value="EUR">EUR - Euro</option>
          <option value="GBP">GBP - British Pound</option>
          <option value="JPY">JPY - Japanese Yen</option>
          <option value="CAD">CAD - Canadian Dollar</option>
          <option value="AUD">AUD - Australian Dollar</option>
          <option value="INR">INR - Indian Rupee</option>
        </select>
        {errors.currency && (
          <p className="mt-1 text-sm text-red-600">{errors.currency.message}</p>
        )}
      </div>

      {/* Date */}
      <div>
        <label htmlFor="date" className="block text-sm font-medium text-gray-700 mb-2">
          Date <span className="text-red-500">*</span>
        </label>
        <input
          type="date"
          id="date"
          {...register('date', { required: 'Date is required' })}
          className={`w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
            errors.date ? 'border-red-500' : 'border-gray-300'
          }`}
        />
        {errors.date && (
          <p className="mt-1 text-sm text-red-600">{errors.date.message}</p>
        )}
      </div>

      {/* Category */}
      <div>
        <label htmlFor="categoryId" className="block text-sm font-medium text-gray-700 mb-2">
          Category
        </label>
        <select
          id="categoryId"
          {...register('categoryId', {
            setValueAs: (value) => (value === '' ? null : parseInt(value, 10)),
          })}
          disabled={loadingCategories}
          className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">-- Select Category --</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
        {loadingCategories && (
          <p className="mt-1 text-sm text-gray-500">Loading categories...</p>
        )}
      </div>

      {/* Description */}
      <div>
        <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-2">
          Description
        </label>
        <textarea
          id="description"
          {...register('description', {
            maxLength: { value: 1000, message: 'Description cannot exceed 1000 characters' },
          })}
          rows={3}
          className={`w-full px-3 py-2 border rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
            errors.description ? 'border-red-500' : 'border-gray-300'
          }`}
          placeholder="Enter expense description..."
        />
        {errors.description && (
          <p className="mt-1 text-sm text-red-600">{errors.description.message}</p>
        )}
      </div>

      {/* Tags */}
      <div>
        <label htmlFor="tags" className="block text-sm font-medium text-gray-700 mb-2">
          Tags
        </label>
        <input
          type="text"
          id="tags"
          {...register('tags')}
          className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          placeholder="business, travel, food (comma-separated)"
        />
        <p className="mt-1 text-sm text-gray-500">
          Enter tags separated by commas
        </p>
      </div>

      {/* Receipt Upload */}
      <div>
        <label htmlFor="receiptFile" className="block text-sm font-medium text-gray-700 mb-2">
          Receipt (Optional)
        </label>
        <input
          type="file"
          id="receiptFile"
          {...register('receiptFile')}
          accept="image/*,.pdf"
          className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
        />
        <p className="mt-1 text-sm text-gray-500">
          Upload a receipt image or PDF (optional)
        </p>
      </div>

      {/* Action Buttons */}
      <div className="flex gap-3 pt-4">
        <button
          type="submit"
          disabled={submitting || uploadingReceipt}
          className="flex-1 bg-blue-600 text-white px-6 py-3 rounded-md font-medium hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {uploadingReceipt
            ? 'Uploading receipt...'
            : submitting
            ? 'Creating...'
            : 'Create Expense'}
        </button>

        {onCancel && (
          <button
            type="button"
            onClick={onCancel}
            disabled={submitting || uploadingReceipt}
            className="px-6 py-3 border border-gray-300 text-gray-700 rounded-md font-medium hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-gray-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}
