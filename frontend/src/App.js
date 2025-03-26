import React from 'react';
import {
  BrowserRouter as Router,
  Routes,
  Route,
  useLocation,
} from 'react-router-dom';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import Landing from './pages/Landing';
import SignIn from './pages/SignIn';
import SignUp from './pages/SignUp';
import BasicVoice from './pages/BasicVoice';
import AiAssistant from './pages/AiAssistant';
import VoiceCreate from './pages/VoiceCreate';
import VoiceStore from './pages/VoiceStore';
import MyPage from './pages/MyPage';

const Layout = ({ children }) => {
  const location = useLocation();
  const noLayoutPaths = ['/', '/sign-in', '/sign-up'];
  const isNoLayout = noLayoutPaths.includes(location.pathname);

  return (
    <div className="flex h-screen">
      {!isNoLayout && <Sidebar />}
      <div className="flex flex-col flex-1">
        {!isNoLayout && <Header />}
        <main
          className={`${location.pathname === '/' ? '' : 'p-4'} overflow-auto`}
        >
          {children}
        </main>
      </div>
    </div>
  );
};

function App() {
  return (
    <Router>
      <Layout>
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/sign-in" element={<SignIn />} />
          <Route path="/sign-up" element={<SignUp />} />
          <Route path="/basic-voice" element={<BasicVoice />} />
          <Route path="/ai-assistant" element={<AiAssistant />} />
          <Route path="/voice-create" element={<VoiceCreate />} />
          <Route path="/voice-store" element={<VoiceStore />} />
          <Route path="/mypage" element={<MyPage />} />
        </Routes>
      </Layout>
    </Router>
  );
}

export default App;
