import React from 'react';
import CreditStats from '../../components/mypage/CreditStats';
import CreditTransactionTabs from '../../components/mypage/CreditTransactionTabs';
import CreditExchangeList from '../../components/mypage/CreditExchangeList';

const MyPayments = () => {
  const currentCredit = 320;

  const handleExchange = () => {
    const won = currentCredit * 100;
    alert(
      `💸 ${currentCredit} 크레딧은 ${won.toLocaleString()}원으로 환전됩니다.`
    );
  };

  return (
    <div className="bg-white p-6 rounded-xl shadow space-y-6">
      <CreditStats
        current={currentCredit}
        charged={500}
        used={150}
        onExchange={handleExchange}
      />
      <CreditTransactionTabs />
      <CreditExchangeList />
      <div className="mt-6 text-sm">
        <h3 className="font-semibold mb-2">크레딧 충전하기</h3>
        <div className="flex items-center space-x-2">
          <select className="border px-2 py-1 rounded">
            <option>5,000원 (50 크레딧)</option>
            <option>10,000원 (100 크레딧)</option>
            <option>20,000원 (200 크레딧)</option>
          </select>
          <button className="bg-purple-600 hover:bg-purple-700 text-white px-3 py-1 rounded">
            결제하기
          </button>
        </div>
      </div>
    </div>
  );
};

export default MyPayments;
