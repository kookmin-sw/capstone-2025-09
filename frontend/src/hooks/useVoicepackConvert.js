import { useState } from 'react';
import axiosInstance from '../utils/axiosInstance';

const useVoiceConvert = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const convertVoice = async (voicePackName, audioBlob, userId) => {
    if (!voicePackName || !audioBlob) {
      alert('보이스팩 이름과 녹음 파일이 필요합니다.');
      return;
    }

    const formData = new FormData();
    formData.append('userId', 7);
    formData.append('name', voicePackName);
    formData.append('voiceFile', new File([audioBlob], 'voice.wav', { type: 'audio/wav' }));
    console.log('🔼 전송할 formData', formData.get('name'), formData.get('userId'));

    const url = 'voicepack/convert';
    console.log('🌐 전송할 API URL:', axiosInstance.defaults.baseURL + url);


    try {
      setLoading(true);
      setError(null);

      const response = await axiosInstance.post('voicepack/convert', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        withCredentials: true, // 쿠키 기반 인증이 필요할 경우
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
