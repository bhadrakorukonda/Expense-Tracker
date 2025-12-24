import { ExpenseForm } from '../components';

export default function CreateExpensePage() {
  const handleSuccess = (expense: any) => {
    console.log('Expense created successfully:', expense);
    alert(`Expense created successfully! Amount: ${expense.amount} ${expense.currency}`);
  };

  const handleCancel = () => {
    console.log('Form cancelled');
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4">
      <div className="max-w-2xl mx-auto">
        <div className="bg-white rounded-lg shadow-md p-6">
          <h1 className="text-2xl font-bold text-gray-900 mb-6">
            Create New Expense
          </h1>
          
          <ExpenseForm onSuccess={handleSuccess} onCancel={handleCancel} />
        </div>
      </div>
    </div>
  );
}
