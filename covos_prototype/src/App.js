import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import './App.css';
import SignIn from "./pages/SignIn";
import SignUp from "./pages/SignUp"
import Landing from "./pages/Landing";
import VoiceCreate from "./pages/VoiceCreate";
import VoiceStore from "./pages/VoiceStore";
import BasicVoice from "./pages/BasicVoice";


function App() {
  return (
      <Router>
        <Routes>
            <Route path="/signin" element={<SignIn />} />
            <Route path="/signup" element={<SignUp/>}/>
            <Route path="/landing" element={<Landing />} />
            <Route path="/voicecreate" element={<VoiceCreate />} />
            <Route path="/voicestore" element={<VoiceStore />} />
            <Route path="/basicvoice" element={<BasicVoice />} />
          {/* 기본 경로를 로그인 페이지로 설정 */}
            <Route path="*" element={<SignIn />} />
        </Routes>
      </Router>
  );
}

export default App;
