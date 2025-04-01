import React, { useEffect, useState } from 'react';

function VoiceStore() {
  const [voicePacks, setVoicePacks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedPack, setSelectedPack] = useState(null);
  const [audioUrl, setAudioUrl] = useState('');
  const closeModal = () => {
    setSelectedPack(null);
    setAudioUrl('');
  };

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

  const handleCardClick = async (pack) => {
    const apiUrl = process.env.REACT_APP_VOICEPACK_API_URL;
    const endpoint = `${apiUrl}/example/${pack.id}`;
    setSelectedPack(pack);
    try {
      const response = await fetch(endpoint);
      if (!response.ok) {
        throw new Error('오디오를 불러오는 데 실패했습니다.');
      }
      const url = await response.text();
      setAudioUrl(url);
    } catch (err) {
      console.error(err);
      setAudioUrl('');
    }
  };

  const handlePurchase = async () => {
    if (!selectedPack) return;
    const userId = sessionStorage.getItem("userId"); // 혹은 sessionStorage.getItem("userId")

    const apiUrl = process.env.REACT_APP_VOICEPACK_API_URL;
    const purchaseEndpoint = `${apiUrl}/usage-right`; // 백엔드 구매 API 경로
    const payload = {
      userId,
      voicePackId: selectedPack.id,
    };

    console.log('voicePackId', payload);
    try {
      const response = await fetch(purchaseEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('구매 요청 실패');
      }

      const result = await response.json();
      alert(`✅ 구매 완료: ${result.message || '성공적으로 구매되었습니다.'}`);
      closeModal();
    } catch (err) {
      console.error('❌ 구매 실패:', err);
      alert('❌ 구매에 실패했습니다. 다시 시도해주세요.');
    }
  };

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
            <div
              key={pack.id}
              className="bg-gray-800 text-white p-4 rounded-lg shadow-md w-48 cursor-pointer hover:bg-gray-700 transition"
              onClick={() => handleCardClick(pack)}
            >
              <h2 className="text-lg font-semibold mb-2 text-center">{pack.name}</h2>
              <p className="text-gray-300 text-sm text-center">📧 {pack.author}</p>
              <p className="text-gray-400 text-xs text-center">📅 {formatDate(pack.createdAt)}</p>
            </div>
          ))}
        </div>
      )}

      {selectedPack && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
          <div className="bg-white p-6 rounded-lg shadow-lg w-96 text-center relative">
            <button
              onClick={closeModal}
              className="absolute top-2 right-2 text-gray-500 hover:text-black text-xl"
            >
              &times;
            </button>
            <h2 className="text-xl font-bold mb-4">{selectedPack.name} 미리듣기</h2>
            {audioUrl ? (
              <>
                <audio controls className="w-full" crossOrigin="anonymous">
                  <source src={audioUrl} type="audio/wav" />
                  브라우저가 오디오를 지원하지 않습니다.
                </audio>
                <button
                  className="mt-4 bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
                  onClick={handlePurchase}
                >
                구매
                </button>
              </>
            ) : (
              <p className="text-red-500">오디오를 불러오는 데 실패했습니다.</p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default VoiceStore;
