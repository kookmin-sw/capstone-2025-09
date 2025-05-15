import React, { useState, useEffect } from 'react';
import AssistantSetup from './AssistantSetup';
import ScriptPlayer from './ScriptPlayer';
import AssistantReadyScreen from './AssistantReadyScreen';

const AiAssistant = () => {
  const [step, setStep] = useState('setup'); // setup → ready → play

  useEffect(() => {
    const config = localStorage.getItem('ai-assistant-config');
    if (config) setStep('ready');
  }, []);

  const handleConfigured = () => setStep('ready');
  const handleEdit = () => setStep('setup');
  const handlePlay = () => setStep('play');

  if (step === 'setup')
    return <AssistantSetup setIsConfigured={handleConfigured} />;
  if (step === 'ready')
    return <AssistantReadyScreen onStart={handlePlay} onEdit={handleEdit} />;
  return <ScriptPlayer onEdit={handleEdit} />;
};

export default AiAssistant;
