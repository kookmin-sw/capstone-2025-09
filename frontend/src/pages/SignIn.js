import React, { useState } from 'react';
import { useSignin } from '../hooks/useSignin';
import GradientButton from '../components/common/GradientButton';

const SignIn = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { signin, loading } = useSignin();

  const handleSignin = () => {
    signin({ email, password });
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen">
      <h1 className="text-3xl font-bold absolute top-40">로그인</h1>

      <form className="w-full max-w-sm" onSubmit={(e) => e.preventDefault()}>
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
          <input
            className="bg-[#F8FAFC] rounded-md w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>

        <p className="text-center text-gray-500 text-xs mt-4 mb-2">
          계정이 없으신가요?{' '}
          <button
            type="button"
            className="text-blue-500 underline"
            onClick={() => (window.location.href = '/sign-up')}
          >
            회원가입하기
          </button>
        </p>

        <div className="flex items-center justify-center">
          <GradientButton
            onClick={handleSignin}
            className="w-1/2 py-2 px-4 text-sm"
            type="button"
            disabled={loading}
          >
            {loading ? '로그인 중...' : '로그인'}
          </GradientButton>
        </div>
      </form>
    </div>
  );
};

export default SignIn;
