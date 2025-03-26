import React from 'react';

const VoiceCreate = () => {
  return (
    <div className="max-w-xl mx-auto">
      <h1 className="text-2xl font-bold text-center mb-8">보이스팩 생성</h1>

      <label className="block font-medium mb-2">
        보이스팩 이름 <span className="text-red-500">*</span>
      </label>
      <input
        type="text"
        className="w-full border border-gray-300 rounded px-3 py-2 mb-6"
        placeholder="Text"
      />

      <label className="block font-medium mb-2">
        보이스팩 샘플 녹음 <span className="text-red-500">*</span>
      </label>
      <p className="text-sm text-gray-500 mb-4">
        녹음 버튼을 누르고 각 문장을 따라 읽어주세요.
      </p>
      <p className="mb-2">
        1. 안녕하세요. 목소리를 제공합니다. 잘 들리시나요? 감사합니다.
      </p>

      <div className="flex items-center space-x-2 mb-8">
        <button className="p-2 bg-white border rounded-full">🎤</button>
        <div className="flex-1 bg-gray-200 rounded-full h-10 flex items-center justify-center">
          <span className="text-sm"></span>
        </div>
      </div>

      <button className="block mx-auto bg-gradient-to-r from-purple-400 to-purple-600 text-white font-bold px-6 py-2 rounded-full">
        생성
      </button>
    </div>
  );
};

export default VoiceCreate;
