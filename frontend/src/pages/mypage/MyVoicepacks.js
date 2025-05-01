import React, { useEffect, useState } from 'react';
import VoicePack from '../../components/common/VoicePack';
import useVoicepackUsage from '../../hooks/useVoicepackUsage';
import useUserStore from '../../utils/userStore';

const MyVoicepacks = () => {
  const { user } = useUserStore((state) => state);
  const [filter, setFilter] = useState('available');
  const { voicepacks } = useVoicepackUsage(filter);
  const [loading, setLoading] = useState(false);

  console.log(voicepacks, filter);

  return (
    <div className="bg-white p-6 rounded-xl shadow">
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
        <p className="col-span-full text-gray-500 text-md mt-12">
          보이스팩이 없습니다.
        </p>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-6 gab-7">
          {voicepacks.map((pack) => (
            <VoicePack
              key={pack.id}
              pack={pack}
              type="mypage"
              filter={pack.author === user?.email ? 'mine' : 'purchased'}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default MyVoicepacks;
