import React, { useEffect, useState } from 'react';
import AudioPlayer from '../../components/common/AudioPlayer';

const ScriptPlayer = ({ onEdit }) => {
  const [audios, setAudios] = useState([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [categories, setCategories] = useState([]);
  const [reportTime, setReportTime] = useState(null);

  const CATEGORY_MAP = ['BBC 뉴스', 'GOOGLE 뉴스', 'IT 소식'];

  useEffect(() => {
    const audioList = localStorage.getItem('assistant-result-audios');
    const config = localStorage.getItem('ai-assistant-config');
    console.log(audioList);
    if (audioList) {
      const parsed = JSON.parse(audioList);
      setAudios(parsed);

      // URL에서 날짜 추출: e.g. .../2025041211/...
      const match = parsed[0]?.match(/\/([0-9]{10})\//);
      if (match && match[1]) {
        const raw = match[1]; // e.g. "2025041211"
        const y = raw.substring(0, 4);
        const m = raw.substring(4, 6);
        const d = raw.substring(6, 8);
        const h = raw.substring(8, 10);
        setReportTime(`${y}년 ${m}월 ${d}일 ${h}시 리포트`);
      }
    }

    if (config) {
      const parsed = JSON.parse(config);
      const mapped = parsed.categories.map((i) => CATEGORY_MAP[i]);
      setCategories(mapped);
    }
  }, []);

  const handleAudioEnd = () => {
    if (currentIndex < audios.length - 1) {
      setCurrentIndex((prev) => prev + 1);
    }
  };

  const currentUrl = audios[currentIndex];

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-xl font-semibold">AI 리포터</h1>
      </div>
      <p className="text-sm text-slate-500 mt-1">
        오늘의 뉴스 요약 리포트를 전달해드립니다.
      </p>
      <div className="text-sm text-slate-600 space-y-2">
        {reportTime && (
          <p className="flex items-center gap-1">
            🕒 <span className="font-medium text-slate-700">{reportTime}</span>
          </p>
        )}
        <div className="flex flex-row gap-2 items-center">
          <p className="flex items-center gap-1 font-medium text-slate-700">
            📰 <span>선택한 카테고리</span>
          </p>
          <div className="flex gap-2 flex-wrap">
            {categories.map((name, index) => (
              <span
                key={index}
                className="px-3 py-1 rounded-full text-xs font-medium bg-indigo-100 text-indigo-700"
              >
                {name}
              </span>
            ))}
          </div>
        </div>
      </div>

      {currentUrl ? (
        <div className="bg-white/80 backdrop-blur rounded-md border border-slate-200 p-4">
          <AudioPlayer audioUrl={currentUrl} onEnd={handleAudioEnd} />
        </div>
      ) : (
        <p className="text-center text-red-500">오디오를 불러올 수 없습니다.</p>
      )}

      <div className="flex justify-center">
        <button
          onClick={onEdit}
          className="text-sm underline text-slate-500 hover:text-slate-700"
        >
          세팅 다시 하기
        </button>
      </div>
    </div>
  );
};

export default ScriptPlayer;
