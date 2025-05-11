import GradientButton from '../../components/common/GradientButton';
import axiosInstance from '../../utils/axiosInstance';
import React, { useState } from 'react';
import useUserStore from '../../utils/userStore';
import { ScaleLoader } from 'react-spinners';

const AssistantReadyScreen = ({ onStart, onEdit }) => {
  const [loading, setLoading] = useState(false);
  const [errorState, setErrorState] = useState(null);
  const [isPolling, setIsPolling] = useState(false);
  const user = useUserStore((state) => state.user);

  const getTitle = () => {
    if (isPolling) return 'AI 리포터가 뉴스를 준비 중입니다';
    if (loading) return '요청을 전송하는 중입니다';
    return 'AI 리포터가 리포트를 준비했어요!';
  };

  const handleStart = async () => {
    if (!user?.id) {
      setErrorState('no-session');
      return;
    }

    setLoading(true);
    try {
      let requestId = localStorage.getItem('assistant-request-id');

      if (!requestId) {
        const postRes = await axiosInstance.post('ai-assistant/synthesis');
        if (postRes.status !== 202 || !postRes.data?.requestId) {
          setErrorState('fail');
          setLoading(false);
          return;
        }
        requestId = postRes.data.requestId;
        localStorage.setItem('assistant-request-id', requestId);
      }

      setIsPolling(true);

      const pollStatus = async () => {
        try {
          console.log(`[Polling] 요청 ID: ${requestId} → 상태 확인 요청 중...`);
          const res = await axiosInstance.get(
            `ai-assistant/synthesis/status/${requestId}`
          );
          const { status, results } = res.data;

          console.log(`[Polling] 상태 응답: ${status}`);

          if (status === 'SUCCESS') {
            const audioUrls = Object.values(results);
            localStorage.setItem(
              'assistant-result-audios',
              JSON.stringify(audioUrls)
            );
            localStorage.removeItem('assistant-request-id');
            setIsPolling(false);
            onStart();
          } else if (status === 'FAILURE') {
            localStorage.removeItem('assistant-request-id');
            setIsPolling(false);
            onEdit();
          } else {
            setTimeout(pollStatus, 3000); // 다시 시도
          }
        } catch (err) {
          console.error(`[Polling] 오류 발생:`, err);
          if (err?.response?.status === 404) {
            localStorage.removeItem('assistant-request-id');
            setIsPolling(false);
            onEdit();
          } else {
            setErrorState('fail');
            setIsPolling(false);
          }
        }
      };

      pollStatus();
    } catch (err) {
      setErrorState('fail');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center my-40 text-center space-y-6">
      <h1 className="text-2xl font-bold text-slate-800">{getTitle()}</h1>
      <p className="text-gray-500 text-sm">
        설정한 카테고리와 문체를 기반으로 AI 리포터가 뉴스를 요약해드립니다.
      </p>

      {(loading || isPolling) && (
        <div className="absolute inset-0 bg-indigo-50 bg-opacity-30 backdrop-blur-sm flex flex-col items-center justify-center z-50 rounded-xl">
          <ScaleLoader
            color="#4F46E5"
            height={40}
            width={4}
            radius={2}
            margin={3}
          />
          <p className="mt-4 text-indigo-600 font-semibold text-lg animate-pulse">
            AI 리포터가 리포트를 준비하고 있어요...
          </p>
          <p className="mt-2 text-sm text-indigo-500 animate-pulse">
            최신 뉴스를 정리 중입니다. 잠시만 기다려주세요.
          </p>
        </div>
      )}

      <div className="flex flex-col gap-4">
        <GradientButton
          className="px-6 py-3"
          onClick={handleStart}
          disabled={loading || errorState || isPolling}
        >
          {loading
            ? '요청 중...'
            : isPolling
              ? '생성 중...'
              : '오늘의 리포트 듣기'}
        </GradientButton>
        <button
          onClick={onEdit}
          className="text-sm underline text-slate-500 hover:text-slate-700"
        >
          설정 다시 하기
        </button>
      </div>
    </div>
  );
};

export default AssistantReadyScreen;
