import {useState} from 'react';
import axiosInstance from '../utils/axiosInstance';

const useVoiceConvert = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const convertVoice = async (formData) => {
    const url = 'voicepack/convert';
    try {
      setLoading(true);
      setError(null);

      const response = await axiosInstance.post(url, formData, {
        headers: {
          'Content-Type': 'multipart/form-data', // 명시적으로 지정
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

  return {convertVoice, loading, error};
};

export default useVoiceConvert;
