import { useCallback } from 'react';
import axiosInstance from '../utils/axiosInstance';

const useVoicepackDetail = () => {
  const getVoicepackAudio = useCallback(async (voicepackId) => {
    const res = await axiosInstance.get(`/voicepack/example/${voicepackId}`, {
      responseType: 'text', // 오디오 URL이 text로 오기 때문에 지정
    });

    return res.data;
  }, []);

  return { getVoicepackAudio };
};

export default useVoicepackDetail;
