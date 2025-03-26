import React, { useEffect, useState } from 'react';

function VoiceMarket() {
  const [voicePacks, setVoicePacks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchVoicePacks = async () => {
      try {
        // ⚠️ 실제 API 요청 대신 더미 데이터 사용
        // const response = await axios.get('/api/voice-packs');
        // setVoicePacks(response.data);

        const dummyData = [
          {
            id: 1,
            name: '감정 보이스팩',
            author: 'voice_creator1@example.com',
            createdAt: '2024-03-26T12:00:00Z',
          },
          {
            id: 2,
            name: '캐릭터 보이스팩',
            author: 'voice_creator2@example.com',
            createdAt: '2024-03-25T14:30:00Z',
          },
          {
            id: 3,
            name: '게임 보이스팩',
            author: 'voice_creator3@example.com',
            createdAt: '2024-03-20T10:15:00Z',
          },
        ];
        setVoicePacks(dummyData);
      } catch (err) {
        console.error('Error fetching voice packs:', err);
        setError('보이스팩을 불러오는 데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchVoicePacks();
  }, []);

  return (
    <div className="flex flex-col items-center min-h-screen bg-white p-4">
      <h1 className="text-3xl font-bold mb-8">보이스팩 구매</h1>

      {loading ? (
        <p>로딩 중...</p>
      ) : error ? (
        <p className="text-red-500">{error}</p>
      ) : (
        <div className="grid grid-cols-3 gap-4">
          {voicePacks.map((pack) => (
            <div key={pack.id} className="bg-gray-800 text-white p-4 rounded-lg shadow-md">
              <h2 className="text-lg font-semibold">{pack.name}</h2>
              <p className="text-sm text-gray-300">제작자: {pack.author}</p>
              <p className="text-sm text-gray-400">
                생성일: {new Date(pack.createdAt).toLocaleDateString()}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default VoiceMarket;
