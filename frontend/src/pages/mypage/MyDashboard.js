import React from 'react';
import { Line } from 'react-chartjs-2';
import Section from '../../components/mypage/Section';

const MyDashboard = ({
  user,
  earningsChart,
  recentCreated,
  recentBought,
  recentSales,
  recentPayments,
}) => {
  return (
    <div className="max-w-full overflow-hidden grid grid-cols-1 lg:grid-cols-3 gap-4">
      <div className="lg:col-span-1 flex flex-col gap-4">
        {/* 유저 프로필 정보 */}
        <div className="bg-white p-4 rounded-xl shadow flex flex-col items-center text-center">
          <img
            src={user.profileImage}
            alt="유저 프로필"
            className="w-20 h-20 rounded-full object-cover border mb-3"
          />
          <h2 className="text-lg font-semibold">{user.name}</h2>
          <p className="text-xs text-gray-500 mb-4">{user.email}</p>
          <div className="grid grid-cols-2 gap-2 w-full text-xs">
            <div className="text-gray-500">
              보유한 크레딧
              <div className="font-bold text-sm text-black">
                {user.credit} 크레딧
              </div>
            </div>
            <div className="text-gray-500">
              총 수입
              <div className="font-bold text-sm text-black">
                {user.totalEarnings} 크레딧
              </div>
            </div>
            <div className="text-gray-500">
              생성한 보이스팩
              <div className="font-bold text-sm text-black">
                {user.createdPacks}개
              </div>
            </div>
            <div className="text-gray-500">
              구매한 보이스팩
              <div className="font-bold text-sm text-black">
                {user.boughtPacks}개
              </div>
            </div>
          </div>
        </div>

        {/* 수익 그래프 */}
        <Section title="월별 수익 통계" icon="📊">
          <Line
            data={earningsChart}
            options={{
              responsive: true,
              plugins: {
                legend: { display: false },
                title: { display: false },
              },
              maintainAspectRatio: false,
            }}
            height={100}
          />
        </Section>
      </div>

      {/* 보이스팩/수익 요약 */}
      <div className="lg:col-span-2 grid grid-cols-1 sm:grid-cols-2 gap-4 content-stretch">
        <Section title="최근 생성한 보이스팩" icon="🎤" className="h-full">
          <ul className="list-disc pl-4">
            {recentCreated.map((pack, idx) => (
              <li key={idx}>{pack} - 100 크레딧</li>
            ))}
          </ul>
        </Section>

        <Section title="최근 구매한 보이스팩" icon="🛒" className="h-full">
          <ul className="list-disc pl-4">
            {recentBought.map((pack, idx) => (
              <li key={idx}>{pack} - 100 크레딧</li>
            ))}
          </ul>
        </Section>

        <Section title="최근 판매 수익" icon="💰" className="h-full">
          <ul className="list-disc pl-4">
            {recentSales.map((sale, idx) => (
              <li key={idx}>
                {sale.name} - {sale.amount / 100} 크레딧
              </li>
            ))}
          </ul>
        </Section>

        <Section title="최근 충전 내역" icon="💳" className="h-full">
          <ul className="list-disc pl-4">
            {recentPayments.map((pay, idx) => (
              <li key={idx}>
                {pay.date} - {pay.amount.toLocaleString()}원 ({pay.amount / 100}{' '}
                크레딧)
              </li>
            ))}
          </ul>
        </Section>
      </div>
    </div>
  );
};

export default MyDashboard;
