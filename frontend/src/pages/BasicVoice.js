import React, {useState} from "react";
import GradientButton from "../components/common/GradientButton";
import useVoicepackUsage from "../hooks/useVoicepackUsage";
import SelectBox from "../components/common/SelectBox";
import useVoicepackSynthesis from "../hooks/useVoicepackSynthesis";
import {ScaleLoader} from "react-spinners";
import AudioPlayer from "../components/common/AudioPlayer";

const BasicVoice = () => {
  const {voicepacks} = useVoicepackUsage();
  const {synthesize} = useVoicepackSynthesis();

  const [selectedVoiceId, setSelectedVoiceId] = useState("");
  const [scriptText, setScriptText] = useState("");
  const [audioUrl, setAudioUrl] = useState(null);
  const [isGenerating, setIsGenerating] = useState(false);
  const [emotionType, setEmotionType] = useState(0); // 기본값: 기본(0)

  const POLLING_INTERVAL = 2000;

  const voicepackOptions = voicepacks.map(({id, name}) => ({
    label: name,
    value: id,
  }));

  const emotionOptions = [
    {label: "기본", value: 0},
    {label: "기쁨", value: 1},
    {label: "슬픔", value: 2},
    {label: "놀람", value: 3},
    {label: "화남", value: 4},
  ];

  const pollSynthesisStatus = async (statusUrl) => {
    const poll = async () => {
      try {
        const res = await fetch(statusUrl, {method: "GET", credentials: "include"});
        const result = await res.json();

        if (result.status === "COMPLETED" && result.resultUrl) {
          const audioRes = await fetch(result.resultUrl);
          const audioBlob = await audioRes.blob();
          const audioObjectUrl = URL.createObjectURL(audioBlob);

          setAudioUrl(audioObjectUrl);
          alert("음성 생성이 완료되었습니다.");
          setIsGenerating(false);
        } else if (result.status === "FAILED") {
          if (result.errorMessage === "부적절한 표현이 감지되어 음성 합성을 진행할 수 없습니다.") {
            alert(result.errorMessage);
          } else {
            alert("음성 합성에 실패했습니다.");
          }
          setIsGenerating(false);
        } else {
          setTimeout(poll, POLLING_INTERVAL);
        }
      } catch (err) {
        console.error("⛔ 폴링 중 에러:", err);
        alert("⚠️ 상태 확인 중 오류가 발생했습니다.");
        setIsGenerating(false);
      }
    };

    poll();
  };

  const handleSynthesize = async () => {
    if (!selectedVoiceId || !scriptText) {
      alert("보이스팩과 스크립트를 모두 입력해주세요.");
      return;
    }

    try {
      setIsGenerating(true);
      const location = await synthesize({
        voicepackId: parseInt(selectedVoiceId),
        prompt: scriptText,
        emotionIndex: emotionType,
      });

      if (location) {
        pollSynthesisStatus(location);
      } else {
        throw new Error("Location 헤더가 없습니다.");
      }
    } catch (error) {
      console.error("❌ 음성 생성 오류:", error);
      alert("TTS 생성 중 오류가 발생했습니다.");
      setIsGenerating(false);
    }
  };

  return (
    <div className="space-y-6">
      {isGenerating && (
        <div
          className="absolute inset-0 bg-violet-50 bg-opacity-40 backdrop-blur-sm flex flex-col items-center justify-center z-50 rounded-xl">
          <ScaleLoader color="#615FFF" height={40} width={4} radius={2} margin={3}/>
          <p className="mt-4 text-indigo-500 font-semibold text-lg animate-pulse">
            보이스 변환 중...
          </p>
          <p className="mt-4 text-indigo-500 font-semibold text-lg animate-pulse">
            페이지를 벗어나면 보이스 변환이 취소될 수 있어요!
          </p>
        </div>
      )}

      <h1 className="text-xl font-bold">베이직 보이스</h1>

      <div>
        {/* 보이스팩 선택 */}
        <div className="flex flex-col md:flex-row mb-4 gap-4">
          <div className="w-1/4">
            <SelectBox
              label="보이스팩"
              value={selectedVoiceId}
              onChange={(value) => setSelectedVoiceId(value)}
              options={voicepackOptions}
              placeholder="보이스팩을 선택해주세요."
            />
          </div>
          <div className="w-1/4">
            <SelectBox
              label="감정"
              value={emotionType}
              onChange={(value) => setEmotionType(Number(value))}
              options={emotionOptions}
              placeholder="감정을 선택해주세요."
            />
          </div>
        </div>

        {/* 텍스트 입력 */}
        <textarea
          value={scriptText}
          onChange={(e) => setScriptText(e.target.value)}
          placeholder="변환할 텍스트를 입력해주세요."
          className="w-full h-40 p-4 bg-white text-gray-600 placeholder-gray-400 rounded-md resize-none focus:outline-none focus:ring-1 focus:ring-indigo-400"
        />

        <div className="mt-4 flex justify-end">
          <GradientButton
            className="px-6 py-3 disabled:opacity-50 disabled:cursor-not-allowed"
            onClick={handleSynthesize}
            disabled={!selectedVoiceId || !scriptText || isGenerating}
          >
            {isGenerating ? "생성 중..." : "생성하기"}
          </GradientButton>
        </div>
      </div>

      {audioUrl && (
        <div className="mt-12 px-2 py-2 bg-white backdrop-blur-sm rounded-xl">
          <div className="flex items-center space-x-4 px-6 py-5">
            <div className="flex-1">
              <AudioPlayer audioUrl={audioUrl}/>
            </div>
            <GradientButton
              className="px-6 py-3"
              onClick={() => {
                const fileName = prompt("저장할 파일 이름을 입력해주세요.");
                if (!fileName) return;

                const link = document.createElement("a");
                link.href = audioUrl;
                link.download = `${fileName}.mp3`;
                link.click();
              }}
            >
              다운로드
            </GradientButton>
          </div>
        </div>
      )}
    </div>
  );
};

export default BasicVoice;
