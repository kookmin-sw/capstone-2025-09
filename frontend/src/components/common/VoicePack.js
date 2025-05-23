import React, { useState } from 'react';
import LP from '../../assets/lp.svg';
import VoicePackModal from './VoicePackModal';
import useUserStore from '../../utils/userStore';

const VoicePack = ({ pack, type = 'voicestore', onRefresh }) => {
  const [isModalOpen, setIsModalOpen] = useState(false);

  const user = useUserStore((state) => state.user);

  const isDashboard = type === 'dashboard';
  const isMypage = type === 'mypage';
  const isMine = pack.author === user.email;

  const handleClick = () => setIsModalOpen(true);
  const closeModal = () => setIsModalOpen(false);

  const formatDate = (isoString) =>
    new Date(isoString).toISOString().split('T')[0];

  return (
    <>
      <div
        className={`bg-violet-50 p-4 border border-indigo-300 rounded-xl hover:shadow-xl cursor-pointer text-center ${
          isDashboard
            ? 'w-36 h-auto'
            : isMypage
              ? 'w-full max-w-[150px] min-w-0'
              : 'max-w-[240px] w-full'
        }`}
        onClick={handleClick}
      >
        <div
          className={`${
            isMypage
              ? 'w-full max-w-[100px] h-auto'
              : 'max-w-[180px] max-h-[180px]'
          } mx-auto mb-2 flex justify-center items-center`}
        >
          <img
            src={pack.imageUrl || LP}
            alt="VoicePack Cover"
            className="aspect-square w-full object-cover rounded-2xl"
            onError={(e) => {
              e.currentTarget.onerror = null;
              e.currentTarget.src = LP;
            }}
          />
        </div>

        <h2
          className={`${
            isDashboard
              ? 'text-[10px] sm:text-xs md:text-sm'
              : isMypage
                ? 'text-xs sm:text-sm md:text-base'
                : 'text-sm sm:text-md md:text-lg'
          } font-semibold mb-1 truncate overflow-hidden whitespace-nowrap`}
        >
          {pack.name}
        </h2>
        <p
          className={`${
            isDashboard
              ? 'text-[10px] sm:text-xs md:text-sm'
              : isMypage
                ? 'text-xs sm:text-sm md:text-md'
                : 'text-sm sm:text-md'
          } text-slate-600 break-all`}
        >
          {pack.authorName}
        </p>
        <p
          className={`${
            isDashboard ? 'text-[10px]' : 'text-xs sm:text-xs'
          } text-slate-600`}
        >
          {formatDate(pack.createdAt)}
        </p>

        <div className="flex justify-center gap-2 mt-2 flex-wrap">
          {Array.isArray(pack.categories) &&
            pack.categories.slice(0, 2).map((category, index) => (
              <span
                key={index}
                className={`${
                  isDashboard
                    ? 'text-[10px] px-1.5 py-0.5'
                    : isMypage
                      ? 'text-[10px] px-1 py-0.5'
                      : 'text-sm px-3 py-1'
                } bg-indigo-100 text-indigo-700 rounded-lg`}
              >
                # {category}
              </span>
            ))}
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
