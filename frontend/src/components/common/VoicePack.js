import React from 'react';
import LP from '../../assets/lp.svg';

function VoicePack({ pack, onClick, formatDate }) {
  return (
    <div
      key={pack.id}
      className="bg-violet-50 p-4 border border-indigo-300 rounded-xl shadow-md hover:shadow-xl w-full max-w-[240px] text-center cursor-pointer"
      onClick={() => onClick(pack)}
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
  );
}

export default VoicePack;
