import { useEffect, useState } from 'react';
import useUserStore from '../utils/userStore';
import { getVoicepacksByUserId } from '../api/getVoicepacks';

const useVoicepackUsage = (filter = 'available', refreshKey = 0) => {
  const [voicepacks, setVoicepacks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const user = useUserStore((state) => state.user);

  useEffect(() => {
    if (!user?.id) {
      setLoading(false);
      return;
    }

    const fetch = async () => {
      setLoading(true);
      try {
        const data = await getVoicepacksByUserId(user.id, filter);
        const filtered = data.filter((v) => v.isVideoBased === false);

        const sorted = [...filtered].sort(
          (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
        );
        setVoicepacks(sorted);
      } catch (err) {
        setError(err);
      } finally {
        setLoading(false);
      }
    };

    fetch();
  }, [user?.id, filter, refreshKey]);
  return { voicepacks, loading, error };
};

export default useVoicepackUsage;
