import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

import Layout from './components/layout/Layout';
import Landing from './pages/Landing';
import SignIn from './pages/SignIn';
import SignUp from './pages/SignUp';
import BasicVoice from './pages/BasicVoice';
import Index from './pages/ai-assistant';
import VoiceCreate from './pages/VoiceCreate';
import VoiceStore from './pages/VoiceStore';
import MyPage from './pages/mypage/index';

function App() {
  return (
    <Router>
      <Layout>
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/sign-in" element={<SignIn />} />
          <Route path="/sign-up" element={<SignUp />} />
          <Route path="/basic-voice" element={<BasicVoice />} />
          <Route path="/ai-assistant" element={<Index />} />
          <Route path="/voice-create" element={<VoiceCreate />} />
          <Route path="/voice-store" element={<VoiceStore />} />
          <Route path="/mypage" element={<MyPage />} />
        </Routes>
      </Layout>
    </Router>
  );
}

export default App;
