import React, { useState } from 'react';
import VoicePack from '../../components/common/VoicePack';

const MyVoicepacks = ({ userId = 1 }) => {
  const [filter, setFilter] = useState('all');

  const voicepacks = [
    {
      id: 1,
      name: '감성 보이스',
      credit: 100,
      authorId: 1,
      author: '박수연',
      createdAt: '2025-04-01',
    },
    {
      id: 2,
      name: '낭독용 보이스',
      credit: 100,
      authorId: 1,
      author: '박수연',
      createdAt: '2025-04-02',
    },
    {
      id: 3,
      name: '아나운서 보이스',
      credit: 100,
      authorId: 2,
      author: '김지우',
      createdAt: '2025-04-03',
    },
    {
      id: 4,
      name: '밝은 감정 보이스',
      credit: 100,
      authorId: 2,
      author: '이서준',
      createdAt: '2025-04-05',
    },
  ];

  const filteredVoicepacks = voicepacks.filter((vp) => {
    if (filter === 'mine') return vp.authorId === userId;
    if (filter === 'purchased') return vp.authorId !== userId;
    return true;
  });

  const handleClick = (pack) => {
    console.log('보이스팩 클릭:', pack);
    // 추후 상세보기, 수정 등 연결
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(
      date.getDate()
    ).padStart(2, '0')}`;
  };

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
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
          {filteredVoicepacks.map((vp) => (
            <VoicePack
              key={vp.id}
              pack={vp}
              onClick={handleClick}
              formatDate={formatDate}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default MyVoicepacks;
