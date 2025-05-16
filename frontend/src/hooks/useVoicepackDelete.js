import { useCallback } from 'react';
import axiosInstance from '../utils/axiosInstance';
import useUserStore from '../utils/userStore';

const useVoicepackDelete = () => {
  const user = useUserStore((state) => state.user);

  const deleteVoicepack = useCallback(
    async (voicepackId) => {
      if (!user?.id) {
        console.error('❌ 사용자 ID가 없습니다.');
        throw new Error('로그인이 필요합니다.');
      }

      try {
        const res = await axiosInstance.delete(`/voicepack/${voicepackId}`, {
          params: { userId: user.id },
        });

        if (res.status === 204) {
          return true;
        } else {
          console.warn('⚠️ 삭제 요청했지만 예상 외의 응답:', res);
          return false;
        }
      } catch (err) {
        console.error('❌ 보이스팩 삭제 실패:', err);
        throw err;
      }
    },
    [user?.id]
  );

  return { deleteVoicepack };
};

export default useVoicepackDelete;
