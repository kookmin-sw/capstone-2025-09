import React, { useEffect, useState } from 'react';
import { Line } from 'react-chartjs-2';
import Section from '../../components/mypage/Section';
import VoicePack from '../../components/common/VoicePack';
import useFetchUserInfo from '../../hooks/useUserInfo';
import useVoicepackUsage from '../../hooks/useVoicepackUsage';
import axiosInstance from '../../utils/axiosInstance';
import { User } from 'lucide-react';

const MyDashboard = ({ user, recentBought }) => {
  const userId = user?.id;

  const [refreshKey, setRefreshKey] = useState(0);
  const { voicepacks: createdVoicepacks } = useVoicepackUsage(
    'mine',
    refreshKey
  );
  const recentCreated = createdVoicepacks.slice(0, 5);
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

  const [recentSales, setRecentSales] = useState([]);
  const [recentPayments, setRecentPayments] = useState([]);

  const handleRefresh = () => setRefreshKey((prev) => prev + 1);

  useFetchUserInfo(userId);

  useEffect(() => {
    if (!userId) return;
    const fetchData = async () => {
      try {
        // 판매 내역 가져오기
        const salesRes = await axiosInstance.get('/sales', {
          params: {
            sellerId: user.id,
            page: 0,
            size: 100,
            sort: 'transactionDate',
          },
        });

        const sales = salesRes.data.content || [];

        // 현재 날짜 기준 최근 4개월 리스트 생성 (월 번호 기준)
        const now = new Date();
        const recentMonths = Array.from({ length: 4 }, (_, i) => {
          const date = new Date(now.getFullYear(), now.getMonth() - (3 - i));
          return {
            label: `${date.getMonth() + 1}월`,
            month: date.getMonth() + 1,
          };
        });

        // 월별 수익 집계
        const monthlyMap = new Map();
        sales.forEach(({ date, amount }) => {
          const saleMonth = new Date(date).getMonth() + 1;
          monthlyMap.set(saleMonth, (monthlyMap.get(saleMonth) || 0) + amount);
        });

        const labels = recentMonths.map((m) => m.label);
        const data = recentMonths.map((m) => monthlyMap.get(m.month) || 0);

        // 그래프 데이터 세팅
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

        // 최근 판매 수익 5개
        setRecentSales(sales.slice(0, 3));

        // 최근 충전 내역 (최대 3개)
        const historyRes = await axiosInstance.get(
          `/credits/history/${user.id}`
        );
        const charges = historyRes.data.charges || [];
        setRecentPayments(charges.slice(0, 3));
      } catch (err) {
        console.error('대시보드 데이터 로딩 실패:', err);
      }
    };

    fetchData();
  }, [userId]);

  if (!user) {
    return (
      <div className="text-center py-10 text-gray-500 text-sm">
        유저 정보를 불러오는 중입니다...
      </div>
    );
  }
  const data = monthlyRevenueData.datasets[0].data;
  const allZero = data.every((v) => v === 0);

  const yScaleOptions = allZero
    ? {
        max: 4,
        ticks: {
          stepSize: 1,
          precision: 0,
          callback: (value) => (Number.isInteger(value) ? value : null),
        },
      }
    : {
        beginAtZero: true,
        ticks: {
          precision: 0,
          callback: (value) => (Number.isInteger(value) ? value : null),
        },
      };

  return (
    <div className="max-w-full overflow-hidden grid grid-cols-1 lg:grid-cols-3 gap-4">
      {/* 좌측: 프로필 + 그래프 */}
      <div className="lg:col-span-1 flex flex-col gap-4">
        <div className="bg-white p-4 rounded-xl flex flex-col items-center text-center">
          {user.profileImageUrl ? (
            <img
              src={user.profileImageUrl}
              alt="유저 프로필"
              className="w-20 h-20 rounded-full object-cover border mb-3"
            />
          ) : (
            <div className="w-20 h-20 rounded-full bg-gray-100 flex items-center justify-center border mb-3">
              <User className="w-10 h-10 text-gray-400" />
            </div>
          )}
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
            data={monthlyRevenueData}
            options={{
              responsive: true,
              plugins: {
                legend: { display: false },
                title: { display: false },
              },
              scales: {
                y: yScaleOptions,
              },
              layout: {
                padding: { top: 10, bottom: 10 }, // 여유 확보
              },
            }}
            height={110}
          />
        </Section>
      </div>

      {/* 우측: 보이스팩 요약 및 수익/충전 내역 */}
      <div className="lg:col-span-2 grid grid-cols-1 sm:grid-cols-2 gap-4 content-stretch">
        <Section title="최근 생성한 보이스팩" icon="🎤" className="h-full">
          <div className="overflow-x-auto scrollbar-hide">
            <div className="flex flex-nowrap gap-3 pr-2 min-w-fit">
              {recentCreated.length > 0 ? (
                recentCreated.map((pack) => (
                  <VoicePack
                    key={pack.id}
                    pack={pack}
                    type="dashboard"
                    onRefresh={handleRefresh}
                  />
                ))
              ) : (
                <p className="text-xs pl-4 text-gray-400">
                  생성한 보이스팩이 없습니다.
                </p>
              )}
            </div>
          </div>
        </Section>

        <Section title="최근 구매한 보이스팩" icon="🛒" className="h-full">
          <div className="overflow-x-auto scrollbar-hide">
            <div className="flex flex-nowrap gap-3 pr-2 min-w-fit">
              {recentBought.length > 0 ? (
                recentBought.map((pack) => (
                  <VoicePack key={pack.id} pack={pack} type="dashboard" />
                ))
              ) : (
                <p className="text-xs pl-4 text-gray-400">
                  구매한 보이스팩이 없습니다.
                </p>
              )}
            </div>
          </div>
        </Section>

        <Section title="최근 판매 수익" icon="💰" className="h-full">
          <ul className="list-disc pl-4">
            {recentSales.length > 0 ? (
              recentSales.map((sale, idx) => (
                <li key={idx}>
                  {sale.voicepackName} - {sale.amount} 크레딧
                </li>
              ))
            ) : (
              <p className="text-xs text-gray-400">판매 내역이 없습니다.</p>
            )}
          </ul>
        </Section>

        <Section title="최근 충전 내역" icon="💳" className="h-full">
          <ul className="list-disc pl-4">
            {recentPayments.length > 0 ? (
              recentPayments.map((pay, idx) => (
                <li key={idx}>
                  {new Date(pay.date).toLocaleDateString()} -{' '}
                  {pay.method || '충전'} ({pay.amountCredit} 크레딧)
                </li>
              ))
            ) : (
              <p className="text-xs text-gray-400">충전 내역이 없습니다.</p>
            )}
          </ul>
        </Section>
      </div>
    </div>
  );
};

export default MyDashboard;
