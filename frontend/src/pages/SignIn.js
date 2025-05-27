import React, { useEffect, useState } from 'react';
import { useSignin } from '../hooks/useSignin';
import GradientButton from '../components/common/GradientButton';
import { useNavigate } from 'react-router-dom';
import { Eye, EyeOff } from 'lucide-react';
import useUserStore from '../utils/userStore';

const SignIn = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const { signin, loading } = useSignin();
  const navigate = useNavigate();

  const user = useUserStore((state) => state.user);

  // 로그인 상태라면 자동 리다이렉트
  useEffect(() => {
    if (user) {
      navigate('/voice-store');
    }
  }, [user, navigate]);

  const handleSignIn = () => {
    signin({ email, password });
  };

  return (
    <div className="max-w-md mx-auto p-4 space-y-6">
      <h1 className="text-2xl font-bold text-center">로그인</h1>

      <div className="space-y-4">
        <label className="block">
          <span className="text-sm text-gray-700">이메일</span>
          <input
            type="email"
            className="w-full px-4 py-2 border-none rounded-md bg-slate-50 focus:outline-none focus:ring-1 focus:ring-indigo-400"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </label>

        <label className="block relative">
          <span className="text-sm text-gray-700">비밀번호</span>
          <input
            type={showPassword ? 'text' : 'password'}
            className="w-full px-4 py-2 mt-1 pr-10 border-none rounded-md bg-slate-50 focus:outline-none focus:ring-1 focus:ring-indigo-400"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <button
            aria-label="비밀번호 보기 버튼"
            type="button"
            onClick={() => setShowPassword((prev) => !prev)}
            className="absolute top-10 right-3 text-gray-500"
          >
            {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
          </button>
        </label>
      </div>

      <div className="flex justify-center">
        <GradientButton
          aria-label="로그인 버튼"
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
          aria-label="회원가입 버튼"
          onClick={() => navigate('/join-agreement')}
          className="text-indigo-400 underline font-semibold"
        >
          회원가입
        </button>
      </p>
    </div>
  );
};

export default SignIn;
