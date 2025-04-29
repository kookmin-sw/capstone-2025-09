import React, { useEffect, useRef, useState } from 'react';
import LP from '../../assets/lp.svg';
import useVoicepackDetail from '../../hooks/useVoicepackDetail';

function VoicePackModal({ pack, onClose, onBuy }) {
  const audioRef = useRef(null);
  const { getVoicepackAudio } = useVoicepackDetail();

  const [audioUrl, setAudioUrl] = useState('');
  const [duration, setDuration] = useState(0);
  const [currentTime, setCurrentTime] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);

  useEffect(() => {
    const fetchAudio = async () => {
      try {
        const url = await getVoicepackAudio(pack.id);
        setAudioUrl(url);
      } catch (err) {
        console.error('❌ 오디오 로딩 실패:', err);
        setAudioUrl('');
      }
    };
    fetchAudio();
  }, [pack.id]);

  const togglePlay = () => {
    if (!audioRef.current) return;
    if (isPlaying) {
      audioRef.current.pause();
    } else {
      audioRef.current.play();
    }
    setIsPlaying(!isPlaying);
  };

  const handleSeek = (e) => {
    const value = e.target.value;
    if (!audioRef.current) return;
    audioRef.current.currentTime = value;
    setCurrentTime(value);
  };

  const handleLoadedMetadata = (e) => {
    setDuration(e.target.duration);
    setCurrentTime(0);
  };

  const handleTimeUpdate = (e) => {
    setCurrentTime(e.target.currentTime);
  };

  const formatSeconds = (seconds) => {
    const min = String(Math.floor(seconds / 60)).padStart(2, '0');
    const sec = String(Math.floor(seconds % 60)).padStart(2, '0');
    return `${min}:${sec}`;
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
      <div className="bg-white p-6 rounded-xl shadow-xl w-full max-w-[600px] flex flex-col sm:flex-row gap-6 relative">
        <button
          onClick={onClose}
          className="absolute top-2 right-4 text-gray-400 hover:text-black text-2xl font-light"
        >
          &times;
        </button>
        <div className="sm:w-1/2 flex flex-col items-center justify-center bg-violet-50 rounded-xl p-4">
          <img src={LP} alt="LP" className="w-[140px] h-[140px] mb-4"/>
          {audioUrl && (
            <>
              <audio
                ref={audioRef}
                src={audioUrl}
                preload="metadata"
                crossOrigin="anonymous"
                onLoadedMetadata={handleLoadedMetadata}
                onTimeUpdate={handleTimeUpdate}
                style={{display: 'none'}}
              />
              <input
                type="range"
                min="0"
                max={duration}
                value={currentTime}
                onChange={handleSeek}
                className="w-full h-1 bg-indigo-300 rounded-full appearance-none mb-3
                  [&::-webkit-slider-thumb]:appearance-none
                  [&::-webkit-slider-thumb]:h-3
                  [&::-webkit-slider-thumb]:w-3
                  [&::-webkit-slider-thumb]:rounded-full
                  [&::-webkit-slider-thumb]:bg-indigo-500
                  [&::-webkit-slider-thumb]:border
                  [&::-webkit-slider-thumb]:border-indigo-500
                  [&::-webkit-slider-thumb]:cursor-pointer focus:outline-none"
              />
              <button
                onClick={togglePlay}
                className="w-10 h-10 rounded-full bg-indigo-500 text-white flex items-center justify-center text-lg hover:bg-indigo-600 transition"
              >
                {isPlaying ? '⏸' : '▶'}
              </button>
              <p className="text-sm text-indigo-500 mt-2">
                {formatSeconds(currentTime)} / {formatSeconds(duration)}
              </p>
            </>
          )}
        </div>
        <div className="sm:w-1/2 flex flex-col justify-start py-2">
          <div className="px-3 gap-2 flex flex-col">
            <h2 className="text-xl font-bold text-left">{pack.name}</h2>
            <p className="text-sm text-slate-600 text-left">{pack.author}</p>
            <div className="flex gap-2 mt-2">
              <span className="text-xs text-indigo-700 bg-indigo-100 px-3 py-1 rounded-lg">#카테고리</span>
              <span className="text-xs text-indigo-700 bg-indigo-100 px-3 py-1 rounded-lg">#카테고리</span>
            </div>
          </div>
          <button
            className="mt-6 bg-gradient-to-r from-violet-400 to-indigo-500 text-white font-semibold py-2 rounded-full hover:opacity-70 transition"
            onClick={onBuy}
          >
            구매하기
          </button>
        </div>
      </div>
    </div>
  );
}

export default VoicePackModal;
