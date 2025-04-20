import React, {useEffect, useRef, useState} from 'react';
import useBuyVoicepack from '../hooks/useBuyVoicepack';
import useVoicepackDetail from '../hooks/useVoicepackDetail';
import SelectBox from "../components/common/SelectBox";
import axiosInstance from '../utils/axiosInstance';
import {Search} from 'lucide-react';
import VoicePackCard from '../components/common/VoicePack';
import VoicePackModal from '../components/common/VoicePackModal';

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
    axiosInstance.get('/voicepack')
      .then(res => setVoicePacks(res.data))
      .catch(err => console.error('❌ 보이스팩 불러오기 실패:', err));
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

  const handleLoadedMetadata = (e) => {
    setDuration(e.target.duration);
    setCurrentTime(0);
  };

  const handleTimeUpdate = (e) => {
    setCurrentTime(e.target.currentTime);
  };

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
              type="search"
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
              <VoicePackCard
                key={pack.id}
                pack={pack}
                onClick={handleCardClick}
                formatDate={formatDate}
              />
            ))
          )}
        </div>

        {selectedPack && (
          <VoicePackModal
            selectedPack={selectedPack}
            audioUrl={audioUrl}
            audioRef={audioRef}
            duration={duration}
            currentTime={currentTime}
            isPlaying={isPlaying}
            handleSeek={handleSeek}
            togglePlay={togglePlay}
            formatSeconds={formatSeconds}
            closeModal={closeModal}
            handlePurchase={handlePurchase}
            handleLoadedMetadata={handleLoadedMetadata}
            handleTimeUpdate={handleTimeUpdate}
          />
        )}
      </div>
    </>
  );
}

export default VoiceStore;
