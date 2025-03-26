import React from 'react';
import { useNavigate } from 'react-router-dom';
import logo from '../assets/logo.svg';

const Landing = () => {
  const navigate = useNavigate();

  return (
    <div className="bg-white text-black">
      <header className="flex justify-between items-center p-6 bg-gray-100">
        <img src={logo} alt="COVOS" width="100" />
        <button
          onClick={() => navigate('/login')}
          className="bg-blue-500 text-white px-4 py-2 rounded"
        >
          로그인
        </button>
      </header>

      <section className="hero h-screen flex flex-col justify-center items-center bg-blue-100">
        <h1 className="text-3xl font-bold mb-4 text-center">
          당신만의 AI 보이스를 만들고,
          <br />
          공유하고, 활용하세요.
        </h1>
        <p className="text-lg mb-6 text-center">
          AI 음성 합성 기술로 나만의 목소리를 제작하고, 마켓플레이스에서
          공유하거나 직접 활용하세요.
        </p>
        <button
          onClick={() => navigate('/login')}
          className="bg-blue-500 text-white px-6 py-3 text-lg font-semibold rounded"
        >
          보이스팩 제작 시작하기
        </button>
      </section>

      <section className="feature h-screen flex flex-col justify-center items-center bg-white">
        <h2 className="text-2xl mb-2">🎙 AI 보이스팩 제작</h2>
        <p>
          쉽고 간편하게 음성을 녹음하고 AI 학습을 통해 보이스팩을 생성하세요.
        </p>
      </section>

      <section className="feature h-screen flex flex-col justify-center items-center bg-gray-100">
        <h2 className="text-2xl mb-2">🛍 마켓플레이스</h2>
        <p>생성한 보이스팩을 업로드하고, 수익을 창출하세요.</p>
        <div className="marketplace flex gap-4 mt-4">
          <div className="card w-28 h-36 bg-blue-200 rounded flex items-center justify-center">
            보이스팩 1
          </div>
          <div className="card w-28 h-36 bg-blue-200 rounded flex items-center justify-center">
            보이스팩 2
          </div>
          <div className="card w-28 h-36 bg-blue-200 rounded flex items-center justify-center">
            보이스팩 3
          </div>
        </div>
      </section>

      <section className="feature h-screen flex flex-col justify-center items-center bg-white">
        <h2 className="text-2xl mb-2">🧠 AI 음성 활용 기능</h2>
        <p>뉴스, 날씨, 명언을 맞춤형 AI 보이스로 들어보세요.</p>
        <ul className="mt-4 space-y-2">
          <li>📢 텍스트 변환 기능</li>
          <li>📡 실시간 뉴스 리딩</li>
          <li>🌤 맞춤형 날씨 알림</li>
        </ul>
      </section>

      <section className="testimonials h-screen flex flex-col justify-center items-center bg-gray-100">
        <h2 className="text-2xl mb-2">💬 사용자 리뷰</h2>
        <p>"이제 내 목소리를 AI로 활용할 수 있어서 놀랍습니다!"</p>
      </section>

      <section className="cta-final py-20 bg-blue-500 text-white text-center">
        <h2 className="text-2xl mb-4 font-semibold">
          지금, 당신의 AI 보이스를 만들어보세요.
        </h2>
        <button
          onClick={() => navigate('/login')}
          className="bg-white text-blue-500 px-6 py-2 rounded font-semibold"
        >
          지금 시작하기
        </button>
      </section>
    </div>
  );
};

export default Landing;
