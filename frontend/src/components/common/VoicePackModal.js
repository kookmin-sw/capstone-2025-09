import React, { useEffect, useRef, useState } from 'react';
import LP from '../../assets/lp.svg';
import { BadgeCent } from 'lucide-react';
import useVoicepackDetail from '../../hooks/useVoicepackDetail';
import useBuyVoicepack from '../../hooks/useBuyVoicepack';
import useUserStore from '../../utils/userStore';
import axiosInstance from '../../utils/axiosInstance';
import useVoicepackUsage from '../../hooks/useVoicepackUsage';

const VoicePackModal = ({
  pack,
  onClose,
  type = 'voicestore',
  filter = null,
  onRefresh,
}) => {
  const audioRef = useRef(null);
  const { getVoicepackAudio } = useVoicepackDetail();
  const { buy } = useBuyVoicepack();

  const { user } = useUserStore((state) => state);

  const { voicepacks: availableVoicepacks } = useVoicepackUsage('available');
  const isAvailable = availableVoicepacks.some((v) => v.id === pack.id);

  const [audioUrl, setAudioUrl] = useState('');
  const [duration, setDuration] = useState(0);
  const [currentTime, setCurrentTime] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);
  const [isPublic, setIsPublic] = useState(pack.isPublic);

  const isMypage = type === 'mypage' || type === 'dashboard';
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
    const confirmed = window.confirm('정말로 구매하시겠습니까?');
    if (!confirmed) return;

    try {
      const result = await buy(pack.id);
      alert(`${result.message || '성공적으로 구매되었습니다.'}`);
      onClose(); // 구매 성공하면 모달 닫기
    } catch (err) {
      console.error('구매 실패:', err);
      // 크래딧 부족 에러 추가
      if (err.response && err.response.status === 409) {
        alert('크래딧이 부족합니다. 충전 후 다시 시도해주세요.');
      } else {
        alert('구매에 실패했습니다. 다시 시도해주세요.');
      }
    }
  };

  const handleTogglePublic = async () => {
    try {
      const response = await axiosInstance.patch(
        `/voicepack/${pack.id}`,
        {
          isPublic: !isPublic,
        },
        {
          params: {
            userId: user.id,
            voicepackId: pack.id,
          },
        }
      );
      setIsPublic(response.data.isPublic);
      alert(
        response.data.isPublic
          ? '공개로 변경되었습니다.'
          : '비공개로 변경되었습니다.'
      );
      if (onRefresh) {
        onRefresh();
      }
    } catch (err) {
      alert('공개 여부 변경에 실패했습니다.');
    }
  };

  const handleDelete = async () => {
    try {
      await axiosInstance.delete(`/voicepack/${pack.id}`, {
        params: {
          userId: user.id,
          voicepackId: pack.id,
        },
      });
      alert('삭제되었습니다.');

      if (onRefresh) {
        onRefresh();
        onClose();
      }
    } catch (err) {
      console.error('삭제 실패:', err);
      alert('삭제에 실패했습니다. 다시 시도해주세요.');
    }
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

        <div className="sm:w-1/2 flex flex-col items-center justify-center bg-violet-50 rounded-xl p-4">
          <img
            src={pack.imageUrl || LP}
            alt="VoicePack Cover"
            className="w-[140px] h-[140px] aspect-square object-cover rounded-2xl mb-4"
            onError={(e) => {
              e.currentTarget.onerror = null;
              e.currentTarget.src = LP;
            }}
          />
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

        <div className="sm:w-1/2 flex flex-col justify-start py-2">
          <div className="px-3 gap-2 flex flex-col">
            <h2 className="text-base sm:text-lg md:text-xl font-bold text-left">
              {pack.name}
            </h2>
            <p className="text-[11px] sm:text-sm text-slate-600 text-left">
              {pack.authorName}
            </p>

            <div className="flex justify-start gap-2 mt-2 flex-wrap">
              {Array.isArray(pack.categories) &&
                pack.categories.slice(0, 2).map((category, index) => (
                  <span
                    key={index}
                    className="text-sm px-3 py-1 bg-indigo-100 text-indigo-700 rounded-lg"
                  >
                    # {category}
                  </span>
                ))}
            </div>

            {/* 버튼 조건 처리 */}
            {showBuyButton && (
              <>
                <div className="flex items-center gap-2 mt-4">
                  <BadgeCent className="w-5 h-5 text-yellow-400" />
                  <p className="text-sm text-slate-600">
                    {pack.price?.toLocaleString() || '0'} 크레딧
                  </p>
                </div>
                <button
                  className="mt-2 bg-gradient-to-r from-violet-400 to-indigo-500 text-white font-semibold text-sm sm:text-base py-1.5 sm:py-2 rounded-full hover:opacity-70 transition"
                  onClick={handlePurchase}
                >
                  구매하기
                </button>
              </>
            )}

            {/* 이미 보유한 경우 메시지 */}
            {!showBuyButton && isVoicestore && (
              <p className="mt-6 text-sm text-indigo-400 font-medium">
                이미 보유한 보이스팩입니다.
              </p>
            )}

            {showEditDelete && (
              <>
                <button
                  onClick={handleTogglePublic}
                  className={`relative inline-flex items-center w-20 h-8 rounded-full transition-colors duration-300 ${
                    isPublic ? 'bg-green-500' : 'bg-gray-300'
                  }`}
                >
                  <span
                    className={`absolute -translate-x-1/2 text-xs font-medium w-full text-white z-10 ${
                      isPublic ? ' left-1/3 ' : 'left-2/3'
                    }`}
                  >
                    {isPublic ? '공개' : '비공개'}
                  </span>
                  <div
                    className={`absolute top-1 left-1 h-6 w-6 rounded-full bg-white shadow transition-transform duration-300 ${
                      isPublic ? 'translate-x-12' : 'translate-x-0'
                    }`}
                  />
                </button>

                <div className="mt-4">
                  <button
                    className="w-full bg-red-500 text-white font-semibold text-sm py-2 rounded-full hover:opacity-80 transition"
                    onClick={handleDelete}
                  >
                    삭제하기
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default VoicePackModal;
