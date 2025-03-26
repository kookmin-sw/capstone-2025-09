import React, { useEffect, useState } from 'react';

function VoiceMarket() {
  const [voicePacks, setVoicePacks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchVoicePacks = async () => {
      const apiUrl = process.env.REACT_APP_VOICEPACK_API_URL;

      try {
        const response = await fetch(apiUrl);
        if (!response.ok) {
          throw new Error(`HTTP 오류! 상태 코드: ${response.status}`);
        }
        const data = await response.json();
        console.log('📥 받아온 데이터:', data);
        setVoicePacks(data);
      } catch (err) {
        console.error('❌ 보이스팩 불러오기 실패:', err);
        setError('보이스팩을 불러오는 데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };

    fetchVoicePacks();
  }, []);

  // 날짜 형식 변환 (YYYY-MM-DD)
  const formatDate = (isoString) => {
    const date = new Date(isoString);
    return date.toISOString().split('T')[0];
  };

  return (
    <div className="flex flex-col items-center min-h-screen bg-white p-4">
      <h1 className="text-3xl font-bold mb-8">보이스팩 구매</h1>

      {loading ? (
        <p>로딩 중...</p>
      ) : error ? (
        <p className="text-red-500">{error}</p>
      ) : (
        <div className="grid grid-cols-4 gap-4">
          {voicePacks.map((pack) => (
            <div key={pack.id} className="bg-gray-800 text-white p-4 rounded-lg shadow-md w-48">
              <h2 className="text-lg font-semibold mb-2 text-center">{pack.name}</h2>
              <p className="text-gray-300 text-sm text-center">📧 {pack.author}</p>
              <p className="text-gray-400 text-xs text-center">📅 {formatDate(pack.createdAt)}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default VoiceMarket;
