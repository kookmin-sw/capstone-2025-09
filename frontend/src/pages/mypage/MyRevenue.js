import React, { useEffect, useState } from 'react';
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
import useUserStore from '../../utils/userStore';
import axiosInstance from '../../utils/axiosInstance';

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

const MyRevenue = () => {
  const { user } = useUserStore((state) => state);
  const [summary, setSummary] = useState(null);
  const [sales, setSales] = useState([]);
  const [monthlyRevenueData, setMonthlyRevenueData] = useState({
    labels: [],
    datasets: [
      {
        label: '월별 수익 (크레딧)',
        data: [],
        borderColor: 'rgba(99, 102, 241, 1)',
        backgroundColor: 'rgba(99, 102, 241, 0.1)',
        fill: true,
      },
    ],
  });

  useEffect(() => {
    const fetchData = async () => {
      if (!user?.id) return;

      try {
        // 1. 통계 요약 데이터
        const summaryRes = await axiosInstance.get(
          `sales/summary?sellerId=${user.id}`
        );
        setSummary(summaryRes.data);

        // 2. 판매 내역 (페이지 0, 10개씩, transactionDate 정렬)
        const salesRes = await axiosInstance.get('sales', {
          params: {
            sellerId: user.id,
            page: 0,
            size: 100,
            sort: 'transactionDate',
          },
        });
        setSales(salesRes.data.content || []);
      } catch (err) {
        console.error('판매 데이터 조회 실패:', err);
      }
    };

    fetchData();
  }, [user?.id]);

  const salesByVoicepack = {
    labels: sales.map((s) => s.voicepackName),
    datasets: [
      {
        label: '판매 건수',
        data: sales.map(() => 1), // 각각 1건씩
        backgroundColor: 'rgba(99, 102, 241, 0.6)',
      },
    ],
  };

  useEffect(() => {
    const now = new Date();
    const recentMonths = Array.from({ length: 6 }, (_, i) => {
      const date = new Date(now.getFullYear(), now.getMonth() - (5 - i));
      return {
        label: `${date.getMonth() + 1}월`,
        month: date.getMonth() + 1,
      };
    });

    const monthlyMap = new Map();
    sales.forEach(({ date, amount }) => {
      const saleMonth = new Date(date).getMonth() + 1;
      monthlyMap.set(saleMonth, (monthlyMap.get(saleMonth) || 0) + amount);
    });

    const labels = recentMonths.map((m) => m.label);
    const data = recentMonths.map((m) => monthlyMap.get(m.month) || 0);

    setMonthlyRevenueData({
      labels,
      datasets: [
        {
          label: '월별 수익 (크레딧)',
          data,
          borderColor: 'rgba(99, 102, 241, 1)',
          backgroundColor: 'rgba(99, 102, 241, 0.1)',
          fill: true,
        },
      ],
    });
  }, [sales]);

  const RevenueStatCards = ({ total, month, count }) => (
    <div className="grid grid-cols-3 gap-4 mb-6 text-sm">
      <div className="bg-indigo-100 p-4 rounded text-center">
        <p className="text-gray-500">총 수익</p>
        <p className="font-bold text-base">{total} 크레딧</p>
      </div>
      <div className="bg-indigo-100 p-4 rounded text-center">
        <p className="text-gray-500">이번 달 수익</p>
        <p className="font-bold text-base">{month} 크레딧</p>
      </div>
      <div className="bg-indigo-100 p-4 rounded text-center">
        <p className="text-gray-500">판매 수</p>
        <p className="font-bold text-base">{count}건</p>
      </div>
    </div>
  );

  return (
    <div className="bg-white p-6 rounded-xl space-y-6">
      <RevenueStatCards
        total={summary?.totalRevenue || 0}
        month={summary?.monthlyRevenue || 0}
        count={summary?.salesCount || 0}
      />

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
                scales: {
                  y: {
                    beginAtZero: true,
                    max: 5,
                    ticks: {
                      stepSize: 1,
                      precision: 0,
                    },
                  },
                },
              }}
              height={100}
            />
          </div>
        </div>

        <div>
          <h3 className="text-sm font-semibold mb-2">월별 수익 추이</h3>
          <Line
            data={monthlyRevenueData}
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
