import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const LandingPage = () => {
  const navigate = useNavigate();
  const [voiceData, setVoiceData] = useState(null);

  const apiUrl = process.env.REACT_APP_SIGNUP_API_URL;

  // ✅ 뒤로가기 방지 로직 추가
  useEffect(() => {
    const handlePopState = (event) => {
      event.preventDefault();
      window.history.pushState(null, '', window.location.pathname); // 뒤로가기 방지
    };

    window.history.pushState(null, '', window.location.pathname); // 히스토리 추가
    window.addEventListener('popstate', handlePopState);

    return () => {
      window.removeEventListener('popstate', handlePopState);
    };
  }, []);

  const handleCreateVoice = async () => {
    try {
      const response = await fetch(`${apiUrl}/test`, {
        method: 'GET',
        credentials: 'include',
        mode: 'cors'
      });
  
      const textData = await response.text();
  
      let data;
      try {
        data = JSON.parse(textData);
      } catch (error) {
        console.warn('JSON 변환 실패, 원본 응답:', textData);
        data = { message: textData };
      }
  
      setVoiceData(data);
      alert(`보이스팩 생성 응답: ${JSON.stringify(data, null, 2)}`);
    } catch (error) {
      console.error('보이스팩 생성 오류:', error);
      alert('보이스팩 생성 중 오류가 발생했습니다.');
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-white">
      <div className="flex flex-col gap-4">
        <button
          onClick={handleCreateVoice}
          className="bg-purple-500 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded-md w-80"
        >
          보이스팩 생성
        </button>
        <button 
          onClick={() => navigate('/voiceMarket')}
          className="bg-purple-500 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded-md w-80"
        >
          보이스팩 구매
        </button>
        <button className="bg-purple-500 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded-md w-80">
          보이스팩 사용처(챗봇)
        </button>
      </div>

      {voiceData && (
        <div className="mt-6 p-4 bg-gray-100 rounded-md w-80">
          <h2 className="text-lg font-semibold">서버 응답:</h2>
          <pre className="text-sm text-gray-700">{JSON.stringify(voiceData, null, 2)}</pre>
        </div>
      )}
    </div>
  );
};

export default LandingPage;
