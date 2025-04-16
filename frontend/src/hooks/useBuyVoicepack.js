import { useCallback } from 'react';
import useUserStore from '../utils/userStore';
import axiosInstance from '../utils/axiosInstance';

const useBuyVoicepack = () => {
  const user = useUserStore((state) => state.user);
  const buy = useCallback(
    async (voicepackId) => {
      if (!user?.id) throw new Error('유저 정보가 없습니다.');

      const purchaseUrl = `/voicepack/usage-right?userId=${user.id}&voicepackId=${voicepackId}`;
      const res = await axiosInstance.post(purchaseUrl, null); // null은 body가 없다는 의미

      return res.data;
    },
    [user]
  );
  return { buy };
};
export default useBuyVoicepack;
