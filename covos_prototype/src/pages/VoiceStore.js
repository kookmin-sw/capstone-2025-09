import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Logo from '../icons/covosLogo.svg';


function VoiceStore() {
  const [voicePacks, setVoicePacks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedPack, setSelectedPack] = useState(null);
  const [audioUrl, setAudioUrl] = useState('');
  const API_URL = process.env.REACT_APP_VOICEPACK_API_URL;
  const navigate = useNavigate();

  const closeModal = () => {
    setSelectedPack(null);
    setAudioUrl('');
  };

  const fetchData = async (url, options = {}) => {
    const response = await fetch(url, options);
    if (!response.ok) throw new Error(`HTTP 오류: ${response.status}`);
    return response;
  };

  useEffect(() => {
    const fetchVoicePacks = async () => {
      try {
        const res = await fetchData(API_URL);
        const data = await res.json();
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
  }, [API_URL]);

  const handleCardClick = async (pack) => {
    setSelectedPack(pack);
    try {
      const res = await fetchData(`${API_URL}/example/${pack.id}`);
      const url = await res.text();
      setAudioUrl(url);
    } catch (err) {
      console.error('❌ 오디오 로딩 실패:', err);
      setAudioUrl('');
    }
  };

  const handlePurchase = async () => {
    if (!selectedPack) return;

    const userId = Number(sessionStorage.getItem("userId"));
    const voicepackId = selectedPack.id;
    const purchaseUrl = `${API_URL}/usage-right?userId=${userId}&voicepackId=${voicepackId}`;

    try {
      const res = await fetchData(purchaseUrl, {
        method: 'POST',
        credentials: 'include',
      });

      const result = await res.json();
      alert(`✅ 구매 완료: ${result.message || '성공적으로 구매되었습니다.'}`);
      closeModal();
    } catch (err) {
      console.error('❌ 구매 실패:', err);
      alert('❌ 구매에 실패했습니다. 다시 시도해주세요.');
    }
  };

  const formatDate = (isoString) => new Date(isoString).toISOString().split('T')[0];

  return (
    <div className="flex flex-col items-center min-h-screen bg-white p-4">
      <div className="mb-8 cursor-pointer" onClick={() => navigate('/landing')}>
        <img src={Logo} alt="Logo"/>
      </div>
      <h1 className="text-3xl font-bold mb-8">보이스팩 구매</h1>

      {loading ? (
        <p>로딩 중...</p>
      ) : error ? (
        <p className="text-red-500">{error}</p>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          {voicePacks.map((pack) => (
            <div
              key={pack.id}
              className="bg-gray-800 text-white p-4 rounded-lg shadow-md cursor-pointer hover:bg-gray-700 transition w-full"
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
                  <source src={audioUrl} type="audio/wav"/>
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
