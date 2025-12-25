import { useState, useEffect } from 'react';
import { Receipt } from '../types';
import { downloadReceipt, deleteReceipt } from '../services';
import ReceiptModal from './ReceiptModal';
import { format, parseISO } from 'date-fns';

interface ReceiptGalleryProps {
  receipts: Receipt[];
  onReceiptsChange?: () => void;
}

export default function ReceiptGallery({ receipts, onReceiptsChange }: ReceiptGalleryProps) {
  const [selectedReceipt, setSelectedReceipt] = useState<Receipt | null>(null);
  const [receiptImageUrl, setReceiptImageUrl] = useState<string | null>(null);
  const [thumbnails, setThumbnails] = useState<Map<string, string>>(new Map());
  const [loadingThumbnails, setLoadingThumbnails] = useState<Set<string>>(new Set());

  // Load thumbnails for image receipts
  useEffect(() => {
    receipts.forEach((receipt) => {
      if (receipt.mimeType.startsWith('image/') && !thumbnails.has(receipt.id) && !loadingThumbnails.has(receipt.id)) {
        loadThumbnail(receipt.id);
      }
    });
  }, [receipts]);

  const loadThumbnail = async (receiptId: string) => {
    setLoadingThumbnails((prev) => new Set(prev).add(receiptId));

    try {
      const blob = await downloadReceipt(receiptId);
      const url = URL.createObjectURL(blob);
      setThumbnails((prev) => new Map(prev).set(receiptId, url));
    } catch (error) {
      console.error('Failed to load thumbnail:', error);
    } finally {
      setLoadingThumbnails((prev) => {
        const newSet = new Set(prev);
        newSet.delete(receiptId);
        return newSet;
      });
    }
  };

  const handleReceiptClick = async (receipt: Receipt) => {
    setSelectedReceipt(receipt);
    setReceiptImageUrl(null);

    try {
      const blob = await downloadReceipt(receipt.id);
      const url = URL.createObjectURL(blob);
      setReceiptImageUrl(url);
    } catch (error) {
      console.error('Failed to load receipt:', error);
    }
  };

  const handleCloseModal = () => {
    if (receiptImageUrl) {
      URL.revokeObjectURL(receiptImageUrl);
    }
    setSelectedReceipt(null);
    setReceiptImageUrl(null);
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteReceipt(id);
      
      // Clean up thumbnail URL
      const thumbnailUrl = thumbnails.get(id);
      if (thumbnailUrl) {
        URL.revokeObjectURL(thumbnailUrl);
        setThumbnails((prev) => {
          const newMap = new Map(prev);
          newMap.delete(id);
          return newMap;
        });
      }
      
      if (onReceiptsChange) {
        onReceiptsChange();
      }
    } catch (error: any) {
      alert(error.response?.data?.message || 'Failed to delete receipt');
    }
  };

  const formatDate = (dateString: string) => {
    try {
      return format(parseISO(dateString), 'MMM dd, yyyy');
    } catch {
      return dateString;
    }
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(2)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  };

  if (receipts.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow-md p-8 text-center">
        <svg className="w-16 h-16 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <h3 className="text-lg font-medium text-gray-900 mb-2">No receipts yet</h3>
        <p className="text-gray-600">Upload your first receipt to get started</p>
      </div>
    );
  }

  return (
    <>
      <div className="bg-white rounded-lg shadow-md p-6">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">
          Receipts Gallery ({receipts.length})
        </h2>

        {/* Grid View */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {receipts.map((receipt) => (
            <div
              key={receipt.id}
              onClick={() => handleReceiptClick(receipt)}
              className="border border-gray-200 rounded-lg overflow-hidden hover:shadow-lg transition-shadow cursor-pointer group"
            >
              {/* Thumbnail/Icon */}
              <div className="h-48 bg-gray-100 flex items-center justify-center relative overflow-hidden">
                {receipt.mimeType.startsWith('image/') ? (
                  thumbnails.has(receipt.id) ? (
                    <img
                      src={thumbnails.get(receipt.id)}
                      alt={receipt.fileName}
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform"
                    />
                  ) : (
                    <div className="flex items-center justify-center">
                      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                    </div>
                  )
                ) : (
                  <div className="text-center">
                    <svg className="w-16 h-16 text-gray-400 mx-auto" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z" clipRule="evenodd" />
                    </svg>
                    <p className="text-xs text-gray-500 mt-2">PDF</p>
                  </div>
                )}
                
                {receipt.expenseId && (
                  <div className="absolute top-2 right-2 bg-green-500 text-white text-xs px-2 py-1 rounded">
                    Linked
                  </div>
                )}
              </div>

              {/* Info */}
              <div className="p-3">
                <p className="font-medium text-sm text-gray-900 truncate" title={receipt.fileName}>
                  {receipt.fileName}
                </p>
                <div className="flex items-center justify-between mt-2 text-xs text-gray-500">
                  <span>{formatDate(receipt.createdAt)}</span>
                  <span>{formatFileSize(receipt.fileSize)}</span>
                </div>
                {receipt.notes && (
                  <p className="mt-2 text-xs text-gray-600 line-clamp-2">
                    {receipt.notes}
                  </p>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Modal */}
      {selectedReceipt && (
        <ReceiptModal
          receipt={selectedReceipt}
          imageUrl={receiptImageUrl}
          onClose={handleCloseModal}
          onDelete={handleDelete}
        />
      )}
    </>
  );
}
