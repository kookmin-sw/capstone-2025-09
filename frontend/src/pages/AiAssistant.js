import React from "react";

const AiAssistant = () => {
    return (
        <div className="space-y-6">
            <h1 className="text-2xl font-bold">AI 비서</h1>
            <div className="flex gap-4">
                <button className="px-4 py-2 bg-gray-300">재생버튼</button>
                <div className="flex-1 px-4 py-2 bg-gray-300">재생창</div>
            </div>
            <div className="grid grid-cols-2 gap-4 bg-gray-300 p-4">
                {Array.from({ length: 4 }).map((_, i) => (
                    <button key={i} className="border rounded py-4">정보 출처</button>
                ))}
            </div>
        </div>
    );
};

export default AiAssistant;