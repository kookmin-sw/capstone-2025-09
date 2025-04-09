// hooks/useVoicepackUsage.js
import { useEffect, useState } from 'react';
import axiosInstance from '../utils/axiosInstance'; // 실제 axios 인스턴스 경로에 맞게 수정

const useVoicepackUsage = () => {
  const [voicepacks, setVoicepacks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchVoicepacks = async () => {
      try {
        const response = await axiosInstance.get('voicepack/usage-right', {
          params: { userId: 7 },
        });
        setVoicepacks(response.data);
      } catch (err) {
        setError(err);
        console.error('보이스팩 불러오기 실패:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchVoicepacks();
  }, []);

  return { voicepacks, loading, error };
};

export default useVoicepackUsage;
