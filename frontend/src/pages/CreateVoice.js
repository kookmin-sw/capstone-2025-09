import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';

function CreateVoice() {
  const [isRecording, setIsRecording] = useState(false); //녹음 중인지 여부 확인
  const [audioUrls, setAudioUrls] = useState([null, null]); //녹음된 오디오 파일의 URL 저장
  const [recordingIndex, setRecordingIndex] = useState(null); //현재 녹음 중인 인덱스
  const [timer, setTimer] = useState(0); //녹음 시간 측정
  const [voicePackName, setVoicePackName] = useState('');//보이스팩 이름
  const mediaRecorderRef = useRef(null); //미디어 녹음기 참조
  const audioChunksRef = useRef([]); //오디오 청크 저장
  const timerRef = useRef(null); //타이머 참조
  const navigate = useNavigate(); //네비게이트 함수 참조.

  const handleStartRecording = async (index) => {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    mediaRecorderRef.current = new MediaRecorder(stream);
    audioChunksRef.current = [];
    setRecordingIndex(index);
    setTimer(0);

    mediaRecorderRef.current.ondataavailable = (event) => {
      audioChunksRef.current.push(event.data);
    };

    mediaRecorderRef.current.onstop = () => {
      const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/wav' });
      const url = URL.createObjectURL(audioBlob);
      setAudioUrls((prev) => {
        const newUrls = [...prev];
        newUrls[index] = url;
        return newUrls;
      });
      clearInterval(timerRef.current);
    };

    mediaRecorderRef.current.start();
    setIsRecording(true);

    timerRef.current = setInterval(() => {
      setTimer((prev) => prev + 1);
    }, 1000);
  };

  const handleStopRecording = () => {
    mediaRecorderRef.current.stop();
    setIsRecording(false);
    setRecordingIndex(null);
  };

  const handleCreateVoicePack = () => {
    if (!voicePackName.trim() || !audioUrls.some((url) => url)) {
      alert('보이스팩 이름과 최소 하나 이상의 녹음을 완료해야 합니다.');
      return;
    }

    // 보이스팩 데이터를 로컬스토리지에 저장
    const savedVoicePacks = JSON.parse(localStorage.getItem('voicePacks')) || [];
    const newVoicePack = {
      name: voicePackName,
      audios: audioUrls.filter((url) => url !== null),
    };
    localStorage.setItem('voicePacks', JSON.stringify([...savedVoicePacks, newVoicePack]));

    // voiceMarket 페이지로 이동
    navigate('/voicemarket');
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-white p-4">
      <h1 className="text-3xl font-bold mb-8">보이스팩 생성</h1>

      <div className="w-full max-w-md">
        <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="voicepackName">
          보이스팩 이름 <span className="text-red-500">*</span>
        </label>
        <input
          id="voicepackName"
          type="text"
          placeholder="보이스팩 이름 입력"
          value={voicePackName}
          onChange={(e) => setVoicePackName(e.target.value)}
          className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
        />
      </div>

      <div className="w-full max-w-md mt-6">
        <h2 className="text-lg font-semibold mb-2">
          보이스팩 샘플 녹음 <span className="text-red-500">*</span>
        </h2>
        <p className="text-sm text-gray-500 mb-4">
          녹음 버튼을 누르고 각 문장을 따라 읽어주세요.
        </p>

        {[1, 2].map((item, index) => (
          <div key={item} className="mb-4">
            <p className="mb-2">
              {item}. 동해물과 백두산이 마르고 닳도록 하느님이 보우하사 우리나라 만세
              무궁화 삼천리 화려 강산 대한 사람 대한으로 길이 보전하세
            </p>
            <div className="flex items-center">
              <button
                onClick={() => (isRecording ? handleStopRecording() : handleStartRecording(index))}
                className={`bg-gray-200 p-2 rounded-full mr-2 ${isRecording && recordingIndex === index ? 'bg-red-500' : ''}`}
              >
                <span role="img" aria-label="microphone">
                  🎤
                </span>
              </button>
              {audioUrls[index] && <audio src={audioUrls[index]} controls className="mr-2" />}
              {isRecording && recordingIndex === index && <span className="text-sm">{timer}s</span>}
            </div>
          </div>
        ))}
      </div>

      <button
        onClick={handleCreateVoicePack}
        className="bg-purple-500 text-white font-bold py-2 px-4 rounded mt-6"
      >
        생성
      </button>
    </div>
  );
}

export default CreateVoice;
