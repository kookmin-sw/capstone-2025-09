import React, { useState } from 'react';
import { Line, Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  ArcElement,
} from 'chart.js';

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  ArcElement
);

const Section = ({ title, icon, children, className = '' }) => (
  <div className={`bg-white rounded-xl shadow p-4 ${className}`}>
    <h3 className="text-sm font-medium mb-2 flex items-center gap-2 text-gray-600">
      <span className="text-base">{icon}</span> {title}
    </h3>
    <div className="text-xs text-gray-700 leading-relaxed overflow-y-auto max-h-32">
      {children}
    </div>
  </div>
);
const VoicepackCard = ({ name, credit, isCreator, isPublic }) => (
  <div className="bg-indigo-200 bg-opacity-30 rounded-lg shadow p-4 flex flex-col justify-between h-48 w-full">
    <div>
      <h4 className="font-semibold text-sm mb-1">{name}</h4>
      <p className="text-xs text-gray-500 mb-3">{credit} 크레딧</p>
    </div>
    {isCreator ? (
      <div className="flex items-center justify-between text-xs text-gray-600">
        <div className="space-x-2">
          <button className="text-blue-500">수정</button>
          <button className="text-red-500">삭제</button>
        </div>
        <label>
          <input type="checkbox" defaultChecked={isPublic} className="mr-1" />{' '}
          공개
        </label>
      </div>
    ) : (
      <button className="text-blue-500 text-xs w-full text-center">
        ▶ 듣기
      </button>
    )}
  </div>
);

const DashboardContent = ({
  user,
  earningsChart,
  recentCreated,
  recentBought,
  recentSales,
  recentPayments,
}) => (
  <div className="max-w-full overflow-hidden grid grid-cols-1 lg:grid-cols-3 gap-4">
    <div className="lg:col-span-1 flex flex-col gap-4">
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

      <Section title="월별 수익 통계" icon="📊">
        <Line
          data={earningsChart}
          options={{
            responsive: true,
            plugins: { legend: { display: false }, title: { display: false } },
            maintainAspectRatio: false,
          }}
          height={100}
        />
      </Section>
    </div>

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

const MyVoicepacks = () => {
  const userId = 1;

  const [filter, setFilter] = useState('all');

  const voicepacks = [
    { id: 1, name: '감성 보이스', credit: 100, authorId: 1 },
    { id: 2, name: '낭독용 보이스', credit: 100, authorId: 1 },
    { id: 3, name: '아나운서 보이스', credit: 100, authorId: 2 },
    { id: 4, name: '밝은 감정 보이스', credit: 100, authorId: 2 },
  ];

  const filteredVoicepacks = voicepacks.filter((vp) => {
    if (filter === 'mine') return vp.authorId === userId;
    if (filter === 'purchased') return vp.authorId !== userId;
    return true;
  });

  return (
    <div className="bg-white p-6 rounded-xl shadow">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-lg font-bold">보이스팩 관리</h2>
        <select
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          className="border px-3 py-1 rounded text-sm"
        >
          <option value="all">전체 보이스팩</option>
          <option value="mine">내가 생성한 보이스팩</option>
          <option value="purchased">구매한 보이스팩</option>
        </select>
      </div>

      {filteredVoicepacks.length === 0 ? (
        <p className="text-sm text-gray-500">보여줄 보이스팩이 없습니다.</p>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-7 gap-4">
          {filteredVoicepacks.map((vp) => (
            <VoicepackCard
              key={vp.id}
              name={vp.name}
              credit={vp.credit}
              isCreator={vp.authorId === userId}
              isPublic={vp.authorId === userId} // default true
            />
          ))}
        </div>
      )}
    </div>
  );
};
const RevenueStatCards = ({ total, month, count }) => (
  <div className="grid grid-cols-3 gap-4 mb-6 text-sm">
    <div className="bg-indigo-100 p-4 rounded shadow text-center">
      <p className="text-gray-500">총 수익</p>
      <p className="font-bold">{total} 크레딧</p>
    </div>
    <div className="bg-indigo-100 p-4 rounded shadow text-center">
      <p className="text-gray-500">이번 달 수익</p>
      <p className="font-bold">{month} 크레딧</p>
    </div>
    <div className="bg-indigo-100 p-4 rounded shadow text-center">
      <p className="text-gray-500">판매 수</p>
      <p className="font-bold">{count}건</p>
    </div>
  </div>
);

const MyRevenue = () => {
  const dummySales = [
    {
      id: 1,
      date: '2025-04-15',
      name: '감성 보이스',
      buyer: 'user123',
      amount: 100,
    },
    {
      id: 2,
      date: '2025-04-10',
      name: '낭독용 보이스',
      buyer: 'user456',
      amount: 100,
    },
    {
      id: 3,
      date: '2025-03-20',
      name: '감성 보이스',
      buyer: 'user789',
      amount: 100,
    },
  ];

  const salesByVoicepack = {
    labels: ['감성 보이스', '낭독용 보이스', 'AI 보이스'],
    datasets: [
      {
        label: '판매 건수',
        data: [2, 1, 0],
        backgroundColor: 'rgba(99, 102, 241, 0.6)',
      },
    ],
  };

  const monthlyRevenue = {
    labels: ['1월', '2월', '3월', '4월'],
    datasets: [
      {
        label: '월별 수익 (크레딧)',
        data: [0, 0, 100, 200],
        borderColor: 'rgba(99, 102, 241, 1)',
        backgroundColor: 'rgba(99, 102, 241, 0.1)',
        fill: true,
      },
    ],
  };

  return (
    <div className="bg-white p-6 rounded-xl shadow space-y-6">
      {/* 요약 카드 */}
      <div className="grid grid-cols-3 gap-4 mb-6 text-sm">
        <div className="bg-indigo-100 p-4 rounded shadow text-center">
          <p className="text-gray-500">총 수익</p>
          <p className="font-bold">320 크레딧</p>
        </div>
        <div className="bg-indigo-100 p-4 rounded shadow text-center">
          <p className="text-gray-500">이번 달 수익</p>
          <p className="font-bold">200 크레딧</p>
        </div>
        <div className="bg-indigo-100 p-4 rounded shadow text-center">
          <p className="text-gray-500">판매 수</p>
          <p className="font-bold">3건</p>
        </div>
      </div>

      {/* 시각화 차트 2개: col로 정렬 */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* 보이스팩별 판매 건수 */}
        <div className="overflow-x-auto">
          <h3 className="text-sm font-semibold mb-2">보이스팩별 판매 건수</h3>
          <div className="min-w-[500px]">
            <Bar
              data={salesByVoicepack}
              options={{
                responsive: true,
                scales: {
                  y: {
                    beginAtZero: true,
                    suggestedMax: 5,
                    ticks: { stepSize: 1 },
                  },
                },
                plugins: {
                  legend: { display: false },
                  title: { display: false },
                },
              }}
              height={100}
            />
          </div>
        </div>

        {/* 월별 수익 추이 */}
        <div>
          <h3 className="text-sm font-semibold mb-2">월별 수익 추이</h3>
          <Line
            data={monthlyRevenue}
            options={{
              responsive: true,
              elements: {
                line: { tension: 0.4 },
                point: { radius: 3 },
              },
              plugins: {
                legend: { display: false },
                title: { display: false },
              },
            }}
            height={100}
          />
        </div>
      </div>
    </div>
  );
};

const CreditStatCards = ({ current, charged, used, onRequestExchange }) => (
  <div className="grid grid-cols-3 gap-4 mb-6 text-sm relative">
    <div className="bg-purple-100 p-4 rounded shadow text-center relative">
      <p className="text-gray-500">보유 크레딧</p>
      <p className="font-bold text-lg">{current} 크레딧</p>
      <button
        onClick={onRequestExchange}
        className="absolute right-3 top-7 text-xs rounded-sm bg-indigo-300 px-2 py-1"
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
const CreditHistoryTabs = () => {
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
    <div className="space-y-4">
      <div className="flex space-x-4 text-sm">
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

      {tab === 'charge' && (
        <ul className="text-xs space-y-2">
          {charges.map((c, i) => (
            <li key={i} className="flex justify-between border-b pb-1">
              <span>{c.date}</span>
              <span>
                {c.amount.toLocaleString()}원 ({c.credit} 크레딧, {c.method})
              </span>
            </li>
          ))}
        </ul>
      )}

      {tab === 'use' && (
        <ul className="text-xs space-y-2">
          {usages.map((u, i) => (
            <li key={i} className="flex justify-between border-b pb-1">
              <span>{u.date}</span>
              <span>
                {u.usage} - {u.credit} 크레딧
              </span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

const CreditExchangeHistory = () => {
  const exchangeRequests = [
    { id: 1, date: '2025-04-01', amount: 50000, status: '진행중' },
    { id: 2, date: '2025-03-25', amount: 32000, status: '완료' },
  ];

  return (
    <div className="space-y-2 text-xs mt-6">
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

const CreditChargeForm = () => (
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
);

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
      <CreditStatCards
        current={currentCredit}
        charged={500}
        used={150}
        onRequestExchange={handleExchange}
      />
      <CreditHistoryTabs />
      <CreditExchangeHistory />
      <CreditChargeForm />
    </div>
  );
};
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

  const recentCreated = ['감성 보이스', '낭독용 보이스', 'AI 비서용 보이스'];
  const recentBought = ['아나운서 보이스', '밝은 감정 보이스'];
  const recentSales = [
    { name: '감성 보이스', amount: 10000 },
    { name: '낭독용 보이스', amount: 12000 },
  ];
  const recentPayments = [
    { date: '2025-04-10', amount: 5000 },
    { date: '2025-04-01', amount: 10000 },
  ];
  const voicepacks = [
    { id: 1, name: '감성 보이스', credit: 100, authorId: 1 },
    { id: 2, name: '낭독용 보이스', credit: 100, authorId: 1 },
    { id: 3, name: '아나운서 보이스', credit: 100, authorId: 2 },
    { id: 4, name: '밝은 감정 보이스', credit: 100, authorId: 2 },
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
  const userId = 1; // 로그인한 사용자
  const [filter, setFilter] = useState('all');

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">마이페이지</h1>

      <div className="flex space-x-4 border-b pb-2 text-sm font-medium">
        {[
          { key: 'dashboard', label: '대시보드' },
          { key: 'voicepacks', label: '보이스팩 관리' },
          { key: 'revenue', label: '수입 관리' },
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
        <DashboardContent
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
