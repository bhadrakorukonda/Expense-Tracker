import { useState, useEffect } from 'react';
import { Expense, ExpenseFilters } from '../types';
import { listExpenses } from '../services';
import { format, parseISO, startOfMonth, endOfMonth, subMonths } from 'date-fns';
import {
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';

interface MonthlyData {
  month: string;
  amount: number;
}

interface CategoryData {
  name: string;
  value: number;
}

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8', '#82CA9D', '#FFC658', '#FF6B9D'];

export default function Dashboard() {
  const [loading, setLoading] = useState(false);
  const [allExpenses, setAllExpenses] = useState<Expense[]>([]);
  const [selectedMonth, setSelectedMonth] = useState(format(new Date(), 'yyyy-MM'));
  const [monthlyData, setMonthlyData] = useState<MonthlyData[]>([]);
  const [categoryData, setCategoryData] = useState<CategoryData[]>([]);
  const [topExpenses, setTopExpenses] = useState<Expense[]>([]);
  const [totalAmount, setTotalAmount] = useState(0);

  // Fetch all expenses for the last 12 months
  useEffect(() => {
    fetchExpenses();
  }, []);

  // Compute aggregations when expenses change
  useEffect(() => {
    if (allExpenses.length > 0) {
      computeMonthlyData();
      computeCategoryData();
      computeTopExpenses();
    }
  }, [allExpenses, selectedMonth]);

  const fetchExpenses = async () => {
    setLoading(true);
    try {
      // Fetch expenses from last 12 months
      const fromDate = format(subMonths(new Date(), 12), 'yyyy-MM-dd');
      const toDate = format(new Date(), 'yyyy-MM-dd');
      
      const filters: ExpenseFilters = {
        fromDate,
        toDate,
      };

      // Fetch all pages
      let allData: Expense[] = [];
      let page = 0;
      let hasMore = true;

      while (hasMore) {
        const response = await listExpenses(filters, page, 100, 'date,desc');
        allData = [...allData, ...response.content];
        hasMore = !response.last;
        page++;
      }

      setAllExpenses(allData);
    } catch (error) {
      console.error('Failed to fetch expenses:', error);
    } finally {
      setLoading(false);
    }
  };

  const computeMonthlyData = () => {
    const monthlyMap = new Map<string, number>();

    // Initialize last 12 months with 0
    for (let i = 11; i >= 0; i--) {
      const month = format(subMonths(new Date(), i), 'yyyy-MM');
      monthlyMap.set(month, 0);
    }

    // Aggregate expenses by month
    allExpenses.forEach((expense) => {
      const month = format(parseISO(expense.date), 'yyyy-MM');
      if (monthlyMap.has(month)) {
        monthlyMap.set(month, (monthlyMap.get(month) || 0) + expense.amount);
      }
    });

    // Convert to array for chart
    const data: MonthlyData[] = Array.from(monthlyMap.entries()).map(([month, amount]) => ({
      month: format(parseISO(`${month}-01`), 'MMM yyyy'),
      amount: Math.round(amount * 100) / 100,
    }));

    setMonthlyData(data);
  };

  const computeCategoryData = () => {
    const categoryMap = new Map<string, number>();
    let total = 0;

    // Filter expenses for selected month
    const monthStart = format(startOfMonth(parseISO(`${selectedMonth}-01`)), 'yyyy-MM-dd');
    const monthEnd = format(endOfMonth(parseISO(`${selectedMonth}-01`)), 'yyyy-MM-dd');

    allExpenses.forEach((expense) => {
      if (expense.date >= monthStart && expense.date <= monthEnd) {
        const category = expense.categoryName || 'Uncategorized';
        categoryMap.set(category, (categoryMap.get(category) || 0) + expense.amount);
        total += expense.amount;
      }
    });

    // Convert to array for chart
    const data: CategoryData[] = Array.from(categoryMap.entries())
      .map(([name, value]) => ({
        name,
        value: Math.round(value * 100) / 100,
      }))
      .sort((a, b) => b.value - a.value);

    setCategoryData(data);
    setTotalAmount(Math.round(total * 100) / 100);
  };

  const computeTopExpenses = () => {
    // Get top 5 expenses from all time
    const sorted = [...allExpenses]
      .sort((a, b) => b.amount - a.amount)
      .slice(0, 5);
    
    setTopExpenses(sorted);
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(value);
  };

  const formatDate = (dateString: string) => {
    try {
      return format(parseISO(dateString), 'MMM dd, yyyy');
    } catch {
      return dateString;
    }
  };

  // Custom tooltip for pie chart
  const renderPieTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      const data = payload[0];
      const percentage = totalAmount > 0 ? ((data.value / totalAmount) * 100).toFixed(1) : 0;
      return (
        <div className="bg-white p-3 border border-gray-300 rounded shadow-lg">
          <p className="font-semibold">{data.name}</p>
          <p className="text-sm text-gray-600">
            {formatCurrency(data.value)} ({percentage}%)
          </p>
        </div>
      );
    }
    return null;
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
          <p className="mt-2 text-gray-600">
            Overview of your expenses and spending patterns
          </p>
        </div>

        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow-md p-6">
            <h3 className="text-sm font-medium text-gray-500 uppercase">Total Expenses</h3>
            <p className="mt-2 text-3xl font-bold text-gray-900">
              {allExpenses.length}
            </p>
            <p className="mt-1 text-sm text-gray-500">Last 12 months</p>
          </div>

          <div className="bg-white rounded-lg shadow-md p-6">
            <h3 className="text-sm font-medium text-gray-500 uppercase">This Month</h3>
            <p className="mt-2 text-3xl font-bold text-gray-900">
              {formatCurrency(totalAmount)}
            </p>
            <p className="mt-1 text-sm text-gray-500">{format(parseISO(`${selectedMonth}-01`), 'MMMM yyyy')}</p>
          </div>

          <div className="bg-white rounded-lg shadow-md p-6">
            <h3 className="text-sm font-medium text-gray-500 uppercase">Average/Month</h3>
            <p className="mt-2 text-3xl font-bold text-gray-900">
              {formatCurrency(
                monthlyData.length > 0
                  ? monthlyData.reduce((sum, m) => sum + m.amount, 0) / monthlyData.length
                  : 0
              )}
            </p>
            <p className="mt-1 text-sm text-gray-500">Last 12 months</p>
          </div>
        </div>

        {/* Charts Row */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
          {/* Monthly Bar Chart */}
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">
              Monthly Expenses (Last 12 Months)
            </h2>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={monthlyData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis 
                  dataKey="month" 
                  tick={{ fontSize: 12 }}
                  angle={-45}
                  textAnchor="end"
                  height={80}
                />
                <YAxis 
                  tick={{ fontSize: 12 }}
                  tickFormatter={(value) => `$${value}`}
                />
                <Tooltip
                  formatter={(value: number) => formatCurrency(value)}
                  contentStyle={{ backgroundColor: 'white', border: '1px solid #ccc' }}
                />
                <Legend />
                <Bar dataKey="amount" fill="#3B82F6" name="Amount" />
              </BarChart>
            </ResponsiveContainer>
          </div>

          {/* Category Pie Chart */}
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-semibold text-gray-900">
                Expenses by Category
              </h2>
              <select
                value={selectedMonth}
                onChange={(e) => setSelectedMonth(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                {Array.from({ length: 12 }, (_, i) => {
                  const date = subMonths(new Date(), i);
                  const value = format(date, 'yyyy-MM');
                  const label = format(date, 'MMMM yyyy');
                  return (
                    <option key={value} value={value}>
                      {label}
                    </option>
                  );
                })}
              </select>
            </div>
            
            {categoryData.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={categoryData}
                    dataKey="value"
                    nameKey="name"
                    cx="50%"
                    cy="50%"
                    outerRadius={100}
                    label={(entry) => `${entry.name}: ${formatCurrency(entry.value)}`}
                    labelLine={false}
                  >
                    {categoryData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip content={renderPieTooltip} />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex items-center justify-center h-[300px] text-gray-500">
                No expenses for selected month
              </div>
            )}
          </div>
        </div>

        {/* Top 5 Expenses */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">
            Top 5 Largest Expenses
          </h2>
          
          {topExpenses.length > 0 ? (
            <div className="space-y-4">
              {topExpenses.map((expense, index) => (
                <div
                  key={expense.id}
                  className="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
                >
                  <div className="flex items-center gap-4">
                    <div className="flex-shrink-0 w-8 h-8 bg-blue-600 text-white rounded-full flex items-center justify-center font-bold">
                      {index + 1}
                    </div>
                    <div>
                      <p className="font-medium text-gray-900">
                        {expense.description || 'No description'}
                      </p>
                      <div className="flex items-center gap-3 mt-1">
                        <span className="text-sm text-gray-500">
                          {formatDate(expense.date)}
                        </span>
                        {expense.categoryName && (
                          <>
                            <span className="text-gray-300">•</span>
                            <span className="text-sm text-gray-600">
                              {expense.categoryName}
                            </span>
                          </>
                        )}
                        {expense.receiptMongoId && (
                          <>
                            <span className="text-gray-300">•</span>
                            <span className="text-sm text-green-600 flex items-center gap-1">
                              <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z" clipRule="evenodd" />
                              </svg>
                              Receipt
                            </span>
                          </>
                        )}
                      </div>
                      {expense.tags && expense.tags.length > 0 && (
                        <div className="flex flex-wrap gap-1 mt-2">
                          {expense.tags.map((tag, idx) => (
                            <span
                              key={idx}
                              className="px-2 py-0.5 text-xs bg-blue-100 text-blue-800 rounded"
                            >
                              {tag}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-xl font-bold text-gray-900">
                      {formatCurrency(expense.amount)}
                    </p>
                    <p className="text-sm text-gray-500">{expense.currency}</p>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-8 text-gray-500">
              No expenses found
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
