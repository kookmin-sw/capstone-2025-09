import React, { useEffect, useRef, useState } from "react";
import WaveSurfer from "wavesurfer.js";
import { Play, Pause } from "lucide-react";
import GradientButton from "./GradientButton";

const WaveformPlayer = ({ audioUrl, onDownload }) => {
  const waveformRef = useRef(null);
  const wavesurfer = useRef(null);

  const [isPlaying, setIsPlaying] = useState(false);
  const [duration, setDuration] = useState("00:00");
  const [currentTime, setCurrentTime] = useState("00:00");

  useEffect(() => {
    if (!audioUrl) return;

    // WaveSurfer 인스턴스 생성
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

    // 오디오 로드
    wavesurfer.current.load(audioUrl);

    // 총 길이 세팅
    wavesurfer.current.on("ready", () => {
      const dur = wavesurfer.current.getDuration();
      setDuration(formatTime(dur));
      setCurrentTime("00:00");
    });

    // 재생 중일 때 현재 시간 업데이트
    wavesurfer.current.on("audioprocess", () => {
      const time = wavesurfer.current.getCurrentTime();
      setCurrentTime(formatTime(time));
    });

    // 재생 끝나면 정지 상태로 전환
    wavesurfer.current.on("finish", () => {
      setIsPlaying(false);
      setCurrentTime(duration); // 끝까지 감
    });

    return () => {
      wavesurfer.current?.destroy();
    };
  }, [audioUrl]);

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

  return (
    <div className="rounded-xl px-6 py-5 flex items-center justify-between space-x-4">
      {/* ▶️ / ⏸ 버튼 */}
      <div className="flex items-center">
        <button
          onClick={togglePlay}
          className="w-12 h-12 rounded-full bg-indigo-500 text-white text-xl flex items-center justify-center shadow-md hover:bg-indigo-300 transition disabled:bg-gray-300"
        >
          {isPlaying ? <Pause/> : <Play/>}
        </button>
      </div>

      {/* waveform bar */}
      <div className="flex-1 mx-4">
        <div ref={waveformRef} className="w-full h-[80px] overflow-hidden scrollbar-hide"/>
      </div>

      {/* 진행 시간 / 총 시간 */}
      <span className="text-indigo-500 font-semibold whitespace-nowrap w-[110px] text-right">
        {currentTime} / {duration}
      </span>

      {/* 다운로드 버튼 */}
      <GradientButton
        className="px-6 py-3 disabled:opacity-50 disabled:cursor-not-allowed"
        onClick={onDownload}
      >
        다운로드
      </GradientButton>
    </div>
  );
};

export default WaveformPlayer;
