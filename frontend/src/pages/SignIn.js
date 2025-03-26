import React from "react";
import { useNavigate } from "react-router-dom";

const SignIn = () => {
    const navigate = useNavigate();

    const handleLogin = () => {
        navigate("/voice-store");
    };

    return (
        <div className="max-w-sm mx-auto p-8 space-y-6">
            <h1 className="text-2xl font-bold text-center">로그인</h1>
            <input type="text" placeholder="아이디" className="w-full border px-3 py-2 rounded" />
            <input type="password" placeholder="비밀번호" className="w-full border px-3 py-2 rounded" />

            <button
                onClick={() => navigate("/register")}
                className="text-blue-500 underline text-sm text-center w-full"
            >
                회원가입 하기
            </button>

            <button
                onClick={handleLogin}
                className="w-full bg-purple-500 text-white py-2 rounded"
            >
                로그인
            </button>
        </div>
    );
};

export default SignIn;
