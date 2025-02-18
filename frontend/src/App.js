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
            <Route path="/" element={<LoginPage />} />
            <Route path="/landingpage" element={<LandingPage />} />
            <Route path="/createvoice" element={<CreateVoice />} />
            <Route path="/voicemarket" element={<VoiceMarket />} />
        </Routes>
      </Router>
  );
}

export default App;
