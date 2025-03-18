import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const LandingPage = () => {
  const navigate = useNavigate();
  const [voiceData, setVoiceData] = useState(null); // ✅ API 응답 저장용 상태

  const apiUrl = process.env.REACT_APP_SIGNUP_API_URL; // ✅ 환경변수에서 API URL 가져오기

  // ✅ "보이스팩 생성" 버튼 클릭 시 /test API 호출
  const handleCreateVoice = async () => {
    try {
      const response = await fetch(`${apiUrl}/test2`, {
        method: 'GET',
        credentials: 'include', // ✅ 쿠키 포함
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
          onClick={handleCreateVoice} // ✅ 버튼 클릭 시 API 호출
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

      {/* ✅ API 응답을 화면에 표시 */}
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
