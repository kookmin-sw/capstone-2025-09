import React, { useState, useEffect } from "react";

const BasicVoice = () => {
  const [voiceList, setVoiceList] = useState([]);
  const [selectedVoice, setSelectedVoice] = useState("");

  // 나중에 백엔드에서 불러올 부분
  useEffect(() => {
    // 예시 데이터, 나중에 백 주소 생기면 fetch나 axios로 교체 가능
    const dummyVoices = [
      { id: "voice1", name: "따뜻한 목소리" },
      { id: "voice2", name: "차분한 목소리" },
      { id: "voice3", name: "밝은 목소리" },
    ];
    setVoiceList(dummyVoices);
  }, []);

  return (
    <div className="space-y-6 mx-16">
      <h1 className="text-2xl font-bold">베이직 보이스</h1>

      <div className="space-y-2">
        {/* 드롭다운 메뉴 */}
        <select
          value={selectedVoice}
          onChange={(e) => setSelectedVoice(e.target.value)}
          className="w-full bg-gray-300 px-4 py-2 rounded"
        >
          <option value="">목소리를 선택하세요</option>
          {voiceList.map((voice) => (
            <option key={voice.id} value={voice.id}>
              {voice.name}
            </option>
          ))}
        </select>

        <textarea
          placeholder="스크립트를 입력하세요."
          className="w-full h-40 p-4 bg-gray-200 rounded resize-none"
        ></textarea>
        <button className="bg-gray-300 px-4 py-2 rounded">변환 버튼</button>
      </div>

      <div className="flex gap-4 items-center">
        <div className="flex-1 bg-gray-300 px-4 py-4 rounded">재생창</div>
        <button className="bg-gray-300 px-4 py-2 rounded">다운로드 버튼</button>
      </div>
    </div>
  );
};

export default BasicVoice;
