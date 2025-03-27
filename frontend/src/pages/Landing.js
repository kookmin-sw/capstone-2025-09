import React, {useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const Landing = () => {
  const navigate = useNavigate();

  const userId = sessionStorage.getItem("userId"); // 혹은 sessionStorage.getItem("userId")
  console.log("유저 아이디 : ", userId)
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

  const handleCreateVoice = () => {
    navigate('/voicecreate');
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
          onClick={() => navigate('/voicestore')}
          className="bg-purple-500 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded-md w-80"
        >
          보이스팩 구매
        </button>
        <button className="bg-purple-500 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded-md w-80">
          보이스팩 사용처(챗봇)
        </button>
      </div>
    </div>
  );
};

export default Landing;
