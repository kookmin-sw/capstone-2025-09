// hooks/useFetchUserInfo.js
import { useEffect } from 'react';
import { fetchUserInfo } from '../api/user';
import useUserStore from '../utils/userStore';

const useFetchUserInfo = (userId) => {
  const setUser = useUserStore((state) => state.setUser);

  useEffect(() => {
    if (!userId) return;
    const fetchData = async () => {
      try {
        const userData = await fetchUserInfo(userId);
        setUser(userData);
      } catch (err) {
        console.error('유저 정보 불러오기 실패:', err);
      }
    };

    fetchData();
  }, [userId, setUser]);
};

export default useFetchUserInfo;
