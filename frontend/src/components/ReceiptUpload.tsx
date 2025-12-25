import { useState } from 'react';
import { Receipt } from '../types';
import { uploadReceipt } from '../services';

interface ReceiptUploadProps {
  onUploadSuccess?: (receipt: Receipt) => void;
}

export default function ReceiptUpload({ onUploadSuccess }: ReceiptUploadProps) {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [notes, setNotes] = useState('');
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedFile(file);
      setError(null);
      
      // Create preview for images
      if (file.type.startsWith('image/')) {
        const url = URL.createObjectURL(file);
        setPreviewUrl(url);
      } else {
        setPreviewUrl(null);
      }
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      setError('Please select a file');
      return;
    }

    setUploading(true);
    setError(null);

    try {
      const receipt = await uploadReceipt(selectedFile, notes);
      
      // Reset form
      setSelectedFile(null);
      setNotes('');
      setPreviewUrl(null);
      
      // Clear file input
      const fileInput = document.getElementById('receipt-file') as HTMLInputElement;
      if (fileInput) fileInput.value = '';
      
      if (onUploadSuccess) {
        onUploadSuccess(receipt);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to upload receipt');
    } finally {
      setUploading(false);
    }
  };

  const handleCancel = () => {
    setSelectedFile(null);
    setNotes('');
    setPreviewUrl(null);
    setError(null);
    
    const fileInput = document.getElementById('receipt-file') as HTMLInputElement;
    if (fileInput) fileInput.value = '';
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h2 className="text-xl font-semibold text-gray-900 mb-4">Upload Receipt</h2>

      {error && (
        <div className="mb-4 bg-red-50 border border-red-400 text-red-800 px-4 py-3 rounded">
          {error}
        </div>
      )}

      <div className="space-y-4">
        {/* File Input */}
        <div>
          <label htmlFor="receipt-file" className="block text-sm font-medium text-gray-700 mb-2">
            Select File <span className="text-red-500">*</span>
          </label>
          <input
            type="file"
            id="receipt-file"
            onChange={handleFileChange}
            accept="image/*,.pdf"
            disabled={uploading}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 file:mr-4 file:py-2 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100 disabled:opacity-50"
          />
          <p className="mt-1 text-sm text-gray-500">
            Supported formats: Images (JPEG, PNG, etc.) and PDF
          </p>
        </div>

        {/* Preview */}
        {previewUrl && (
          <div className="border border-gray-300 rounded-md p-4">
            <p className="text-sm font-medium text-gray-700 mb-2">Preview:</p>
            <img
              src={previewUrl}
              alt="Receipt preview"
              className="max-w-full h-auto max-h-64 rounded border border-gray-200"
            />
          </div>
        )}

        {selectedFile && !previewUrl && (
          <div className="border border-gray-300 rounded-md p-4">
            <div className="flex items-center gap-3">
              <svg className="w-10 h-10 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z" clipRule="evenodd" />
              </svg>
              <div>
                <p className="font-medium text-gray-900">{selectedFile.name}</p>
                <p className="text-sm text-gray-500">
                  {(selectedFile.size / 1024).toFixed(2)} KB
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Notes Input */}
        <div>
          <label htmlFor="receipt-notes" className="block text-sm font-medium text-gray-700 mb-2">
            Notes (Optional)
          </label>
          <textarea
            id="receipt-notes"
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            disabled={uploading}
            rows={3}
            placeholder="Add notes about this receipt..."
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
          />
        </div>

        {/* Action Buttons */}
        <div className="flex gap-3">
          <button
            onClick={handleUpload}
            disabled={!selectedFile || uploading}
            className="flex-1 bg-blue-600 text-white px-6 py-3 rounded-md font-medium hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {uploading ? 'Uploading...' : 'Upload Receipt'}
          </button>
          
          {selectedFile && (
            <button
              onClick={handleCancel}
              disabled={uploading}
              className="px-6 py-3 border border-gray-300 text-gray-700 rounded-md font-medium hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-gray-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              Cancel
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
