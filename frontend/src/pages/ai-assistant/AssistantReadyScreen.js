// src/pages/ai-assistant/AssistantReadyScreen.js
import React from 'react';
import GradientButton from '../../components/common/GradientButton';
//import { ReactComponent as AssistantIcon } from '../../assets/ai-assistant.svg'; // 있으면 사용

const AssistantReadyScreen = ({ onStart, onEdit }) => {
  return (
    <div className="flex flex-col items-center justify-center my-40 text-center space-y-6">
      {/* AI 비서 아이콘 or 이미지 */}
      {/*<div className="w-40 h-40">*/}
      {/*  <AssistantIcon className="w-full h-full" />*/}
      {/*  /!* 이미지인 경우:*/}
      {/*  <img src="/images/assistant.png" alt="AI 비서" className="w-full h-full object-contain" />*/}
      {/*  *!/*/}
      {/*</div>*/}

      <h1 className="text-2xl font-bold">AI 비서가 준비되었어요!</h1>
      <p className="text-gray-500 text-sm">
        설정된 정보로 오늘의 스크립트를 준비했어요.
      </p>

      <div className="flex flex-col gap-4">
        <GradientButton className="px-6 py-3" onClick={onStart}>
          오늘의 비서 이용하기
        </GradientButton>
        <button
          onClick={onEdit}
          className="text-sm underline text-gray-500 hover:text-gray-700"
        >
          설정 수정
        </button>
      </div>
    </div>
  );
};

export default AssistantReadyScreen;
