import React, { useState } from 'react';
import VoicePack from '../../components/common/VoicePack';
import useVoicepackUsage from '../../hooks/useVoicepackUsage';
import useUserStore from '../../utils/userStore';

const MyVoicepacks = () => {
  const { user } = useUserStore((state) => state);
  const [filter, setFilter] = useState('available');
  const [refreshKey, setRefreshKey] = useState(0);
  const { voicepacks } = useVoicepackUsage(filter, refreshKey);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 12;
  const handleRefresh = () => {
    setRefreshKey((prev) => prev + 1); // 값 변경 → useEffect 재실행
  };
  const [loading] = useState(false);

  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = voicepacks.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(voicepacks.length / itemsPerPage);

  return (
    <div className="bg-white p-6 rounded-xl ">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-lg font-bold">보이스팩 관리</h2>
        <select
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          className="border px-3 py-1 rounded text-sm"
        >
          <option value="available">전체 보이스팩</option>
          <option value="mine">내가 생성한 보이스팩</option>
          <option value="purchased">구매한 보이스팩</option>
        </select>
      </div>

      {loading ? (
        <p className="text-sm text-gray-500">불러오는 중...</p>
      ) : voicepacks.length === 0 ? (
        <p className="col-span-full text-gray-500 text-center text-sm mt-12">
          보이스팩이 없습니다.
        </p>
      ) : (
        <div className="container mx-auto min-h-screen-1/2">
          <div className="mx-auto grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-7 justify-center">
            {currentItems.map((pack) => (
              <VoicePack
                key={pack.id}
                pack={pack}
                type="mypage"
                filter={pack.author === user?.email ? 'mine' : 'purchased'}
                onRefresh={handleRefresh}
              />
            ))}
          </div>
          {/* 페이지네이션 UI */}
          {totalPages > 1 && (
            <div className="flex justify-center mt-6 space-x-2">
              {Array.from({ length: totalPages }, (_, index) => (
                <button
                  key={index}
                  onClick={() => {
                    setCurrentPage(index + 1);
                    window.scrollTo({ top: 0, behavior: 'smooth' });
                  }}
                  className={`px-3 py-1 rounded-md ${
                    currentPage === index + 1
                      ? 'bg-indigo-400 text-white'
                      : 'bg-gray-200 hover:bg-gray-300'
                  }`}
                >
                  {index + 1}
                </button>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default MyVoicepacks;
