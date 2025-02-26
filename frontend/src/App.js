import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import './App.css';
import LoginPage from "./pages/LoginPage";
import LandingPage from "./pages/LandingPage";
import CreateVoice from "./pages/CreateVoice";
import VoiceMarket from "./pages/VoiceMarket";

function App() {
  return (
      <Router>
        <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<LoginPage />} />
            <Route path="/landingpage" element={<LandingPage />} />
            <Route path="/createvoice" element={<CreateVoice />} />
            <Route path="/voicemarket" element={<VoiceMarket />} />
            {/* 기본 경로를 로그인 페이지로 설정 */}
            <Route path="*" element={<LoginPage />} />
        </Routes>
      </Router>
  );
}

export default App;
