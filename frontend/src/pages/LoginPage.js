import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isSignUp, setIsSignUp] = useState(location.pathname === '/signup');

  // ✅ 이메일과 비밀번호 상태
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  // isSignUp 상태 변경 시 URL 업데이트
  useEffect(() => {
    navigate(isSignUp ? '/signup' : '/login', { replace: true });
  }, [isSignUp, navigate]);

  const handleLoginOrSignUp = async () => {
    const apiUrl = process.env.REACT_APP_SIGNUP_API_URL;
    const endpoint = isSignUp ? `${apiUrl}/signup` : `${apiUrl}/login`;

    try {
      const response = await fetch(endpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
        credentials: 'include', // ✅ 세션 쿠키 포함
      });
      const textData = await response.text(); // 🔹 서버 응답을 우선 text로 읽음

      let data;
      try {
        data = JSON.parse(textData); // 🔹 JSON으로 변환 가능하면 파싱
      } catch (error) {
        console.warn('JSON 변환 실패, 원본 응답:', textData);
        data = { message: textData }; // 🔹 JSON이 아니면 그대로 사용
      }

      console.log(isSignUp ? '회원가입 응답:' : '로그인 응답:', data);

      if (response.ok) {
        if (isSignUp) {
          alert('회원가입이 완료되었습니다! 로그인해주세요.');
          setIsSignUp(false);
        } else {
          alert(data.message || '로그인 성공!');
          navigate('/landingpage');
        }

        setEmail('');
        setPassword('');
      } else {
        alert(`${isSignUp ? '회원가입' : '로그인'} 실패: ${data.message || '알 수 없는 오류 발생'}`);
      }
    } catch (error) {
      console.error(`${isSignUp ? '회원가입' : '로그인'} 오류:`, error);
      alert(`${isSignUp ? '회원가입' : '로그인'} 중 오류가 발생했습니다.`);
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
