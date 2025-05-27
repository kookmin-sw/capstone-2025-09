// components/Header.js
import React from 'react';
import { User } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const Header = () => {
  const navigate = useNavigate();

  return (
    <header className="flex items-center justify-end px-8 py-2 bg-stone-50/40">
      <button
        aria-label="로그아웃 버튼"
        onClick={() => navigate('/mypage')}
        className="w-10 h-10 bg-white opacity-80 rounded-[8px] flex items-center justify-center border-none"
      >
        <User size={20} className="text-gray-400" />
      </button>
    </header>
  );
};

export default Header;
