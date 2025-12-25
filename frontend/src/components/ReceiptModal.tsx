import { Receipt } from '../types';
import { format, parseISO } from 'date-fns';

interface ReceiptModalProps {
  receipt: Receipt;
  imageUrl: string | null;
  onClose: () => void;
  onDelete?: (id: string) => void;
}

export default function ReceiptModal({ receipt, imageUrl, onClose, onDelete }: ReceiptModalProps) {
  const handleDelete = () => {
    if (window.confirm('Are you sure you want to delete this receipt?')) {
      if (onDelete) {
        onDelete(receipt.id);
      }
      onClose();
    }
  };

  const formatDate = (dateString: string) => {
    try {
      return format(parseISO(dateString), 'MMM dd, yyyy HH:mm');
    } catch {
      return dateString;
    }
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(2)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto" onClick={onClose}>
      <div className="flex items-center justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:block sm:p-0">
        {/* Background overlay */}
        <div className="fixed inset-0 transition-opacity bg-gray-500 bg-opacity-75" />

        {/* Modal panel */}
        <div
          className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-4xl sm:w-full"
          onClick={(e) => e.stopPropagation()}
        >
          {/* Header */}
          <div className="bg-gray-50 px-6 py-4 border-b border-gray-200">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-gray-900">
                Receipt Details
              </h3>
              <button
                onClick={onClose}
                className="text-gray-400 hover:text-gray-600 focus:outline-none"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>

          {/* Content */}
          <div className="px-6 py-4">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* Image/File Display */}
              <div>
                {imageUrl ? (
                  receipt.mimeType.startsWith('image/') ? (
                    <img
                      src={imageUrl}
                      alt={receipt.fileName}
                      className="w-full h-auto rounded border border-gray-300"
                    />
                  ) : (
                    <div className="flex flex-col items-center justify-center h-64 bg-gray-100 rounded border border-gray-300">
                      <svg className="w-16 h-16 text-gray-400 mb-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clipRule="evenodd" />
                      </svg>
                      <p className="text-gray-600 font-medium">{receipt.fileName}</p>
                      <p className="text-sm text-gray-500 mt-1">PDF Document</p>
                      <a
                        href={imageUrl}
                        download={receipt.fileName}
                        className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                      >
                        Download PDF
                      </a>
                    </div>
                  )
                ) : (
                  <div className="flex items-center justify-center h-64 bg-gray-100 rounded">
                    <div className="text-center">
                      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
                      <p className="text-gray-600">Loading receipt...</p>
                    </div>
                  </div>
                )}
              </div>

              {/* Receipt Information */}
              <div className="space-y-4">
                <div>
                  <h4 className="text-sm font-medium text-gray-500 uppercase mb-2">File Information</h4>
                  <div className="space-y-2">
                    <div>
                      <span className="text-sm font-medium text-gray-700">File Name:</span>
                      <p className="text-sm text-gray-900 break-all">{receipt.fileName}</p>
                    </div>
                    <div>
                      <span className="text-sm font-medium text-gray-700">Type:</span>
                      <p className="text-sm text-gray-900">{receipt.mimeType}</p>
                    </div>
                    <div>
                      <span className="text-sm font-medium text-gray-700">Size:</span>
                      <p className="text-sm text-gray-900">{formatFileSize(receipt.fileSize)}</p>
                    </div>
                    <div>
                      <span className="text-sm font-medium text-gray-700">Uploaded:</span>
                      <p className="text-sm text-gray-900">{formatDate(receipt.createdAt)}</p>
                    </div>
                    {receipt.expenseId && (
                      <div>
                        <span className="text-sm font-medium text-gray-700">Linked to Expense:</span>
                        <p className="text-sm text-blue-600">ID #{receipt.expenseId}</p>
                      </div>
                    )}
                  </div>
                </div>

                {receipt.notes && (
                  <div>
                    <h4 className="text-sm font-medium text-gray-500 uppercase mb-2">Notes</h4>
                    <div className="bg-gray-50 rounded-md p-3">
                      <p className="text-sm text-gray-900 whitespace-pre-wrap">{receipt.notes}</p>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Footer */}
          <div className="bg-gray-50 px-6 py-4 border-t border-gray-200 flex justify-between">
            <button
              onClick={handleDelete}
              className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500"
            >
              Delete Receipt
            </button>
            <button
              onClick={onClose}
              className="px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-gray-500"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
