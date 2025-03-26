import React from "react";
import { useNavigate } from "react-router-dom";

const Register = () => {
    const navigate = useNavigate();

    const handleRegister = () => {
        navigate("/login");
    };

    return (
        <div className="max-w-md mx-auto p-8 space-y-4">
            <h1 className="text-2xl font-bold text-center">회원가입</h1>
            <input type="text" placeholder="이름" className="w-full border px-3 py-2 rounded" />
            <input type="text" placeholder="아이디" className="w-full border px-3 py-2 rounded" />
            <input type="password" placeholder="비밀번호" className="w-full border px-3 py-2 rounded" />
            <input type="password" placeholder="비밀번호 확인" className="w-full border px-3 py-2 rounded" />
            <button onClick={handleRegister} className="w-full bg-purple-500 text-white py-2 rounded">회원가입</button>
        </div>
    );
};

export default Register;