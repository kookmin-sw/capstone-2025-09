// components/Sidebar.js
import React from "react";
import { Link, useLocation } from "react-router-dom";
import { Mic, Store } from "lucide-react";

const Sidebar = () => {
    const location = useLocation();

    const isActive = (path) =>
        location.pathname.startsWith(path) ? "bg-gray-700 text-white" : "bg-gray-300";

    return (
        <div className="w-48 bg-gray-200 p-4 space-y-4">
            <Link to="/" className="block text-center font-bold text-xl mb-4">
                로고
            </Link>
            <Link
                to="/basic-voice"
                className={`flex items-center space-x-2 p-2 rounded ${isActive("/basic-voice")}`}
            >
                <Mic size={20} />
                <span>베이직 보이스</span>
            </Link>
            <Link
                to="/ai-assistant"
                className={`flex items-center space-x-2 p-2 rounded ${isActive("/ai-assistant")}`}
            >
                <Store size={20} />
                <span>AI 비서</span>
            </Link>
        </div>
    );
};

export default Sidebar;