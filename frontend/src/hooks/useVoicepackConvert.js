import { useState } from 'react';
import axiosInstance from '../utils/axiosInstance';
import useUserStore from '../utils/userStore';

const useVoiceConvert = () => {
  const user = useUserStore((state) => state.user);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const convertVoice = async (voicePackName, audioBlob, userId) => {
    if (!voicePackName || !audioBlob) {
      alert('보이스팩 이름과 녹음 파일이 필요합니다.');
      return;
    }

    const formData = new FormData();
    formData.append('userId', user.userId); // ✅ userId를 동적으로 받도록 수정
    formData.append('name', voicePackName);
    formData.append(
      'voiceFile',
      new File([audioBlob], 'voice.wav', { type: 'audio/wav' })
    );

    const url = 'voicepack/convert';

    try {
      setLoading(true);
      setError(null);

      const response = await axiosInstance.post(url, formData, {
        headers: {
          'Content-Type': undefined, // axios가 자동으로 설정하게 함
        },
      });
      return response.data;
    } catch (err) {
      console.error('보이스팩 변환 오류:', err);
      setError(err);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { convertVoice, loading, error };
};

export default useVoiceConvert;
