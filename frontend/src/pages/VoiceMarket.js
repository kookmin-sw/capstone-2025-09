import React, { useEffect, useState } from 'react';

function VoiceMarket() {
  const [voicePacks, setVoicePacks] = useState([]);
  const [playingIndex, setPlayingIndex] = useState(null);

  useEffect(() => {
    const savedVoicePacks = JSON.parse(localStorage.getItem('voicePacks')) || [];
    setVoicePacks(savedVoicePacks);
  }, []);

  const handlePlayAudio = (audioUrl, index) => {
    if (playingIndex !== index) {
      const audio = new Audio(audioUrl);
      audio.play();
      setPlayingIndex(index);

      audio.onended = () => {
        setPlayingIndex(null);
      };
    }
  };

  return (
    <div className="flex flex-col items-center min-h-screen bg-white p-4">
      <h1 className="text-3xl font-bold mb-8">보이스팩 구매</h1>

      <div className="grid grid-cols-4 gap-4">
        {voicePacks.map((pack, index) => (
          <div key={index} className="bg-gray-800 text-white p-4 rounded-lg shadow-md">
            <h2 className="text-lg font-semibold">{pack.name}</h2>
            <div className="flex flex-wrap mt-2 space-x-2">
              {pack.audios.map((audioUrl, i) => (
                <button
                  key={i}
                  onClick={() => handlePlayAudio(audioUrl, `${index}-${i}`)}
                  className="bg-gray-500 text-white px-3 py-2 rounded-full focus:outline-none hover:bg-gray-600"
                >
                  {playingIndex === `${index}-${i}` ? '⏸️ ' : '▶️'}
                </button>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default VoiceMarket;
