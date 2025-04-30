import React from 'react';

const CreditExchangeList = () => {
  const exchangeRequests = [
    { id: 1, date: '2025-04-01', amount: 50000, status: '진행중' },
    { id: 2, date: '2025-03-25', amount: 32000, status: '완료' },
  ];

  return (
    <div className="space-y-2 text-xs">
      <h3 className="font-semibold mb-2">환전 내역</h3>
      {exchangeRequests.map((r) => (
        <div key={r.id} className="flex justify-between border-b pb-1">
          <span>{r.date}</span>
          <span>
            {r.amount.toLocaleString()}원{' '}
            <span
              className={`ml-2 ${
                r.status === '진행중'
                  ? 'text-yellow-600'
                  : r.status === '완료'
                    ? 'text-green-600'
                    : 'text-red-600'
              }`}
            >
              ({r.status})
            </span>
          </span>
        </div>
      ))}
    </div>
  );
};

export default CreditExchangeList;
