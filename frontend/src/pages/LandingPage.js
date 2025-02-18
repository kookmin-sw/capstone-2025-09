import React from 'react';
import { useNavigate } from 'react-router-dom';

const LandingPage = () => {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-white">
      <div className="flex flex-col gap-4">
        <button
          onClick={() => navigate('/createVoice')}
          className="bg-purple-500 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded-md w-80"
        >
          보이스팩 생성
        </button>
        <button 
          onClick={() => navigate('/voiceMarket')}
          className="bg-purple-500 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded-md w-80">
          보이스팩 구매
        </button>
        <button className="bg-purple-500 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded-md w-80">
          보이스팩 사용처(챗봇)
        </button>
      </div>
    </div>
  );
};

export default LandingPage;
