// components/Sidebar.js
import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Mic, BotMessageSquare, ShoppingBag, LogOut, AudioLines } from 'lucide-react';
import logo from '../../assets/logo-new.svg';

const Sidebar = () => {
  const location = useLocation();

  const isActive = (path) =>
    location.pathname.startsWith(path)
      ? 'bg-slate-50 text-indigo-500'
      : 'text-gray-400';

  return (
    <div className="w-48 bg-white p-4 flex flex-col justify-between h-screen">
      {/* 상단: 로고 + 메뉴들 */}
      <div className="space-y-4">
        <Link
          to="/"
          className="flex content-center justify-center text-center font-bold text-xl mb-20"
        >
          <img src={logo} alt="COVOS" width={120} />
        </Link>
        <Link
          to={"/voice-create"}
          className={`flex items-center space-x-2 p-2 rounded ${isActive('/voice-create')}`}
        >
          <Mic size={20} />
          <span>보이스팩 생성</span>
        </Link>
        <Link
          to={"/voice-store"}
          className={`flex items-center space-x-2 p-2 rounded ${isActive('/voice-store')}`}
        >
          <ShoppingBag size={20} />
          <span>마켓플레이스</span>
        </Link>
        <Link
          to={"/basic-voice"}
          className={`flex items-center space-x-2 p-2 rounded ${isActive('/basic-voice')}`}
        >
          <AudioLines size={20} />
          <span>베이직 보이스</span>
        </Link>
        <Link
          to={"/ai-assistant"}
          className={`flex items-center space-x-2 p-2 rounded ${isActive('/ai-assistant')}`}
        >
          <BotMessageSquare size={20} />
          <span>AI 비서</span>
        </Link>
      </div>

      {/* 하단: 로그아웃 버튼 */}
      <Link
        to={"/sign-in"} // 실제 로그아웃 로직 연결 필요 시 수정
        className="flex items-center space-x-2 p-2 rounded text-gray-400"
      >
        <LogOut size={20} />
        <span>로그아웃</span>
      </Link>
    </div>
  );
};

export default Sidebar;