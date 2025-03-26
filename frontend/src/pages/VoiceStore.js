import React from "react";

const VoiceStore = () => {
    const voices = Array.from({ length: 12 }, (_, i) => `보이스팩 이름 ${i + 1}`);

    return (
        <div>
            <h1 className="text-2xl font-bold text-center mb-4">보이스팩 구매</h1>
            <div className="flex justify-between mb-4">
                <input className="w-full max-w-md px-4 py-2 border rounded bg-gray-100" placeholder="검색창" />
                <button className="ml-4 text-sm">정렬</button>
            </div>
            <div className="grid grid-cols-5 gap-4">
                {voices.map((name, index) => (
                    <div key={index} className="bg-gray-800 text-white p-4 rounded text-center">
                        <div className="mb-1 text-sm">{name}</div>
                        <div className="mb-2 text-yellow-300">🪙 4,000</div>
                        <div className="flex justify-center gap-2">
                            <button>▶️</button>
                            <button>$</button>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default VoiceStore;