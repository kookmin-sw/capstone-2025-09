import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const SignUpPage = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  // ✅ 뒤로가기하면 로그인 페이지로 이동
  useEffect(() => {
    const handlePopState = () => {
      navigate('/login', { replace: true });
    };

    window.addEventListener('popstate', handlePopState);
    return () => {
      window.removeEventListener('popstate', handlePopState);
    };
  }, [navigate]);

  const handleSignUp = async () => {
    const apiUrl = process.env.REACT_APP_SIGNUP_API_URL;
    const endpoint = `${apiUrl}/signup`;

    try {
      const response = await fetch(endpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
        credentials: 'include',
      });
      const textData = await response.text();
      
      let data;
      try {
        data = JSON.parse(textData);
      } catch (error) {
        console.warn('JSON 변환 실패, 원본 응답:', textData);
        data = { message: textData };
      }

      console.log('회원가입 응답:', data);

      if (response.ok) {
        alert('회원가입이 완료되었습니다! 로그인해주세요.');
        navigate('/login');
        setEmail('');
        setPassword('');
      } else {
        alert(`회원가입 실패: ${data.message || '알 수 없는 오류 발생'}`);
      }
    } catch (error) {
      console.error('회원가입 오류:', error);
      alert('회원가입 중 오류가 발생했습니다.');
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen">
      <h1 className="text-3xl font-bold absolute top-40">회원가입</h1>

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
          이미 계정이 있으신가요?{' '}
          <button
            type="button"
            className="text-blue-500 underline"
            onClick={() => navigate('/login')}
          >
            로그인하기
          </button>
        </p>

        <div className="flex items-center justify-center">
          <button
            className="bg-purple-500 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded-2xl focus:outline-none focus:shadow-outline w-1/2"
            type="button"
            onClick={handleSignUp}
          >
            회원가입
          </button>
        </div>
      </form>
    </div>
  );
};

export default SignUpPage;
