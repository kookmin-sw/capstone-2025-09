import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const LoginPage = () => {
  const navigate = useNavigate();
  const [isSignUp, setIsSignUp] = useState(false); // 로그인 모드인지 회원가입 모드인지 확인하는 상태
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleLoginOrSignUp = async () => {
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    if (isSignUp) {
      // 회원가입 요청
      try {
        const response = await fetch(process.env.REACT_APP_SIGNUP_API_URL, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ email, password }),
        });

        const data = await response.json(); // 응답 데이터 파싱
        console.log('Sign Up Response:', data); // 응답 데이터 콘솔 출력

        if (response.ok) {
          alert('회원가입이 완료되었습니다!');
          setEmail('');
          setPassword('');
          setIsSignUp(false); // 회원가입 후 다시 로그인 화면으로 이동
        } else {
          alert(`회원가입에 실패했습니다: ${data.message || 'Unknown error'}`);
        }
      } catch (error) {
        console.error('Error during sign up:', error);
        alert('회원가입 중 오류가 발생했습니다.');
      }
    } else {
      // 로그인 모드일 때 랜딩 페이지로 네비게이트
      navigate('/landingpage', { replace: true });
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
            value={email}
            onChange={(e) => setEmail(e.target.value)}
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
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
        </div>

        <p className="text-center text-gray-500 text-xs mt-4 mb-2">
          {isSignUp ? (
            ''
          ) : (
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
