import React from 'react';
import { useLocation } from 'react-router-dom';
import Sidebar from './Sidebar';
import Header from './Header';
import BlurBackgrounds from '../visual/BlurBackground';
import PageContainer from '../common/PageContainer';

const Layout = ({ children }) => {
  const location = useLocation();
  const noLayoutPaths = ['/', '/sign-in', '/sign-up'];
  const isNoLayout = noLayoutPaths.includes(location.pathname);

  return (
    <div className="flex h-screen">
      {!isNoLayout && <Sidebar />}
      <div className="flex flex-col flex-1 relative">
        {!isNoLayout && (
          <div className="absolute top-0 left-0 w-full z-10">
            <Header/>
          </div>
        )}
        <main
          className={`relative flex-1 ${location.pathname === '/' ? '' : 'py-20 px-8'}`}
        >
          {/* 배경 블러 */}
          <div className="absolute inset-0 -z-10 pointer-events-none overflow-hidden bg-indigo-100">
            <BlurBackgrounds/>
          </div>

          {/* 콘텐츠 영역 */}
          <div className="relative z-0">
            {isNoLayout ? children : <PageContainer>{children}</PageContainer>}
          </div>
        </main>
      </div>
    </div>
  );
};

export default Layout;