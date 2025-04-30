import React, { useState } from 'react';
import MyDashboard from './MyDashboard';
import MyVoicepacks from './MyVoicepacks';
import MyRevenue from './MyRevenue';
import MyPayments from './MyPayments';

const MyPage = () => {
  const [tab, setTab] = useState('dashboard');

  const user = {
    name: '박수연',
    email: 'suwith@kookmin.ac.kr',
    profileImage: 'https://avatars.githubusercontent.com/u/85792738?v=4',
    credit: 320,
    totalEarnings: 120000,
    createdPacks: 5,
    soldPacks: 3,
    boughtPacks: 7,
  };

  // 전체 보이스팩 더미데이터
  const allVoicepacks = [
    {
      id: 127,
      name: '감성 보이스',
      author: 'suwith@kookmin.ac.kr',
      createdAt: '2025-04-12T14:34:24.59599Z',
      price: 100,
      isPublic: true,
    },
    {
      id: 128,
      name: '낭독용 보이스',
      author: 'suwith@kookmin.ac.kr',
      createdAt: '2025-04-10T10:00:00.000Z',
      price: 100,
      isPublic: true,
    },
    {
      id: 129,
      name: 'AI 비서용 보이스',
      author: 'suwith@kookmin.ac.kr',
      createdAt: '2025-04-09T09:00:00.000Z',
      price: 100,
      isPublic: false,
    },
    {
      id: 130,
      name: '아나운서 보이스',
      author: 'kim@naver.com',
      createdAt: '2025-04-08T12:00:00.000Z',
      price: 100,
      isPublic: true,
    },
    {
      id: 131,
      name: '밝은 감정 보이스',
      author: 'lee@daum.net',
      createdAt: '2025-04-07T15:00:00.000Z',
      price: 100,
      isPublic: true,
    },
  ];
  // 최근 생성한 보이스팩 (작성자 이메일이 현재 유저와 같음)
  const recentCreated = allVoicepacks
    .filter((pack) => pack.author === user.email)
    .slice(0, 5);

  // 최근 구매한 보이스팩 (작성자 이메일이 현재 유저와 다름)
  const recentBought = allVoicepacks
    .filter((pack) => pack.author !== user.email)
    .slice(0, 5);

  const recentSales = [
    { name: '감성 보이스', amount: 10000, createdAt: '2025-04-27T12:00:00Z' },
    { name: '낭독용 보이스', amount: 12000, createdAt: '2025-04-17T12:00:00Z' },
  ];
  const recentPayments = [
    { date: '2025-04-10', amount: 5000, createdAt: '2025-04-27T12:00:00Z' },
    { date: '2025-04-01', amount: 10000, createdAt: '2025-04-14T12:00:00Z' },
  ];

  const earningsChart = {
    labels: ['1월', '2월', '3월', '4월'],
    datasets: [
      {
        label: '월별 수익',
        data: [30000, 25000, 20000, 45000],
        borderColor: 'rgba(75,192,192,1)',
        fill: false,
      },
    ],
  };

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">마이페이지</h1>

      <div className="flex space-x-4 border-b pb-2 text-sm font-medium">
        {[
          { key: 'dashboard', label: '대시보드' },
          { key: 'voicepacks', label: '보이스팩 관리' },
          { key: 'revenue', label: '수익 분석' },
          { key: 'payments', label: '크레딧 관리' },
        ].map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={`px-3 py-1 rounded-t ${
              tab === t.key
                ? 'bg-violet-50 text-indigo-500 border-b-2 border-indigo-500'
                : 'text-gray-500 hover:text-indigo-500'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'dashboard' && (
        <MyDashboard
          user={user}
          earningsChart={earningsChart}
          recentCreated={recentCreated}
          recentBought={recentBought}
          recentSales={recentSales}
          recentPayments={recentPayments}
        />
      )}
      {tab === 'voicepacks' && <MyVoicepacks />}
      {tab === 'revenue' && <MyRevenue credit={user.credit} />}
      {tab === 'payments' && <MyPayments />}
    </div>
  );
};

export default MyPage;
