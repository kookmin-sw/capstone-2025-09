import React, { useEffect, useState } from 'react';
import axiosInstance from '../utils/axiosInstance';
import { Search } from 'lucide-react';
import SelectBox from '../components/common/SelectBox';
import VoicePackCard from '../components/common/VoicePack';

function VoiceStore() {
  const [voicePacks, setVoicePacks] = useState([]);
  const [filteredPacks, setFilteredPacks] = useState([]);
  const [sortOption, setSortOption] = useState('latest');
  const [searchQuery, setSearchQuery] = useState('');
  const [committedQuery, setCommittedQuery] = useState('');

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
      result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    } else if (sortOption === 'oldest') {
      result.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
    }
    setFilteredPacks(result);
  }, [voicePacks, sortOption, committedQuery]);

  return (
    <>
      <h1 className="text-xl font-bold">마켓플레이스</h1>
      <div className="container mx-auto px-4">
        <div className="flex justify-end items-center mb-6">
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

        <div className="min-h-screen">
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6 justify-center">
            {filteredPacks.length === 0 ? (
              <p className="col-span-full text-gray-500 text-md mt-12">검색 결과가 없습니다.</p>
            ) : (
              filteredPacks.map((pack) => (
                <VoicePackCard
                  key={pack.id}
                  pack={pack}
                  type="voicestore"
                />
              ))
            )}
          </div>
        </div>
      </div>
    </>
  );
}

export default VoiceStore;
