import React, {useEffect, useRef, useState} from 'react';
import useBuyVoicepack from '../hooks/useBuyVoicepack';
import useVoicepackDetail from '../hooks/useVoicepackDetail';
import SelectBox from "../components/common/SelectBox";
import axiosInstance from '../utils/axiosInstance';
import {Search} from 'lucide-react';
import LP from '../assets/lp.svg';

function VoiceStore() {
  const [voicePacks, setVoicePacks] = useState([]);
  const [filteredPacks, setFilteredPacks] = useState([]);
  const [selectedPack, setSelectedPack] = useState(null);
  const [audioUrl, setAudioUrl] = useState('');
  const [duration, setDuration] = useState(0);
  const [currentTime, setCurrentTime] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);
  const [sortOption, setSortOption] = useState('latest');
  const [searchQuery, setSearchQuery] = useState('');
  const [committedQuery, setCommittedQuery] = useState('');

  const audioRef = useRef(null);
  const {buy} = useBuyVoicepack();
  const {getVoicepackAudio} = useVoicepackDetail();

  const closeModal = () => {
    setSelectedPack(null);
    setAudioUrl('');
    setDuration(0);
    setCurrentTime(0);
    setIsPlaying(false);
  };

  const handleSearch = () => {
    setCommittedQuery(searchQuery);
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  useEffect(() => {
    const fetchVoicePacks = async () => {
      try {
        const res = await axiosInstance.get('/voicepack');
        setVoicePacks(res.data);
      } catch (err) {
        console.error('❌ 보이스팩 불러오기 실패:', err);
      }
    };
    fetchVoicePacks();
  }, []);

  useEffect(() => {
    let result = [...voicePacks];
    if (committedQuery) {
      result = result.filter(
        (pack) =>
          pack.name.toLowerCase().includes(committedQuery.toLowerCase()) ||
          pack.author.toLowerCase().includes(committedQuery.toLowerCase())
      );
    }
    if (sortOption === 'name') {
      result.sort((a, b) => a.name.localeCompare(b.name));
    } else if (sortOption === 'latest') {
      result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)); // 최신순
    } else if (sortOption === 'oldest') {
      result.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt)); // 오래된순
    }
    setFilteredPacks(result);
  }, [voicePacks, sortOption, committedQuery]);

  const handleCardClick = async (pack) => {
    setSelectedPack(pack);
    try {
      const url = await getVoicepackAudio(pack.id);
      setAudioUrl(url);
    } catch (err) {
      console.error('❌ 오디오 로딩 실패:', err);
      setAudioUrl('');
    }
  };

  const handlePurchase = async () => {
    if (!selectedPack) return;
    try {
      const result = await buy(selectedPack.id);
      alert(`${result.message || '성공적으로 구매되었습니다.'}`);
      closeModal();
    } catch (err) {
      console.error('구매 실패:', err);
      alert('구매에 실패했습니다. 다시 시도해주세요.');
    }
  };

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

  const formatSeconds = (seconds) => {
    const min = String(Math.floor(seconds / 60)).padStart(2, '0');
    const sec = String(Math.floor(seconds % 60)).padStart(2, '0');
    return `${min}:${sec}`;
  };

  const formatDate = (isoString) => new Date(isoString).toISOString().split('T')[0];

  return (
    <>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-xl font-bold">마켓플레이스</h1>
        <div className="flex gap-2">
          <SelectBox
            label=""
            value={sortOption}
            onChange={(e) => setSortOption(e.target.value)}
            options={[
              {label: '이름순', value: 'name'},
              {label: '최근 등록순', value: 'latest'},
              {label: '오래된 등록순', value: 'oldest'},
            ]}
            placeholder="정렬"
          />
          <div className="flex items-center border border-[#D9D9D9] px-2 rounded-lg text-sm bg-white mt-1">
            <button onClick={handleSearch} className="mr-2">
              <Search className="w-5 h-5 text-gray-400"/>
            </button>
            <input
              type="text"
              placeholder="보이스팩을 검색해보세요."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyDown={handleKeyDown}
              className="outline-none w-full text-sm"
            />
          </div>
        </div>
      </div>

      <div className="w-full px-8 min-h-screen">
        <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-6 justify-items-center">
          {filteredPacks.length === 0 ? (
            <p className="col-span-full text-gray-500 text-md mt-12">검색 결과가 없습니다.</p>
          ) : (
            filteredPacks.map((pack) => (
              <div
                key={pack.id}
                className="bg-violet-50 p-4 border border-indigo-300 rounded-xl shadow-md hover:shadow-xl w-full max-w-[240px] text-center cursor-pointer"
                onClick={() => handleCardClick(pack)}
              >
                <div className="max-w-[180px] max-h-[180px] mx-auto mb-2">
                  <img src={LP} alt="LP"/>
                </div>
                <h2 className="text-lg font-semibold mb-1">{pack.name}</h2>
                <p className="text-xs text-slate-600">{pack.author}</p>
                <p className="text-xs text-slate-600">{formatDate(pack.createdAt)}</p>
                <div className="flex justify-center gap-2 mt-2">
                  <span className="text-xs text-indigo-700 bg-indigo-100 px-3 py-1 rounded-lg">#카테고리</span>
                  <span className="text-xs text-indigo-700 bg-indigo-100 px-3 py-1 rounded-lg">#카테고리</span>
                </div>
              </div>
            ))
          )}
        </div>

        {selectedPack && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
            <div
              className="bg-white p-6 rounded-xl shadow-xl w-full max-w-[600px] flex flex-col sm:flex-row gap-6 relative">
              <button
                onClick={closeModal}
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
                      onLoadedMetadata={(e) => {
                        setDuration(e.target.duration);
                        setCurrentTime(0);
                      }}
                      onTimeUpdate={(e) => setCurrentTime(e.target.currentTime)}
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
                  <h2 className="text-xl font-bold text-left">{selectedPack.name}</h2>
                  <p className="text-sm text-slate-600 text-left">{selectedPack.author}</p>
                  <div className="flex gap-2 mt-2">
                    <span className="text-xs text-indigo-700 bg-indigo-100 px-3 py-1 rounded-lg">#카테고리</span>
                    <span className="text-xs text-indigo-700 bg-indigo-100 px-3 py-1 rounded-lg">#카테고리</span>
                  </div>
                </div>
                <button
                  className="mt-6 bg-gradient-to-r from-violet-400 to-indigo-500 text-white font-semibold py-2 rounded-full hover:opacity-70 transition"
                  onClick={handlePurchase}
                >
                  구매하기
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </>
  );
}

export default VoiceStore;
