import React, { useMemo } from 'react';
import { Play, Pause } from 'lucide-react';

const getRandomDuration = () => {
  const totalSec = Math.floor(Math.random() * 40) + 20;
  const currentSec = Math.floor(Math.random() * totalSec);
  const format = (sec) =>
    `${String(Math.floor(sec / 60)).padStart(2, '0')}:${String(sec % 60).padStart(2, '0')}`;
  return {
    current: format(currentSec),
    total: format(totalSec),
    ratio: currentSec / totalSec,
  };
};
const LandingpageVoicepack = ({ name = '보이스팩 이름', gradient }) => {
  const { current, total, ratio } = useMemo(() => getRandomDuration(), []);

  return (
    <div className="w-48 h-64 bg-white rounded-2xl flex flex-col items-center justify-between px-4 py-6 transition-transform">
      <div
        className={`w-24 h-24 bg-gradient-to-br ${gradient} rounded-full flex items-center justify-center mb-4`}
      >
        <div className="w-10 h-10 bg-white rounded-full" />
      </div>
      <p className="text-base font-semibold text-center text-gray-800">
        {name}
      </p>
      <div className="w-full mt-4">
        <div className="w-full h-1 bg-gray-300 rounded-full mb-3">
          <div
            className="h-full bg-indigo-500 rounded-full"
            style={{ width: `${Math.floor(ratio * 100)}%` }}
          />
        </div>
        <div className="flex items-center justify-between text-indigo-500">
          <button className="p-1 rounded">
            <Play size={18} />
          </button>
          <p className="text-xs text-gray-500">
            {current} / {total}
          </p>
          <button className="p-1 rounded">
            <Pause size={18} />
          </button>
        </div>
      </div>
    </div>
  );
};

export default LandingpageVoicepack;
