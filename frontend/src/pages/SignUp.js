import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import GradientButton from '../components/common/GradientButton';
import axiosInstance from '../utils/axiosInstance';
import { Eye, EyeOff } from 'lucide-react';
import { User } from 'lucide-react';


const SignUp = () => {
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [name, setName] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [profileImage, setProfileImage] = useState(null);
  const [imagePreview, setImagePreview] = useState(null); // 미리보기용 상태
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

  useEffect(() => {
    const agreed = sessionStorage.getItem('agreedToTerms');
    if (agreed !== 'true') {
      alert('약관에 동의하셔야 회원가입이 가능합니다.');
      navigate('/join-agreement');
    }
  }, [navigate]);

  const handleSignUp = async () => {
    if (!email || !name || !password) {
      alert('이메일, 닉네임, 비밀번호를 모두 입력해주세요.');
      return;
    }

    if (password !== passwordConfirm) {
      alert('비밀번호가 일치하지 않습니다.');
      return;
    }

    try {
      const formData = new FormData();
      formData.append('email', email);
      formData.append('name', name);
      formData.append('password', password);

      if (profileImage) {
        formData.append('profileImage', profileImage);
      } else {
        formData.append('profileImage', '');
      }

      const response = await axiosInstance.post('/users/signup', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      const data = response.data;
      alert(data.message || '회원가입이 완료되었습니다. 로그인해주세요.');
      sessionStorage.removeItem('agreedToTerms');
      navigate('/sign-in');
    } catch (error) {
      console.error('회원가입 오류:', error);
      const message =
        error.response?.data?.message || '회원가입 중 오류가 발생했습니다.';
      alert(`회원가입 실패: ${message}`);
    }
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setProfileImage(file);
      setImagePreview(URL.createObjectURL(file));
    } else {
      setProfileImage(null);
      setImagePreview(null);
    }
  };

  return (
    <div className="max-w-md mx-auto p-4 space-y-6">
      <h1 className="text-2xl font-bold text-center">회원가입</h1>
      <div className="space-y-2">
        <div className="block text-center">
          {imagePreview ? (
            <img
              src={imagePreview}
              alt="프로필 미리보기"
              className="w-24 h-24 mx-auto rounded-full object-cover border border-gray-100 mb-3"
            />
          ) : (
            <div className="w-24 h-24 mx-auto rounded-full bg-slate-50 flex items-center justify-center border mb-3">
              <User className="w-10 h-10 text-gray-400"/>
            </div>
          )}
          <label
            htmlFor="imageUpload"
            className="inline-block px-4 py-1 bg-gradient-to-r from-violet-400 to-indigo-500 text-white rounded-md cursor-pointer hover:opacity-70 transition"
          >
            파일 선택
          </label>

          {!imagePreview && (
            <p className="text-xs text-gray-500 mt-2">
              이미지를 선택하지 않으면 기본 프로필 이미지가 사용됩니다.
            </p>
          )}

          <input
            id="imageUpload"
            type="file"
            accept="image/*"
            onChange={handleImageChange}
            className="hidden"
          />
        </div>

        <label className="block">
          <span className="text-sm text-gray-700">이메일</span>
          <input
            type="text"
            className="w-full px-3 py-2 mt-1 border-none rounded-md bg-slate-50 focus:outline-none focus:ring-1 focus:ring-indigo-400"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </label>

        <label className="block">
          <span className="text-sm text-gray-700">닉네임</span>
          <input
            type="text"
            className="w-full px-3 py-2 mt-1 border-none rounded-md bg-slate-50 focus:outline-none focus:ring-1 focus:ring-indigo-400"
            value={name}
            onChange={(e) => setName(e.target.value)}
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
            {showPassword ? <EyeOff size={18}/> : <Eye size={18}/>}
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
            {showPasswordConfirm ? <EyeOff size={18}/> : <Eye size={18}/>}
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
