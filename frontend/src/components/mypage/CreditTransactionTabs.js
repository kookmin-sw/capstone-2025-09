import React, { useState } from 'react';

const CreditTransactionTabs = ({ charges = [], usages = [] }) => {
  const [tab, setTab] = useState('charge');

  const formatDate = (iso) => {
    const date = new Date(iso);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(
      date.getDate()
    ).padStart(2, '0')}`;
  };

  return (
    <div className="space-y-4 text-sm">
      <div className="flex space-x-4">
        <button
          onClick={() => setTab('charge')}
          className={`px-3 py-1 rounded ${tab === 'charge' ? 'bg-purple-200' : 'bg-gray-100'}`}
        >
          유입 내역
        </button>
        <button
          onClick={() => setTab('use')}
          className={`px-3 py-1 rounded ${tab === 'use' ? 'bg-purple-200' : 'bg-gray-100'}`}
        >
          지출 내역
        </button>
      </div>

      <ul className="text-xs space-y-2 overflow-y-auto max-h-[170px] pr-1">
        {(tab === 'charge' ? charges : usages).map((item, idx) => (
          <li key={idx} className="flex justify-between border-b pb-1">
            {tab === 'charge' ? (
              <>
                <span>{formatDate(item.date)}</span>
                <span>
                  {item.amountCredit} 크레딧 ({item.method})
                </span>
              </>
            ) : (
              <>
                <span>{formatDate(item.date)}</span>
                <span>
                  {item.usage} - {item.amountCredit} 크레딧
                </span>
              </>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default CreditTransactionTabs;
