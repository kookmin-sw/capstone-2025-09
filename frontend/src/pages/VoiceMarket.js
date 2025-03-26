import React, { useEffect, useState } from 'react';

function VoiceMarket() {
  const [voicePacks, setVoicePacks] = useState([]);
  const [playingIndex, setPlayingIndex] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchVoicePacks = async () => {
      const apiUrl = process.env.REACT_APP_VOICEPACK_API_URL;
      console.log('📡 API 요청 중:', apiUrl); // API URL 출력

      try {
        const response = await fetch(apiUrl);
        if (!response.ok) {
          throw new Error(`HTTP 오류! 상태 코드: ${response.status}`);
        }
        const data = await response.json();
        console.log('📥 받아온 데이터:', data); // 데이터 콘솔 출력
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

  useEffect(() => {
    console.log('🔍 렌더링된 보이스팩:', voicePacks); // 렌더링 후 상태 확인
  }, [voicePacks]);

  const handlePlayAudio = (audioUrl, index) => {
    if (playingIndex !== index) {
      const audio = new Audio(audioUrl);
      audio.play();
      setPlayingIndex(index);

      audio.onended = () => {
        setPlayingIndex(null);
      };
    }
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
          {voicePacks.map((pack, index) => (
            <div key={index} className="bg-gray-800 text-white p-4 rounded-lg shadow-md">
              <h2 className="text-lg font-semibold mb-2">{pack.name}</h2>
              <div className="flex flex-wrap gap-2">
                {pack.audios.map((audioUrl, i) => (
                  <button
                    key={i}
                    onClick={() => handlePlayAudio(audioUrl, `${index}-${i}`)}
                    className={`px-3 py-2 rounded-full focus:outline-none transition-all duration-200 ${
                      playingIndex === `${index}-${i}`
                        ? 'bg-green-500 text-white' // 활성화 상태
                        : 'bg-gray-500 text-white hover:bg-gray-600'
                    }`}
                  >
                    {playingIndex === `${index}-${i}` ? '⏸️ 정지' : '▶️ 재생'}
                  </button>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default VoiceMarket;
