import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import ExpensesList from './pages/ExpensesList';
import CreateExpensePage from './pages/CreateExpensePage';
import CategoriesPage from './pages/CategoriesPage';
import ReceiptsPage from './pages/ReceiptsPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        
        <Route
          path="/dashboard"
          element={
            <Layout>
              <Dashboard />
            </Layout>
          }
        />
        
        <Route
          path="/expenses"
          element={
            <Layout>
              <ExpensesList />
            </Layout>
          }
        />
        
        <Route
          path="/expenses/new"
          element={
            <Layout>
              <CreateExpensePage />
            </Layout>
          }
        />
        
        <Route
          path="/categories"
          element={
            <Layout>
              <CategoriesPage />
            </Layout>
          }
        />
        
        <Route
          path="/receipts"
          element={
            <Layout>
              <ReceiptsPage />
            </Layout>
          }
        />
        
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
