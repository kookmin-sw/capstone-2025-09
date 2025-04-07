import React, { useState, useEffect } from "react";
import { useNavigate } from 'react-router-dom';
import Logo from "../icons/covosLogo.svg";

const BasicVoice = () => {
  const [voiceList, setVoiceList] = useState([]);
  const [selectedVoiceId, setSelectedVoiceId] = useState("");
  const [scriptText, setScriptText] = useState("");
  const [audioUrl, setAudioUrl] = useState(null);

  const API_URL = process.env.REACT_APP_VOICEPACK_API_URL;
  const userId = sessionStorage.getItem("userId");
  const navigate = useNavigate();

  const POLLING_INTERVAL = 2000;

  // 🔸 보이스 목록 불러오기
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

  // 🔁 폴링 함수 (무제한 대기)
  const pollSynthesisStatus = async (statusUrl) => {
    const poll = async () => {
      try {
        const res = await fetch(statusUrl, {
          method: "GET",
          credentials: "include",
        });

        const result = await res.json();
        console.log("📡 폴링 결과:", result);

        if (result.status === "COMPLETED" && result.resultUrl) {
          const audioRes = await fetch(result.resultUrl);
          const audioBlob = await audioRes.blob();
          const audioObjectUrl = URL.createObjectURL(audioBlob);

          console.log("🔗 생성된 오디오 URL:", audioObjectUrl);
          setAudioUrl(audioObjectUrl);
          alert("✅ 음성 생성이 완료되었습니다!");
        } else if (result.status === "FAILED") {
          alert("❌ 음성 생성에 실패했습니다.");
        } else {
          setTimeout(poll, POLLING_INTERVAL);
        }
      } catch (err) {
        console.error("⛔ 폴링 중 에러:", err);
        alert("⚠️ 상태 확인 중 오류가 발생했습니다.");
      }
    };

    poll();
  };

  // 🔸 변환 버튼
  const handleSynthesize = async () => {
    if (!selectedVoiceId || !scriptText) {
      alert("보이스팩과 스크립트를 모두 입력해주세요.");
      return;
    }

    try {
      const response = await fetch(
        `${API_URL}/synthesis?userId=${userId}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          credentials: "include",
          body: JSON.stringify({
            voicepackId: parseInt(selectedVoiceId),
            prompt: scriptText,
          }),
        }
      );

      if (!response.ok) throw new Error("TTS 생성 요청에 실패했습니다.");

      let locationHeader = response.headers.get("Location");
      if (locationHeader?.startsWith("http://")) {
        locationHeader = locationHeader.replace("http://", "https://");
      }
      console.log("📍 Location Header:", locationHeader);

      if (locationHeader) {
        alert("TTS 요청이 접수되었습니다. 생성 중...");
        pollSynthesisStatus(locationHeader);
      }
    } catch (error) {
      console.error("❌ 변환 중 오류 발생:", error);
      alert("TTS 생성 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="space-y-6 mx-16">
      <div className="mt-8 mb-8 cursor-pointer" onClick={() => navigate('/landing')}>
        <img src={Logo} alt="Logo" />
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

        <button
          onClick={handleSynthesize}
          className="w-full bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 transition"
        >
          변환
        </button>
      </div>

      <div className="flex gap-4 items-center">
        <div className="flex-1 bg-gray-300 px-4 py-4 rounded text-center">
          {audioUrl && typeof audioUrl === "string" ? (
            <audio controls src={audioUrl} className="w-full" />
          ) : (
            "🎵 재생창 (준비 중)"
          )}
        </div>
        <button
          className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600 transition"
          onClick={() => {
            if (audioUrl) {
              const link = document.createElement("a");
              link.href = audioUrl;
              link.download = "voice.mp3";
              link.click();
            } else {
              alert("아직 음성이 준비되지 않았습니다.");
            }
          }}
        >
          다운로드
        </button>
      </div>
    </div>
  );
};

export default BasicVoice;
