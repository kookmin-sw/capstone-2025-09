import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import axiosInstance from '../utils/axiosInstance';
import useUserStore from '../utils/userStore';
import { getVoicepacksByUserId } from '../api/getVoicepacks';

export const useSignin = () => {
  const navigate = useNavigate();
  const setUser = useUserStore((state) => state.setUser);
  const [loading, setLoading] = useState(false);

  const signin = async ({ email, password }) => {
    try {
      setLoading(true);

      const response = await axiosInstance.post('/users/login', {
        email,
        password,
      });

      const data = response.data;

      sessionStorage.setItem('userInfo', JSON.stringify(data));

      if (response.status === 200) {
        const voicepacks = await getVoicepacksByUserId(data);

        // 전역 상태 설정
        setUser({
          id: data,
          email: email,
          voicepacks,
        });

        alert(data.message || '로그인 성공!');
        navigate('/voice-store');
      } else {
        alert(`로그인 실패: ${data.message || '알 수 없는 오류 발생'}`);
      }
    } catch (error) {
      console.error('로그인 오류:', error);
      alert(
        `로그인 중 오류가 발생했습니다: ${error.response?.data?.message || '서버 오류'}`
      );
    } finally {
      setLoading(false);
    }
  };

  return { signin, loading };
};
