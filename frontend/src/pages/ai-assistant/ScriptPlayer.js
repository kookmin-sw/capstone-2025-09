// src/pages/ai-assistant/ScriptPlayer.js
import React from 'react';
import GradientButton from '../../components/common/GradientButton';

const ScriptPlayer = ({ onEdit }) => {
  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-xl font-semibold">AI 비서</h1>
        <GradientButton onClick={onEdit} className="px-4 py-2 text-sm">
          수정하기
        </GradientButton>
      </div>

      {/* 오디오 스크립트 */}
      <div className="bg-white p-6 rounded-lg shadow-md space-y-4">
        <p className="text-sm">
          안녕하세요. 오늘 0월 0일 0요일 날씨는 맑음입니다. 오늘 주요 뉴스는 ~~
          입니다.
        </p>
        <div className="flex items-center justify-between border-t pt-4">
          <div className="flex items-center gap-2">
            <button>⏮️</button>
            <button>▶️</button>
            <button>⏭️</button>
          </div>
          <span className="text-sm text-blue-500">00:16:00</span>
        </div>
      </div>

      {/* 출처 리스트 */}
      <div>
        <p className="text-sm font-medium mb-2">정보 출처</p>
        <ul className="space-y-2">
          {[1, 2].map((i) => (
            <li
              key={i}
              className="bg-[#F8F5FF] rounded-md px-4 py-3 text-sm flex items-center justify-between"
            >
              <span className="font-medium text-gray-700">출처 {i}</span>
              <a
                href="https://www.link.com"
                className="text-blue-600 underline break-all ml-2"
                target="_blank"
                rel="noopener noreferrer"
              >
                www.link.com/Lorem ipsum dolor sit amet consectetur.
              </a>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
};

export default ScriptPlayer;
