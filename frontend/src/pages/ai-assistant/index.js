// src/pages/ai-assistant/index.js
import React, { useState, useEffect } from 'react';
import AssistantSetup from './AssistantSetup';
import ScriptPlayer from './ScriptPlayer';

const AiAssistant = () => {
  const [isConfigured, setIsConfigured] = useState(false);

  useEffect(() => {
    const config = localStorage.getItem('ai-assistant-config');
    if (config) setIsConfigured(true);
  }, []);

  const handleEdit = () => {
    setIsConfigured(false); // 세팅 화면으로 되돌리기
  };

  return isConfigured ? (
    <ScriptPlayer onEdit={handleEdit} />
  ) : (
    <AssistantSetup setIsConfigured={setIsConfigured} />
  );
};

export default AiAssistant;
