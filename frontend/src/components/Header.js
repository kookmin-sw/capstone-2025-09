// components/Header.js
import React from "react";
import { UserCircle } from "lucide-react";
import { useNavigate } from "react-router-dom";

const Header = () => {
    const navigate = useNavigate();

    return (
        <header className="flex items-center justify-end px-6 py-3 bg-white shadow">
            <button onClick={() => navigate("/mypage")}>
                <UserCircle size={28} />
            </button>
        </header>
    );
};

export default Header;
