import React, {useState} from 'react';
import LP from '../../assets/lp.svg';
import VoicePackModal from './VoicePackModal';

function VoicePack({pack, type = 'voicestore'}) { // ← type 추가하고 기본값은 marketplace
  const [isModalOpen, setIsModalOpen] = useState(false);

  const handleCardClick = () => {
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
  };

  const formatDate = (isoString) => {
    return new Date(isoString).toISOString().split('T')[0];
  };

  // ⭐ 여기 추가: type에 따라 카드 크기 클래스 구분
  const cardSize = type === 'voicestore'
    ? 'max-w-[240px]' // 마켓플레이스용 큰 카드
    : 'max-w-[180px]'; // 마이페이지용 작은 카드

  return (
    <>
      <div
        className={`bg-violet-50 p-4 border border-indigo-300 rounded-xl shadow-md hover:shadow-xl w-full ${cardSize} text-center cursor-pointer`}
        onClick={handleCardClick}
      >
        <div className="max-w-[180px] max-h-[180px] mx-auto mb-2">
          <img src={LP} alt="LP"/>
        </div>
        <h2 className="text-sm sm:text-md md:text-lg font-semibold mb-1">{pack.name}</h2>
        <p className="text-[10px] sm:text-xs text-slate-600 break-all">{pack.author}</p>
        <p className="text-[10px] sm:text-xs text-slate-600">{formatDate(pack.createdAt)}</p>

        <div className="flex justify-center gap-2 mt-2 flex-wrap">
          <span
            className="text-[10px] sm:text-xs md:text-sm bg-indigo-100 text-indigo-700 px-2 sm:px-3 py-0.5 sm:py-1 rounded-lg">
            #카테고리
          </span>
                  <span
                    className="text-[10px] sm:text-xs md:text-sm bg-indigo-100 text-indigo-700 px-2 sm:px-3 py-0.5 sm:py-1 rounded-lg">
            #카테고리
          </span>
        </div>

      </div>

      {isModalOpen && (
        <VoicePackModal
          pack={pack}
          onClose={closeModal}
          type={type}
        />
      )}
    </>
  );
}

export default VoicePack;
