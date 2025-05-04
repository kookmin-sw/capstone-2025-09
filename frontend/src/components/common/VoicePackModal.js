import React, { useEffect, useRef, useState } from 'react';
import LP from '../../assets/lp.svg';
import useVoicepackDetail from '../../hooks/useVoicepackDetail';
import useBuyVoicepack from '../../hooks/useBuyVoicepack';
import useVoicepackUsage from '../../hooks/useVoicepackUsage';

const VoicePackModal = ({
  pack,
  onClose,
  type = 'voicestore',
  filter = null,
}) => {
  const audioRef = useRef(null);
  const { getVoicepackAudio } = useVoicepackDetail();
  const { buy } = useBuyVoicepack();
  const { voicepacks: availableVoicepacks } = useVoicepackUsage('available');
  const isAvailable = availableVoicepacks.some((v) => v.id === pack.id);

  const [audioUrl, setAudioUrl] = useState('');
  const [duration, setDuration] = useState(0);
  const [currentTime, setCurrentTime] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);

  const isMypage = type === 'mypage';
  const isVoicestore = type === 'voicestore';
  const showEditDelete = isMypage && filter === 'mine';
  const showBuyButton = type === 'voicestore' && !isAvailable;

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
  }, [pack.id, getVoicepackAudio]);

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

  const handlePurchase = async () => {
    try {
      const result = await buy(pack.id);
      alert(`${result.message || '성공적으로 구매되었습니다.'}`);
      onClose(); // 구매 성공하면 모달 닫기
    } catch (err) {
      console.error('구매 실패:', err);
      alert('구매에 실패했습니다. 다시 시도해주세요.');
    }
  };

  const handleEdit = () => {
    alert('수정 기능은 추후 구현 예정입니다.');
  };

  const handleDelete = () => {
    alert('삭제 기능은 추후 구현 예정입니다.');
  };

  return (
    <div className="fixed top-0 left-48 right-0 bottom-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
      <div className="bg-white p-6 rounded-xl shadow-xl w-full max-w-[600px] flex flex-col sm:flex-row gap-6 relative">
        <button
          onClick={onClose}
          className="absolute top-2 right-4 text-gray-400 hover:text-black text-2xl font-light"
        >
          &times;
        </button>

        {/* 왼쪽: 이미지 및 플레이어 */}
        <div className="sm:w-1/2 flex flex-col items-center justify-center bg-violet-50 rounded-xl p-4">
          <img src={LP} alt="LP" className="w-[140px] h-[140px] mb-4" />
          {audioUrl && (
            <>
              <audio
                ref={audioRef}
                src={audioUrl}
                preload="metadata"
                crossOrigin="anonymous"
                onLoadedMetadata={handleLoadedMetadata}
                onTimeUpdate={handleTimeUpdate}
                style={{ display: 'none' }}
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

        {/* 오른쪽: 텍스트 정보 및 액션 */}
        <div className="sm:w-1/2 flex flex-col justify-start py-2">
          <div className="px-3 gap-2 flex flex-col">
            <h2 className="text-base sm:text-lg md:text-xl font-bold text-left">
              {pack.name}
            </h2>
            <p className="text-[11px] sm:text-sm text-slate-600 text-left">
              {pack.author}
            </p>

            <div className="flex gap-2 mt-2 flex-wrap">
              <span className="text-[10px] sm:text-xs md:text-sm bg-indigo-100 text-indigo-700 px-2 sm:px-3 py-0.5 sm:py-1 rounded-lg">
                #카테고리
              </span>
              <span className="text-[10px] sm:text-xs md:text-sm bg-indigo-100 text-indigo-700 px-2 sm:px-3 py-0.5 sm:py-1 rounded-lg">
                #카테고리
              </span>
            </div>

            {/* 버튼 조건 처리 */}
            {showBuyButton && (
              <button
                className="mt-6 bg-gradient-to-r from-violet-400 to-indigo-500 text-white font-semibold text-sm sm:text-base py-1.5 sm:py-2 rounded-full hover:opacity-70 transition"
                onClick={handlePurchase}
              >
                구매하기
              </button>
            )}
            {/* 이미 보유한 경우 메시지 */}
            {!showBuyButton && isVoicestore && (
              <p className="mt-6 text-sm text-indigo-400 font-medium">
                이미 보유한 보이스팩입니다.
              </p>
            )}

            {showEditDelete && (
              <div className="mt-6 flex flex-wrap gap-4">
                <button
                  className="flex-1 bg-yellow-400 text-white font-semibold text-sm sm:text-base py-1.5 sm:py-2 rounded-full hover:opacity-80 transition"
                  onClick={handleEdit}
                >
                  수정하기
                </button>
                <button
                  className="flex-1 bg-red-500 text-white font-semibold text-sm sm:text-base py-1.5 sm:py-2 rounded-full hover:opacity-80 transition"
                  onClick={handleDelete}
                >
                  삭제하기
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default VoicePackModal;
