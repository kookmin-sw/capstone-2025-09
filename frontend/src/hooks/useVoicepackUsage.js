import { useEffect, useState } from 'react';
import useUserStore from '../utils/userStore';
import { getVoicepacksByUserId } from '../api/getVoicepacks';

const useVoicepackUsage = () => {
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
      try {
        const data = await getVoicepacksByUserId(user.id);
        setVoicepacks(data);
      } catch (err) {
        setError(err);
      } finally {
        setLoading(false);
      }
    };

    fetch();
  }, [user?.id]);

  return { voicepacks, loading, error };
};

export default useVoicepackUsage;
