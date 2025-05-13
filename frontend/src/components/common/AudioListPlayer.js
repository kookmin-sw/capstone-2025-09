import React, { useEffect, useRef, useState } from 'react';
import { Play, Pause } from 'lucide-react';

const AudioListPlayer = ({ audioUrls }) => {
  const audioRef = useRef(new Audio());
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [currentTime, setCurrentTime] = useState('00:00');
  const [totalDuration, setTotalDuration] = useState('00:00');
  const intervalRef = useRef(null);

  const formatTime = (seconds) => {
    if (isNaN(seconds)) return '00:00';
    const mins = String(Math.floor(seconds / 60)).padStart(2, '0');
    const secs = String(Math.floor(seconds % 60)).padStart(2, '0');
    return `${mins}:${secs}`;
  };

  useEffect(() => {
    if (!audioUrls || audioUrls.length === 0) return;

    const current = audioRef.current;
    current.src = audioUrls[currentIndex];
    current.load();

    const updateTime = () => {
      setCurrentTime(formatTime(current.currentTime));
    };

    current.onloadedmetadata = () => {
      setTotalDuration(formatTime(current.duration));
      setCurrentTime('00:00');
    };

    current.onended = () => {
      if (currentIndex < audioUrls.length - 1) {
        setCurrentIndex((prev) => prev + 1);
      } else {
        setIsPlaying(false);
      }
    };

    intervalRef.current = setInterval(updateTime, 500);

    return () => {
      current.pause();
      clearInterval(intervalRef.current);
    };
  }, [currentIndex, audioUrls]);

  const togglePlay = () => {
    const audio = audioRef.current;
    if (isPlaying) {
      audio.pause();
      setIsPlaying(false);
    } else {
      audio
        .play()
        .then(() => {
          setIsPlaying(true);
        })
        .catch((err) => {
          console.error('🔊 재생 실패:', err);
        });
    }
  };

  return (
    <div className="w-full flex items-center justify-between space-x-4 px-6 py-5">
      <button
        onClick={togglePlay}
        className="w-12 h-12 rounded-full bg-indigo-500 text-white text-xl flex items-center justify-center shadow-md hover:bg-indigo-300 transition"
      >
        {isPlaying ? <Pause /> : <Play />}
      </button>

      <div className="flex-1 mx-4">
        <div className="w-full text-sm text-slate-600">
          <p>
            재생 중: {currentIndex + 1} / {audioUrls.length}
          </p>
        </div>
      </div>

      <span className="text-indigo-500 font-semibold whitespace-nowrap w-[120px] text-right">
        {currentTime} / {totalDuration}
      </span>
    </div>
  );
};

export default AudioListPlayer;
