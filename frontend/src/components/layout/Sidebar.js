import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
  Mic,
  BotMessageSquare,
  ShoppingBag,
  LogOut,
  AudioLines,
  Quote,
  BookUser,
} from 'lucide-react';
import useUserStore from '../../utils/userStore';

const Sidebar = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const clearUser = useUserStore((state) => state.clearUser);

  const isActive = (path) =>
    location.pathname.startsWith(path)
      ? 'bg-slate-50 text-indigo-500'
      : 'text-gray-400';

  const handleLogout = () => {
    clearUser();
    sessionStorage.clear();
    localStorage.clear();
    navigate('/'); // 랜딩 페이지로 이동
  };

  return (
    <div className="fixed top-0 left-0 w-48 bg-white p-4 flex flex-col justify-between h-screen shadow-md z-20">
      {/* 상단: 로고 + 메뉴들 */}
      <div className="space-y-4">
        <Link
          to="/"
          className="flex content-center justify-center text-center font-bold text-xl mb-20"
        >
          <img src="/assets/logo-new.svg" alt="COVOS" width={120} />
        </Link>
        <Link
          to="/voice-create"
          className={`flex items-center space-x-2 p-2 rounded ${isActive('/voice-create')}`}
        >
          <Mic size={20} />
          <span>보이스팩 생성</span>
        </Link>
        <Link
          to="/voice-store"
          className={`flex items-center space-x-2 p-2 rounded ${isActive('/voice-store')}`}
        >
          <ShoppingBag size={20} />
          <span>마켓플레이스</span>
        </Link>
        <Link
          to="/basic-voice"
          className={`flex items-center space-x-2 p-2 rounded ${isActive('/basic-voice')}`}
        >
          <AudioLines size={20} />
          <span>베이직 보이스</span>
        </Link>
        <Link
          to="/ai-assistant"
          className={`flex items-center space-x-2 p-2 rounded ${isActive('/ai-assistant')}`}
        >
          <BotMessageSquare size={20} />
          <span>AI 리포터</span>
        </Link>
        <Link
          to="/quote"
          className={`flex items-center space-x-2 p-2 rounded ${isActive('/quote')}`}
        >
          <Quote size={20} />
          <span>오늘의 명언</span>
        </Link>
        <Link
          to="/remember-voice"
          className={`flex items-center space-x-2 p-2 rounded ${isActive('/remember-voice')}`}
        >
          <BookUser size={20} />
          <span>리멤버 보이스</span>
        </Link>
      </div>

      {/* 하단: 로그아웃 버튼 */}
      <button
        onClick={handleLogout}
        className="flex items-center space-x-2 p-2 rounded text-gray-400"
      >
        <LogOut size={20} />
        <span>로그아웃</span>
      </button>
    </div>
  );
};

export default Sidebar;
