import GradientButton from '../../components/common/GradientButton';
import axiosInstance from '../../utils/axiosInstance';
import { useState } from 'react';
import useUserStore from '../../utils/userStore';

const AssistantReadyScreen = ({ onStart, onEdit }) => {
  const [loading, setLoading] = useState(false);
  const [errorState, setErrorState] = useState(null);
  const user = useUserStore((state) => state.user);

  const handleStart = async () => {
    if (!user?.id) {
      setErrorState('no-session');
      return;
    }

    setLoading(true);
    try {
      const postRes = await axiosInstance.post(`ai-assistant/synthesis`);

      if (postRes.status !== 202 || !postRes.data?.requestId) {
        setErrorState('fail');
        setLoading(false);
        return;
      }

      const requestId = postRes.data.requestId;
      console.log('요청 ID:', requestId);
      const getRes = await axiosInstance.get(
        `ai-assistant/synthesis/status/${requestId}`
      );
      const { status, results } = getRes.data;

      if (status === 'SUCCESS') {
        const audioUrls = Object.values(results);
        localStorage.setItem(
          'assistant-result-audios',
          JSON.stringify(audioUrls)
        );
        onStart(); // 상태를 'play'로
      } else if (status === 'FAILURE' || getRes.status === 404) {
        onEdit(); // 설정화면으로
      } else {
        alert('아직 생성이 완료되지 않았습니다. 잠시 후 다시 시도해주세요.');
      }
    } catch (err) {
      if (err?.response?.status === 404) {
        onEdit();
      } else {
        setErrorState('fail');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center my-40 text-center space-y-6">
      <h1 className="text-2xl font-bold">AI 비서가 준비되었어요!</h1>
      <p className="text-gray-500 text-sm">
        설정된 정보로 오늘의 스크립트를 준비했어요.
      </p>

      <div className="flex flex-col gap-4">
        <GradientButton
          className="px-6 py-3 disabled:bg-gray-300"
          onClick={handleStart}
          disabled={loading || errorState}
        >
          {loading ? '로딩 중...' : '오늘의 비서 이용하기'}
        </GradientButton>
        <button
          onClick={onEdit}
          className="text-sm underline text-gray-500 hover:text-gray-700"
        >
          설정 수정
        </button>
      </div>
    </div>
  );
};

export default AssistantReadyScreen;
