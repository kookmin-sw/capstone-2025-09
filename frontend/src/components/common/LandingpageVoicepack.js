import React from 'react';
import { Play, Pause } from 'lucide-react';

const LandingpageVoicepack = ({
                                name = '보이스팩 이름',
                                duration = '00:16 / 01:00',
                              }) => {
  return (
    <div className="w-48 h-64 bg-white rounded-2xl flex flex-col items-center justify-between px-4 py-6 transition-transform">
      {/* 썸네일 영역 */}
      <div className="w-24 h-24 bg-gradient-to-br from-indigo-500 to-purple-500 rounded-full flex items-center justify-center mb-4">
        <div className="w-10 h-10 bg-white rounded-full" />
      </div>

      {/* 보이스팩 이름 */}
      <p className="text-base font-semibold text-center text-gray-800">
        {name}
      </p>

      {/* 플레이어 */}
      <div className="w-full mt-4">
        {/* 진행 바 */}
        <div className="w-full h-1 bg-gray-300 rounded-full mb-3">
          <div className="h-full w-2/5 bg-indigo-500 rounded-full"></div>
        </div>

        {/* 컨트롤러 */}
        <div className="flex items-center justify-between text-indigo-500">
          <button className="p-1 rounded">
            <Play size={18} />
          </button>
          <p className="text-xs text-gray-500">{duration}</p>
          <button className="p-1 rounded">
            <Pause size={18} />
          </button>
        </div>
      </div>
    </div>
  );
};

export default LandingpageVoicepack;
