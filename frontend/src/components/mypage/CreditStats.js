import React from 'react';

const CreditStats = ({ current, charged, used, onExchange }) => (
  <div className="grid grid-cols-3 gap-4 text-sm relative">
    <div className="bg-purple-100 p-4 rounded shadow text-center relative">
      <p className="text-gray-500">보유 크레딧</p>
      <p className="font-bold text-lg">{current} 크레딧</p>
      <button
        onClick={onExchange}
        className="absolute right-3 top-7 text-xs bg-indigo-300 px-2 py-1 rounded"
      >
        환전 신청
      </button>
    </div>
    <div className="bg-purple-100 p-4 rounded shadow text-center">
      <p className="text-gray-500">총 충전</p>
      <p className="font-bold text-lg">{charged} 크레딧</p>
    </div>
    <div className="bg-purple-100 p-4 rounded shadow text-center">
      <p className="text-gray-500">이번 달 사용</p>
      <p className="font-bold text-lg">{used} 크레딧</p>
    </div>
  </div>
);

export default CreditStats;
