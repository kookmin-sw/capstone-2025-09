import React, { useEffect, useState } from 'react';
import CreditTransactionTabs from '../../components/mypage/CreditTransactionTabs';
import useUserStore from '../../utils/userStore';
import axiosInstance from '../../utils/axiosInstance';

const MyPayments = () => {
  const { user } = useUserStore((state) => state);

  const [currentCredit, setCurrentCredit] = useState(0);
  const [selectedAmount, setSelectedAmount] = useState(5000);
  const [charged, setCharged] = useState(0);
  const [used, setUsed] = useState(0);
  const [charges, setCharges] = useState([]);
  const [usages, setUsages] = useState([]);

  useEffect(() => {
    if (!user?.id) return;

    const fetchAll = async () => {
      try {
        // 잔액
        const balanceRes = await axiosInstance.get(
          `credits/balance/${user.id}`
        );
        setCurrentCredit(balanceRes.data.balance);

        // 충전/사용 내역
        const historyRes = await axiosInstance.get(
          `credits/history/${user.id}`
        );
        const chargesData = historyRes.data.charges || [];
        const usagesData = historyRes.data.usages || [];

        // 총합 계산
        const totalCharged = chargesData.reduce(
          (sum, item) => sum + (item.amountCredit || 0),
          0
        );
        const totalUsed = usagesData.reduce(
          (sum, item) => sum + (item.amountCredit || 0),
          0
        );

        setCharged(totalCharged);
        setUsed(totalUsed);
        setCharges(chargesData);
        setUsages(usagesData);
      } catch (err) {
        console.error('크레딧 정보 조회 실패:', err);
      }
    };

    fetchAll();
  }, [user?.id]);

  const handlePayment = async () => {
    try {
      await axiosInstance.post('credits/charge', {
        userId: user.id,
        amount: selectedAmount,
      });
      alert(`${selectedAmount} 크레딧이 충전되었습니다.`);
      setCurrentCredit((prev) => prev + selectedAmount);
      setCharged((prev) => prev + selectedAmount);
    } catch (err) {
      alert('결제에 실패했습니다. 다시 시도해주세요.');
    }
  };

  return (
    <div className="bg-white p-6 rounded-xl   space-y-6">
      {/* 크레딧 정보 */}
      <div className="grid grid-cols-3 gap-4 text-sm relative">
        <div className="bg-purple-100 p-4 rounded text-center relative">
          <p className="text-gray-500">보유 크레딧</p>
          <p className="font-bold text-base">{currentCredit} 크레딧</p>
        </div>
        <div className="bg-purple-100 p-4 rounded text-center">
          <p className="text-gray-500">총 충전</p>
          <p className="font-bold text-base">{charged} 크레딧</p>
        </div>
        <div className="bg-purple-100 p-4 rounded text-center">
          <p className="text-gray-500">이번 달 사용</p>
          <p className="font-bold text-base">{used} 크레딧</p>
        </div>
      </div>

      {/* 충전/사용 내역 */}
      <CreditTransactionTabs charges={charges} usages={usages} />

      {/* 결제 영역 */}
      <div className="mt-6 text-sm">
        <h3 className="font-semibold mb-2">크레딧 충전하기</h3>
        <div className="flex items-center space-x-2">
          <select
            className="border px-2 py-1 rounded"
            value={selectedAmount}
            onChange={(e) => setSelectedAmount(Number(e.target.value))}
          >
            <option value={1000}>10,000원 (1000 크레딧)</option>
            <option value={2000}>20,000원 (2000 크레딧)</option>
            <option value={5000}>50,000원 (5000 크레딧)</option>
          </select>
          <button
            aria-label="결제하기"
            onClick={handlePayment}
            className="bg-purple-600 hover:bg-purple-700 text-white px-3 py-1 rounded"
          >
            결제하기
          </button>
        </div>
      </div>
    </div>
  );
};

export default MyPayments;
