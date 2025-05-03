import { useCallback } from 'react';
import axiosInstance from '../utils/axiosInstance';
import useUserStore from '../utils/userStore';

const useVoicepackDetail = () => {
  const user = useUserStore((state) => state.user);
  const getVoicepackAudio = useCallback(async (voicepackId) => {
    const res = await axiosInstance.get(`/voicepack/example/${voicepackId}`, {
      responseType: 'text', // 오디오 URL이 text로 오기 때문에 지정
    });

    return res.data;
  }, []);

  const makePublic = async (voicepackId) => {
    const res = await axiosInstance.patch(
      `/voicepack/${voicepackId}?userId=${user.id}`,
      {
        isPublic: true,
      }
    );
    return res.data;
  };


  return { getVoicepackAudio,makePublic };
};

export default useVoicepackDetail;
