import React, { Suspense, lazy } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Layout from './components/layout/Layout';
import Landing from './pages/Landing';

// Lazy-loaded 페이지 컴포넌트
const SignIn = lazy(() => import('./pages/SignIn'));
const SignUp = lazy(() => import('./pages/SignUp'));
const BasicVoice = lazy(() => import('./pages/BasicVoice'));
const Index = lazy(() => import('./pages/ai-assistant'));
const VoiceCreate = lazy(() => import('./pages/VoiceCreate'));
const VoiceStore = lazy(() => import('./pages/VoiceStore'));
const MyPage = lazy(() => import('./pages/mypage/index'));
const Quote = lazy(() => import('./pages/Quote'));
const JoinAgreement = lazy(() => import('./pages/JoinAgreement'));
const RememberVoice = lazy(() => import('./pages/RememberVoice'));

function App() {
  return (
    <Router>
      <Suspense fallback={<div>로딩 중...</div>}>
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
            <Route path="/quote" element={<Quote />} />
            <Route path="/join-agreement" element={<JoinAgreement />} />
            <Route path="/remember-voice" element={<RememberVoice />} />
          </Routes>
        </Layout>
      </Suspense>
    </Router>
  );
}

export default App;
