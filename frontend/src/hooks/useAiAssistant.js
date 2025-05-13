import { useCallback } from 'react';
import axiosInstance from '../utils/axiosInstance';
import useUserStore from '../utils/userStore';

const useAiAssistant = () => {
  const user = useUserStore((state) => state.user);

  // 1. 음성 합성 요청
  const requestSynthesis = useCallback(async () => {
    try {
      const res = await axiosInstance.post('/ai-assistant/synthesis');
      return res.data; // ex: { requestId: 123 }
    } catch (error) {
      console.error('음성 합성 요청 실패:', error);
      throw error;
    }
  }, []);

  // 2. 합성 상태 확인
  const checkSynthesisStatus = useCallback(async (requestId) => {
    try {
      const res = await axiosInstance.get(
        `/ai-assistant/synthesis/status/${requestId}`
      );
      return res.data; // ex: { status: 'PROCESSING', results: { ... } }
    } catch (error) {
      console.error('합성 상태 확인 실패:', error);
      throw error;
    }
  }, []);

  return { requestSynthesis, checkSynthesisStatus };
};

export default useAiAssistant;
