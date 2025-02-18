import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const LoginPage = () => {
  const navigate = useNavigate();
  const [isSignUp, setIsSignUp] = useState(false); // 회원가입 모드인지 확인하는 상태

  const handleLoginOrSignUp = () => {
    if (isSignUp) {
      // 회원가입 시 처리 로직 (실제 회원가입 API 호출 추가 가능)
      alert('회원가입이 완료되었습니다!'); 
      setIsSignUp(false); // 회원가입 후 다시 로그인 화면으로 이동
    } else {
      navigate('/landingpage');
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen">
      <h1 className="text-3xl font-bold absolute top-40">
        {isSignUp ? '회원가입' : '로그인'}
      </h1>

      <form className="w-full max-w-sm">
        <div className="mb-4">
          <label
            className="block text-gray-700 text-sm font-bold mb-2"
            htmlFor="email"
          >
            이메일
          </label>
          <input
            className="bg-[#F8FAFC] rounded-md w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            id="email"
            type="email"
          />
        </div>
        <div className="mb-6">
          <label
            className="block text-gray-700 text-sm font-bold mb-2"
            htmlFor="password"
          >
            비밀번호
          </label>
          <div className="relative">
            <input
              className="bg-[#F8FAFC] rounded-md w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              id="password"
              type="password"
            />
          </div>
        </div>

        <p className="text-center text-gray-500 text-xs mt-4 mb-2">
          {isSignUp ? ("") : (
            <>
              계정이 없으신가요?{' '}
              <button
                type="button"
                className="text-blue-500 underline"
                onClick={() => setIsSignUp(true)}
              >
                회원가입하기
              </button>
            </>
          )}
        </p>

        <div className="flex items-center justify-center">
          <button
            className="bg-purple-500 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded-2xl focus:outline-none focus:shadow-outline w-1/2"
            type="button"
            onClick={handleLoginOrSignUp}
          >
            {isSignUp ? '회원가입' : '로그인'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default LoginPage;
