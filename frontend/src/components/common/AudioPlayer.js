import React, { useEffect, useRef, useState } from "react";
import WaveSurfer from "wavesurfer.js";
import { Play, Pause } from "lucide-react";

const AudioPlayer = ({ audioUrl }) => {
  const waveformRef = useRef(null);
  const wavesurfer = useRef(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [duration, setDuration] = useState("00:00");
  const [currentTime, setCurrentTime] = useState("00:00");

  const formatTime = (seconds) => {
    if (isNaN(seconds)) return "00:00";
    const mins = String(Math.floor(seconds / 60)).padStart(2, "0");
    const secs = String(Math.floor(seconds % 60)).padStart(2, "0");
    return `${mins}:${secs}`;
  };

  const togglePlay = () => {
    if (wavesurfer.current) {
      wavesurfer.current.playPause();
      setIsPlaying(wavesurfer.current.isPlaying());
    }
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
    <div className="w-full flex items-center justify-between space-x-4 px-6 py-5">
      <button
        onClick={togglePlay}
        className="w-12 h-12 rounded-full bg-indigo-500 text-white text-xl flex items-center justify-center shadow-md hover:bg-indigo-300 transition"
      >
        {isPlaying ? <Pause /> : <Play />}
      </button>

      <div className="flex-1 mx-4">
        <div ref={waveformRef} className="w-full h-[80px] overflow-hidden scrollbar-hide" />
      </div>

      <span className="text-indigo-500 font-semibold whitespace-nowrap w-[110px] text-right">
        {currentTime} / {duration}
      </span>
    </div>
  );
};

export default AudioPlayer;
