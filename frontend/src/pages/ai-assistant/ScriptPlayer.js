import React, { useEffect, useState } from 'react';
import AudioListPlayer from '../../components/common/AudioListPlayer';

const ScriptPlayer = ({ onEdit }) => {
  const [audios, setAudios] = useState([]);
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

      const now = new Date();
      const y = now.getFullYear();
      const m = String(now.getMonth() + 1).padStart(2, '0');
      const d = String(now.getDate()).padStart(2, '0');
      const h = String(now.getHours()).padStart(2, '0');
      setReportTime(`${y}년 ${m}월 ${d}일 ${h}시 리포트`);
    }

    if (config) {
      const parsed = JSON.parse(config);
      setCategories(parsed.categories); // 인덱스 배열
    }
  }, []);

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold">AI 리포터</h1>
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
            {categories.map((i) => (
              <span
                key={i}
                className="px-3 py-1 rounded-full text-xs font-medium bg-indigo-100 text-indigo-700"
              >
                {CATEGORY_MAP[i]}
              </span>
            ))}
          </div>
        </div>
      </div>

      {audios ? (
        <div className="bg-white/80 backdrop-blur rounded-md border border-slate-200 p-4">
          {/* DOM 렌더 후 시점 보장 */}
          <div className="w-full h-auto">
            <AudioListPlayer audioUrls={audios} />
          </div>
        </div>
      ) : null}

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
