import React from 'react';
import voicepackList from '../data/VoicepackData';

const VoiceStore = () => {
  return (
    <div>
      <h1 className="text-2xl font-bold text-center mb-4">보이스팩 구매</h1>
      <div className="flex justify-between mb-4">
        <input
          className="w-full max-w-md px-4 py-2 border rounded bg-gray-100"
          placeholder="검색창"
        />
        <button className="ml-4 text-sm">정렬</button>
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
        {voicepackList.map((voicepack) => (
          <div
            key={voicepack.voicepack_id}
            className="bg-gray-800 text-white p-4 rounded text-center"
          >
            <div className="mb-1 text-sm font-semibold">{voicepack.name}</div>
            <div className="mb-2 text-yellow-300">
              🪙 {voicepack.price.toLocaleString()}
            </div>
            <div className="flex justify-center gap-2">
              <button>▶️</button>
              <button>$</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default VoiceStore;
