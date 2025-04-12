import React, { useState } from 'react';
import { useSignin } from '../hooks/useSignin';
import GradientButton from '../components/common/GradientButton';
import { useNavigate } from 'react-router-dom';

const SignIn = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { signin, loading } = useSignin();
  const navigate = useNavigate();

  const handleSignIn = () => {
    signin({ email, password });
  };

  return (
    <div className="max-w-md mx-auto p-8 space-y-6">
      <h1 className="text-2xl font-bold text-center">로그인</h1>

      <div className="space-y-4">
        <label className="block">
          <span className="text-sm text-gray-700">이메일</span>
          <input
            type="email"
            className="w-full border px-3 py-2 rounded mt-1"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </label>
        <label className="block">
          <span className="text-sm text-gray-700">비밀번호</span>
          <input
            type="password"
            className="w-full border px-3 py-2 rounded mt-1"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </label>
      </div>

      <div className="flex justify-center">
        <GradientButton
          onClick={handleSignIn}
          className="w-1/2 py-2 px-4 text-sm"
          type="button"
          disabled={loading}
        >
          {loading ? '로그인 중...' : '로그인'}
        </GradientButton>
      </div>

      <p className="text-center text-sm text-gray-600">
        계정이 없으신가요?{' '}
        <button
          onClick={() => navigate('/sign-up')}
          className="text-blue-500 underline"
        >
          회원가입하기
        </button>
      </p>
    </div>
  );
};

export default SignIn;
