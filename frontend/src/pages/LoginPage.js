import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isSignUp, setIsSignUp] = useState(location.pathname === '/signup'); // URL 기반 초기값 설정

  // ✅ 이메일과 비밀번호 상태
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  // isSignUp 상태 변경 시 URL 업데이트
  useEffect(() => {
    if (isSignUp) {
      navigate('/signup', { replace: true });
    } else {
      navigate('/login', { replace: true });
    }
  }, [isSignUp, navigate]);

  const handleLoginOrSignUp = async () => {
    if (isSignUp) {
      try {
        // ✅ 회원가입 API 요청
        const response = await fetch(process.env.REACT_APP_SIGNUP_API_URL, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ email, password }),
        });

        const data = await response.json(); // 🔹 서버 응답 JSON 변환
        console.log('회원가입 응답:', data); // ✅ 서버 응답 콘솔 출력

        if (response.ok) {
          alert('회원가입이 완료되었습니다!');
          setIsSignUp(false); // 회원가입 후 로그인 화면으로 변경
          setEmail(''); // ✅ 이메일 초기화
          setPassword(''); // ✅ 비밀번호 초기화
        } else {
          alert(`회원가입 실패: ${data.message || '알 수 없는 오류 발생'}`);
        }
      } catch (error) {
        console.error('회원가입 오류:', error);
        alert('회원가입 중 오류가 발생했습니다.');
      }
    } else {
      // ✅ 로그인 로직 (필요하면 API 요청 추가 가능)
      navigate('/landingpage'); // 로그인 성공 시 이동할 페이지
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen">
      <h1 className="text-3xl font-bold absolute top-40">
        {isSignUp ? '회원가입' : '로그인'}
      </h1>

      <form className="w-full max-w-sm">
        <div className="mb-4">
          <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="email">
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
          <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="password">
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
          {!isSignUp && (
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
