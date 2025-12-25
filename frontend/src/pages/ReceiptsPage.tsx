import { useState, useEffect } from 'react';
import { Receipt } from '../types';
import { listUnassignedReceipts } from '../services';
import ReceiptUpload from '../components/ReceiptUpload';
import ReceiptGallery from '../components/ReceiptGallery';

export default function ReceiptsPage() {
  const [receipts, setReceipts] = useState<Receipt[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchReceipts();
  }, []);

  const fetchReceipts = async () => {
    setLoading(true);
    try {
      const data = await listUnassignedReceipts();
      setReceipts(data);
    } catch (error) {
      console.error('Failed to fetch receipts:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleUploadSuccess = (receipt: Receipt) => {
    setReceipts([receipt, ...receipts]);
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Receipts Manager</h1>
          <p className="mt-2 text-gray-600">
            Upload and manage your expense receipts
          </p>
        </div>

        {/* Upload Section */}
        <div className="mb-8">
          <ReceiptUpload onUploadSuccess={handleUploadSuccess} />
        </div>

        {/* Gallery Section */}
        {loading ? (
          <div className="bg-white rounded-lg shadow-md p-8 text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
            <p className="mt-4 text-gray-600">Loading receipts...</p>
          </div>
        ) : (
          <ReceiptGallery receipts={receipts} onReceiptsChange={fetchReceipts} />
        )}
      </div>
    </div>
  );
}
