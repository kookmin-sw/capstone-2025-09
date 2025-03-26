// App.js
import React from "react";
import { BrowserRouter as Router, Routes, Route, useLocation } from "react-router-dom";
import Sidebar from "./components/Sidebar";
import Header from "./components/Header";
import Landing from "./pages/Landing";
import Login from "./pages/Login";
import Register from "./pages/Register";
import BasicVoice from "./pages/BasicVoice";
import AiAssistant from "./pages/AiAssistant";
import VoiceCreate from "./pages/VoiceCreate";
import VoiceStore from "./pages/VoiceStore";
import MyPage from "./pages/MyPage"; // ✅ 추가

const Layout = ({ children }) => {
    const location = useLocation();
    const noLayoutPaths = ["/", "/login", "/register"];
    const isNoLayout = noLayoutPaths.includes(location.pathname);

    return (
        <div className="flex h-screen">
            {!isNoLayout && <Sidebar />}
            <div className="flex flex-col flex-1">
                {!isNoLayout && <Header />}
                <main className={`${location.pathname === '/' ? '' : 'p-4'} overflow-auto`}>
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
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="/basic-voice" element={<BasicVoice />} />
                    <Route path="/ai-assistant" element={<AiAssistant />} />
                    <Route path="/voice-create" element={<VoiceCreate />} />
                    <Route path="/voice-store" element={<VoiceStore />} />
                    <Route path="/mypage" element={<MyPage />} /> {/* ✅ 마이페이지 라우트 추가 */}
                </Routes>
            </Layout>
        </Router>
    );
}

export default App;
