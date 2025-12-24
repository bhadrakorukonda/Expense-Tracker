import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      <div className="text-center">
        <h1 className="text-4xl font-bold text-gray-800 mb-4">
          Expense Tracker
        </h1>
        <p className="text-gray-600">
          Frontend setup complete with TypeScript, React, TailwindCSS, and API services
        </p>
      </div>
    </div>
  </React.StrictMode>
);
