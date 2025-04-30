import React, { useState } from 'react';

const CreditTransactionTabs = () => {
  const [tab, setTab] = useState('charge');

  const charges = [
    { date: '2025-04-10', amount: 5000, credit: 50, method: '카카오페이' },
    { date: '2025-04-01', amount: 10000, credit: 100, method: '토스' },
  ];

  const usages = [
    { date: '2025-04-12', usage: '감성 보이스 구매', credit: 100 },
    { date: '2025-04-08', usage: '기능 사용 - 발표 AI', credit: 20 },
  ];

  return (
    <div className="space-y-4 text-sm">
      <div className="flex space-x-4">
        <button
          onClick={() => setTab('charge')}
          className={`px-3 py-1 rounded ${tab === 'charge' ? 'bg-purple-200' : 'bg-gray-100'}`}
        >
          충전 내역
        </button>
        <button
          onClick={() => setTab('use')}
          className={`px-3 py-1 rounded ${tab === 'use' ? 'bg-purple-200' : 'bg-gray-100'}`}
        >
          사용 내역
        </button>
      </div>

      <ul className="text-xs space-y-2">
        {(tab === 'charge' ? charges : usages).map((item, idx) => (
          <li key={idx} className="flex justify-between border-b pb-1">
            {tab === 'charge' ? (
              <>
                <span>{item.date}</span>
                <span>
                  {item.amount.toLocaleString()}원 ({item.credit} 크레딧,{' '}
                  {item.method})
                </span>
              </>
            ) : (
              <>
                <span>{item.date}</span>
                <span>
                  {item.usage} - {item.credit} 크레딧
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
