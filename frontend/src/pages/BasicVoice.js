import React from "react";

const BasicVoice = () => {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">베이직 보이스</h1>

      <div className="space-y-2">
        <button className="bg-gray-300 px-4 py-2 rounded">목소리 선택</button>
        <textarea
          placeholder="스크립트를 입력하세요."
          className="w-full h-40 p-4 bg-gray-200 rounded resize-none"
        ></textarea>
        <button className="bg-gray-300 px-4 py-2 rounded">변환 버튼</button>
      </div>

      <div className="flex gap-4 items-center">
        <div className="flex-1 bg-gray-300 px-4 py-4 rounded">재생창</div>
        <button className="bg-gray-300 px-4 py-2 rounded">다운로드 버튼</button>
      </div>
    </div>
  );
};

export default BasicVoice;