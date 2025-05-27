import { useState } from 'react';
import axiosInstance from '../utils/axiosInstance';
import useUserStore from '../utils/userStore';

const useVoicepackQuote = () => {
  const user = useUserStore((state) => state.user);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const createQuote = async (emotion, category, voicepackId) => {
    if (!emotion || !category || !voicepackId) {
      alert('감정, 카테고리, 그리고 음성팩 ID가 필요합니다.');
      return;
    }

    const data = {
      emotion,
      category,
      voicepackId,
    };

    const url = `/quote?userId=${user.id}`;

    try {
      setLoading(true);
      setError(null);

      const response = await axiosInstance.post(url, data, {
        headers: {
          'Content-Type': 'application/json',
        },
      });

      const rawLocation = response.headers.location;

      if (!rawLocation) {
        throw new Error("Location 헤더가 없습니다.");
      }

      const location = rawLocation.replace("http://", "https://");

      return {
        ...response.data,
        location,
      };
    } catch (err) {
      console.error('보이스팩 quote 생성 오류:', err);
      setError(err);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { createQuote, loading, error };
};

export default useVoicepackQuote;
