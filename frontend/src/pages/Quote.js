import React, { useState } from 'react';
import GradientButton from '../components/common/GradientButton';
import useVoicepackUsage from '../hooks/useVoicepackUsage';
import SelectBox from '../components/common/SelectBox';
import { ScaleLoader } from 'react-spinners';
import AudioPlayer from '../components/common/AudioPlayer';
import useVoicepackQuote from '../hooks/useVoicepackQuote';

const Quote = () => {
  const { voicepacks } = useVoicepackUsage();
  const { createQuote } = useVoicepackQuote();

  const [selectedVoiceId, setSelectedVoiceId] = useState('');
  const [scriptText, setScriptText] = useState('');
  const [audioUrl, setAudioUrl] = useState(null);
  const [isGenerating, setIsGenerating] = useState(false);
  const [cultureType, setCultureType] = useState('');

  const POLLING_INTERVAL = 2000;

  const voicepackOptions = voicepacks.map(({ id, name }) => ({
    label: name,
    value: id,
  }));

  const cultureOptions = [
    { label: '한국', value: 'KOREAN' },
    { label: '동양', value: 'EASTERN' },
    { label: '서양', value: 'WESTERN' },
  ];

  // location 주소로 상태 폴링
  const pollSynthesisStatus = async (statusUrl) => {
    const poll = async () => {
      try {
        const res = await fetch(statusUrl, {
          method: 'GET',
          credentials: 'include',
        });
        const result = await res.json();

        if (result.status === 'COMPLETED' && result.resultUrl) {
          const audioRes = await fetch(result.resultUrl);
          const audioBlob = await audioRes.blob();
          const audioObjectUrl = URL.createObjectURL(audioBlob);
          setAudioUrl(audioObjectUrl);
          alert('명언 생성이 완료되었습니다.');
          setIsGenerating(false);
        } else if (result.status === 'FAILED') {
          alert('명언 생성에 실패했습니다.');
          setIsGenerating(false);
        } else {
          setTimeout(poll, POLLING_INTERVAL);
        }
      } catch (err) {
        console.error('⛔ 폴링 중 에러:', err);
        alert('⚠️ 상태 확인 중 오류가 발생했습니다.');
        setIsGenerating(false);
      }
    };

    poll();
  };

  const handleSynthesize = async () => {
    if (!selectedVoiceId || !scriptText) {
      alert('보이스팩과 스크립트를 모두 입력해주세요.');
      return;
    }

    try {
      setIsGenerating(true);

      const quoteResponse = await createQuote(
        scriptText,
        cultureType,
        parseInt(selectedVoiceId)
      );
      const statusUrl = quoteResponse.location;
      pollSynthesisStatus(statusUrl);
    } catch (error) {
      console.error('❌ 음성 생성 오류:', error);
      alert('TTS 생성 중 오류가 발생했습니다.');
      setIsGenerating(false);
    }
  };

  return (
    <div className="space-y-6">
      {isGenerating && (
        <div className="absolute inset-0 bg-violet-50 bg-opacity-40 backdrop-blur-sm flex flex-col items-center justify-center z-50 rounded-xl">
          <ScaleLoader
            color="#615FFF"
            height={40}
            width={4}
            radius={2}
            margin={3}
          />
          <p className="mt-4 text-indigo-500 font-semibold text-lg animate-pulse">
            명언 생성 중...
          </p>
          <p className="mt-4 text-indigo-500 font-semibold text-lg animate-pulse">
            페이지를 벗어나면 명언 생성이 취소될 수 있어요!
          </p>
        </div>
      )}

      <h1 className="text-xl font-bold">오늘의 명언</h1>

      <div>
        <div className="flex flex-col md:flex-row mb-4 gap-4">
          <div className="w-1/4">
            <SelectBox
              label="보이스팩"
              value={selectedVoiceId}
              onChange={(value) => setSelectedVoiceId(value)}
              options={voicepackOptions}
              placeholder="보이스팩을 선택해주세요."
            />
          </div>

          <div className="w-1/4">
            <SelectBox
              label="카테고리"
              value={cultureType}
              onChange={(value) => setCultureType(value)}
              options={cultureOptions}
              placeholder="카테고리를 선택해주세요."
            />
          </div>
        </div>
        <textarea
          value={scriptText}
          onChange={(e) => setScriptText(e.target.value)}
          placeholder="오늘의 감정을 표현해보세요. 당신을 위한 한 줄 명언을 전해드립니다."
          className="w-full h-40 p-4 bg-white text-gray-600 placeholder-gray-400 rounded-md resize-none focus:outline-none focus:ring-1 focus:ring-indigo-400"
        />

        <div className="mt-4 flex justify-end">
          <GradientButton
            aria-label="생성하기 버튼"
            className="px-6 py-3 disabled:opacity-50 disabled:cursor-not-allowed"
            onClick={handleSynthesize}
            disabled={!selectedVoiceId || !scriptText || isGenerating}
          >
            {isGenerating ? '생성 중...' : '생성하기'}
          </GradientButton>
        </div>
      </div>

      {audioUrl && (
        <div className="mt-12 px-2 py-2 bg-white backdrop-blur-sm rounded-xl">
          <div className="flex items-center space-x-4 px-6 py-5">
            <div className="flex-1">
              <AudioPlayer audioUrl={audioUrl} />
            </div>
            <GradientButton
              aria-label="다운로드 버튼"
              className="px-6 py-3"
              onClick={() => {
                const fileName = prompt('저장할 파일 이름을 입력해주세요.');
                if (!fileName) return;

                const link = document.createElement('a');
                link.href = audioUrl;
                link.download = `${fileName}.mp3`;
                link.click();
              }}
            >
              다운로드
            </GradientButton>
          </div>
        </div>
      )}
    </div>
  );
};

export default Quote;
