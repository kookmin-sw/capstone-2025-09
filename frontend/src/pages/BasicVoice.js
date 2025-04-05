import React, { useState, useEffect } from "react";
import { useNavigate } from 'react-router-dom';
import Logo from "../icons/covosLogo.svg";

const BasicVoice = () => {
  const [voiceList, setVoiceList] = useState([]);
  const [selectedVoiceId, setSelectedVoiceId] = useState("");
  const [scriptText, setScriptText] = useState("");

  const API_URL = process.env.REACT_APP_VOICEPACK_API_URL;
  const userId = sessionStorage.getItem("userId");
  const navigate = useNavigate();

  const fetchVoiceList = async () => {
    try {
      const response = await fetch(`${API_URL}/usage-right?userId=${userId}`, {
        method: "GET",
        credentials: "include",
      });

      if (!response.ok) throw new Error("보이스팩 목록을 불러오지 못했습니다.");

      const data = await response.json();
      console.log("🎙️ 가져온 보이스팩 목록:", data);
      setVoiceList(data);
    } catch (error) {
      console.error("❌ 오류 발생:", error);
    }
  };

  useEffect(() => {
    fetchVoiceList();
  }, []);

  const handleVoiceChange = (e) => setSelectedVoiceId(e.target.value);
  const handleScriptChange = (e) => setScriptText(e.target.value);

  return (
    <div className="space-y-6 mx-16">
      <div className="mt-8 mb-8 cursor-pointer" onClick={() => navigate('/landing')}>
        <img src={Logo} alt="Logo"/>
      </div>
      <h1 className="text-2xl font-bold">베이직 보이스</h1>

      <div className="space-y-2">
        <select
          value={selectedVoiceId}
          onChange={handleVoiceChange}
          className="w-full bg-gray-300 px-4 py-2 rounded"
        >
          <option value="">목소리를 선택하세요</option>
          {voiceList.map((voice) => (
            <option key={voice.voicepackId} value={voice.voicepackId}>
              {voice.voicepackName}
            </option>
          ))}
        </select>

        <textarea
          value={scriptText}
          onChange={handleScriptChange}
          placeholder="스크립트를 입력하세요."
          className="w-full h-40 p-4 bg-gray-200 rounded resize-none"
        />

        <button className="w-full bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 transition">
          변환
        </button>
      </div>

      <div className="flex gap-4 items-center">
        <div className="flex-1 bg-gray-300 px-4 py-4 rounded text-center">
          🎵 재생창 (미구현)
        </div>
        <button className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600 transition">
          다운로드
        </button>
      </div>
    </div>
  );
};

export default BasicVoice;
