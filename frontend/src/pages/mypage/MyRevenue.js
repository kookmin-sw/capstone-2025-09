import React from 'react';
import { Bar, Line } from 'react-chartjs-2';
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
} from 'chart.js';

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

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
  // 추후 API 응답 기반으로 변경 예정
  const dummySales = [
    { id: 1, name: '감성 보이스', count: 2 },
    { id: 2, name: '낭독용 보이스', count: 1 },
    { id: 3, name: 'AI 보이스', count: 0 },
  ];

  const salesByVoicepack = {
    labels: dummySales.map((s) => s.name),
    datasets: [
      {
        label: '판매 건수',
        data: dummySales.map((s) => s.count),
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
      <RevenueStatCards total={320} month={200} count={3} />

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="overflow-x-auto">
          <h3 className="text-sm font-semibold mb-2">보이스팩별 판매 건수</h3>
          <div className="min-w-[500px]">
            <Bar
              data={salesByVoicepack}
              options={{
                responsive: true,
                plugins: {
                  legend: { display: false },
                  title: { display: false },
                },
              }}
              height={100}
            />
          </div>
        </div>

        <div>
          <h3 className="text-sm font-semibold mb-2">월별 수익 추이</h3>
          <Line
            data={monthlyRevenue}
            options={{
              responsive: true,
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

export default MyRevenue;
