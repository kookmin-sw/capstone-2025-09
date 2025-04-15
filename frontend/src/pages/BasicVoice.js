import React, { useEffect, useRef, useState } from "react";
import GradientButton from "../components/common/GradientButton";
import useVoicepackUsage from "../hooks/useVoicepackUsage";
import SelectBox from "../components/common/SelectBox";
import useVoicepackSynthesis from "../hooks/useVoicepackSynthesis";
import { ScaleLoader } from "react-spinners";
import WaveSurfer from "wavesurfer.js";
import { Play, Pause } from "lucide-react";

const BasicVoice = () => {
  const { voicepacks } = useVoicepackUsage();
  const { synthesize } = useVoicepackSynthesis();

  const [selectedVoiceId, setSelectedVoiceId] = useState("");
  const [scriptText, setScriptText] = useState("");
  const [audioUrl, setAudioUrl] = useState(null);
  const [isGenerating, setIsGenerating] = useState(false);

  const [isPlaying, setIsPlaying] = useState(false);
  const [duration, setDuration] = useState("00:00");
  const [currentTime, setCurrentTime] = useState("00:00");
  const waveformRef = useRef(null);
  const wavesurfer = useRef(null);

  const POLLING_INTERVAL = 2000;

  const voicepackOptions = voicepacks.map(({ voicepackId, voicepackName }) => ({
    label: voicepackName,
    value: voicepackId,
  }));

  const pollSynthesisStatus = async (statusUrl) => {
    const poll = async () => {
      try {
        const res = await fetch(statusUrl, { method: "GET", credentials: "include" });
        const result = await res.json();
        console.log("📡 폴링 결과:", result);

        if (result.status === "COMPLETED" && result.resultUrl) {
          const audioRes = await fetch(result.resultUrl);
          const audioBlob = await audioRes.blob();
          const audioObjectUrl = URL.createObjectURL(audioBlob);

          setAudioUrl(audioObjectUrl);
          alert("음성 생성이 완료되었습니다.");
          setIsGenerating(false);
        } else if (result.status === "FAILED") {
          alert("음성 생성에 실패했습니다.");
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

  const togglePlay = () => {
    if (wavesurfer.current) {
      wavesurfer.current.playPause();
      setIsPlaying(wavesurfer.current.isPlaying());
    }
  };

  const formatTime = (seconds) => {
    if (isNaN(seconds)) return "00:00";
    const mins = String(Math.floor(seconds / 60)).padStart(2, "0");
    const secs = String(Math.floor(seconds % 60)).padStart(2, "0");
    return `${mins}:${secs}`;
  };

  useEffect(() => {
    if (!audioUrl) return;

    wavesurfer.current = WaveSurfer.create({
      container: waveformRef.current,
      waveColor: "#c7d2fe",
      progressColor: "#6366f1",
      cursorColor: "#6366f1",
      height: 80,
      barWidth: 2,
      barGap: 1.5,
      responsive: true,
    });

    wavesurfer.current.load(audioUrl);

    wavesurfer.current.on("ready", () => {
      const dur = wavesurfer.current.getDuration();
      setDuration(formatTime(dur));
      setCurrentTime("00:00");
    });

    wavesurfer.current.on("audioprocess", () => {
      const time = wavesurfer.current.getCurrentTime();
      setCurrentTime(formatTime(time));
    });

    wavesurfer.current.on("finish", () => {
      setIsPlaying(false);
      setCurrentTime(duration);
    });

    return () => {
      wavesurfer.current?.destroy();
    };
  }, [audioUrl,duration]);

  return (
    <div className="space-y-6">
      {isGenerating && (
        <div className="absolute inset-0 bg-violet-50 bg-opacity-40 backdrop-blur-sm flex flex-col items-center justify-center z-50 rounded-xl">
          <ScaleLoader color="#615FFF" height={40} width={4} radius={2} margin={3} />
          <p className="mt-4 text-indigo-500 font-semibold text-lg animate-pulse">
            보이스 변환 중...
          </p>
          <p className="mt-4 text-indigo-500 font-semibold text-lg animate-pulse">
            "페이지를 벗어나면 보이스 변환이 취소될 수 있어요!"
          </p>
        </div>
      )}

      <h1 className="text-xl font-bold">베이직 보이스</h1>

      <div>
        <div className="w-1/4 mb-2">
          <SelectBox
            label="보이스팩"
            value={selectedVoiceId}
            onChange={(e) => setSelectedVoiceId(e.target.value)}
            options={voicepackOptions}
            placeholder="보이스팩을 선택해주세요."
          />
        </div>

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
        <div className="mt-12 px-2 py-2 bg-white backdrop-blur-sm rounded-xl  space-y-4">
          <div className="rounded-xl px-6 py-5 flex items-center justify-between space-x-4">
            <div className="flex items-center">
              <button
                onClick={togglePlay}
                className="w-12 h-12 rounded-full bg-indigo-500 text-white text-xl flex items-center justify-center shadow-md hover:bg-indigo-300 transition disabled:bg-gray-300"
              >
                {isPlaying ? <Pause /> : <Play />}
              </button>
            </div>

            <div className="flex-1 mx-4">
              <div ref={waveformRef} className="w-full h-[80px] overflow-hidden scrollbar-hide" />
            </div>

            <span className="text-indigo-500 font-semibold whitespace-nowrap w-[110px] text-right">
              {currentTime} / {duration}
            </span>

            <GradientButton
              className="px-6 py-3 disabled:opacity-50 disabled:cursor-not-allowed"
              onClick={() => {
                const fileName = prompt("저장할 파일 이름을 입력해주세요.");
                if (!fileName) return;

                const link = document.createElement("a");
                link.href = audioUrl;
                link.download = `${fileName}.mp3`; // 사용자가 입력한 이름 + 확장자
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
