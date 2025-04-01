import React, { useState, useEffect } from "react";

const BasicVoice = () => {
  const [voiceList, setVoiceList] = useState([]);
  const [selectedVoice, setSelectedVoice] = useState("");

  useEffect(() => {
    const fetchVoiceList = async () => {
      const userId = sessionStorage.getItem("userId"); // 예: "7"
      const apiUrl = process.env.REACT_APP_VOICEPACK_API_URL;
      const endpoint = `${apiUrl}/usage-right?userId=${userId}`;

      try {
        const response = await fetch(endpoint, {
          method: "GET",
          credentials: "include", // 쿠키 등 인증 필요 시
        });

        if (!response.ok) {
          throw new Error("보이스팩 목록을 불러오지 못했습니다.");
        }

        const data = await response.json();
        setVoiceList(data); // 예: [{ id: 1, name: '차분한 목소리' }, ...]
      } catch (error) {
        console.error("❌ 오류 발생:", error);
      }
    };

    fetchVoiceList();
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
