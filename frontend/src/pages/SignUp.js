import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import GradientButton from '../components/common/GradientButton';
import axiosInstance from '../utils/axiosInstance';
import { Eye, EyeOff } from 'lucide-react';

const SignUp = () => {
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showPasswordConfirm, setShowPasswordConfirm] = useState(false);

  useEffect(() => {
    const handlePopState = () => {
      navigate('/sign-in', { replace: true });
    };
    window.addEventListener('popstate', handlePopState);
    return () => {
      window.removeEventListener('popstate', handlePopState);
    };
  }, [navigate]);

  const handleSignUp = async () => {
    if (password !== passwordConfirm) {
      alert('비밀번호가 일치하지 않습니다.');
      return;
    }

    try {
      const response = await axiosInstance.post('/users/signup', {
        email,
        password,
      });

      const data = response.data;

      alert(data.message || '회원가입이 완료되었습니다. 로그인해주세요.');
      navigate('/sign-in');
    } catch (error) {
      console.error('회원가입 오류:', error);
      const message =
        error.response?.data?.message || '회원가입 중 오류가 발생했습니다.';
      alert(`회원가입 실패: ${message}`);
    }
  };

  return (
    <div className="max-w-md mx-auto p-8 space-y-6">
      <h1 className="text-2xl font-bold text-center">회원가입</h1>

      <div className="space-y-4">
        <label className="block">
          <span className="text-sm text-gray-700">이메일</span>
          <input
            type="text"
            className="w-full px-3 py-2 mt-1 border-none rounded-md bg-slate-50 focus:outline-none focus:ring-1 focus:ring-indigo-400"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </label>

        <label className="block relative">
          <span className="text-sm text-gray-700">비밀번호</span>
          <input
            type={showPassword ? 'text' : 'password'}
            className="w-full px-3 py-2 mt-1 border-none rounded-md bg-slate-50 focus:outline-none focus:ring-1 focus:ring-indigo-400 pr-10"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <button
            type="button"
            onClick={() => setShowPassword((prev) => !prev)}
            className="absolute top-10 right-3 text-gray-500"
          >
            {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
          </button>
        </label>

        <label className="block relative">
          <span className="text-sm text-gray-700">비밀번호 확인</span>
          <input
            type={showPasswordConfirm ? 'text' : 'password'}
            className="w-full px-3 py-2 mt-1 border-none rounded-md bg-slate-50 focus:outline-none focus:ring-1 focus:ring-indigo-400 pr-10"
            value={passwordConfirm}
            onChange={(e) => setPasswordConfirm(e.target.value)}
          />
          <button
            type="button"
            onClick={() => setShowPasswordConfirm((prev) => !prev)}
            className="absolute top-10 right-3 text-gray-500"
          >
            {showPasswordConfirm ? <EyeOff size={18} /> : <Eye size={18} />}
          </button>
        </label>
      </div>

      <div className="flex justify-center">
        <GradientButton
          onClick={handleSignUp}
          className="w-1/2 py-2 px-4 text-sm"
          type="button"
        >
          회원가입
        </GradientButton>
      </div>

      <p className="text-center text-sm text-gray-600">
        이미 계정이 있으신가요?{' '}
        <button
          onClick={() => navigate('/sign-in')}
          className="text-indigo-400 underline font-semibold"
        >
          로그인
        </button>
      </p>
    </div>
  );
};

export default SignUp;
