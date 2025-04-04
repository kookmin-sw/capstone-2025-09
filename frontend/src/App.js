import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';

import Layout from './layout/Layout';
import Landing from './pages/Landing';
import SignIn from './pages/SignIn';
import SignUp from './pages/SignUp';
import BasicVoice from './pages/BasicVoice';
import AiAssistant from './pages/AiAssistant';
import VoiceCreate from './pages/VoiceCreate';
import VoiceStore from './pages/VoiceStore';
import MyPage from './pages/MyPage';

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
