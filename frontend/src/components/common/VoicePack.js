import React, { useState } from 'react';
import LP from '../../assets/lp.svg';
import VoicePackModal from './VoicePackModal';
import useUserStore from '../../utils/userStore';

const VoicePack = ({ pack, type = 'voicestore', onRefresh }) => {
  const [isModalOpen, setIsModalOpen] = useState(false);

  const user = useUserStore((state) => state.user);

  const isMypage = type === 'mypage';
  const isMine = pack.author === user.email;

  const handleClick = () => setIsModalOpen(true);
  const closeModal = () => setIsModalOpen(false);

  const formatDate = (isoString) =>
    new Date(isoString).toISOString().split('T')[0];

  return (
    <>
      <div
        className={`bg-violet-50 p-4 border border-indigo-300 rounded-xl shadow-md hover:shadow-xl cursor-pointer text-center ${
          isMypage ? 'w-36 h-auto' : 'max-w-[240px] w-full'
        }`}
        onClick={handleClick}
      >
        <div
          className={`${
            isMypage ? 'w-[100px] h-[100px]' : 'max-w-[180px] max-h-[180px]'
          } mx-auto mb-2`}
        >
          <img src={LP} alt="LP" className="w-full h-full object-contain" />
        </div>
        <h2
          className={`${
            isMypage ? 'text-sm' : 'text-sm sm:text-md md:text-lg'
          } font-semibold mb-1`}
        >
          {pack.name}
        </h2>
        <p
          className={`${
            isMypage ? 'text-[10px]' : 'text-xs sm:text-xs'
          } text-slate-600 break-all`}
        >
          {pack.author}
        </p>
        <p
          className={`${
            isMypage ? 'text-[10px]' : 'text-xs sm:text-xs'
          } text-slate-600`}
        >
          {formatDate(pack.createdAt)}
        </p>

        <div className="flex justify-center gap-2 mt-2 flex-wrap">
          <span
            className={`${
              isMypage
                ? 'text-[8px] px-2 py-0.5'
                : 'text-xs sm:text-xs px-3 py-1'
            } bg-indigo-100 text-indigo-700 rounded-lg`}
          >
            #카테고리
          </span>
          <span
            className={`${
              isMypage
                ? 'text-[8px] px-2 py-0.5'
                : 'text-xs sm:text-xs px-3 py-1'
            } bg-indigo-100 text-indigo-700 rounded-lg`}
          >
            #카테고리
          </span>
        </div>
      </div>

      {isModalOpen && (
        <VoicePackModal
          pack={pack}
          onClose={closeModal}
          type={type}
          filter={isMine ? 'mine' : 'purchased'}
          onRefresh={onRefresh}
        />
      )}
    </>
  );
};

export default VoicePack;
