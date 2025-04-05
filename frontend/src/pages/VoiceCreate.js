import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { createFFmpeg, fetchFile } from '@ffmpeg/ffmpeg';
import WaveSurfer from 'wavesurfer.js';

function VoiceCreate() {
  const [isRecording, setIsRecording] = useState(false);
  const [voicePackName, setVoicePackName] = useState('');
  const [timer, setTimer] = useState(0);
  const [audioBlob, setAudioBlob] = useState(null);
  const [isFFmpegLoaded, setIsFFmpegLoaded] = useState(false);

  const ffmpegRef = useRef(null);
  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);
  const timerRef = useRef(null);
  const navigate = useNavigate();

  const wavesurferRef = useRef(null);
  const waveformContainerRef = useRef(null);

  // FFmpeg 초기 로딩
  useEffect(() => {
    const loadFFmpeg = async () => {
      const ffmpegInstance = createFFmpeg({ log: false });
      await ffmpegInstance.load();
      ffmpegRef.current = ffmpegInstance;
      setIsFFmpegLoaded(true);
    };
    loadFFmpeg();
  }, []);

  // WAV 오디오 설정 및 파형 표시
  useEffect(() => {
    if (audioBlob && waveformContainerRef.current) {
      if (wavesurferRef.current) {
        wavesurferRef.current.destroy();
      }

      const wavesurfer = WaveSurfer.create({
        container: waveformContainerRef.current,
        waveColor: '#ddd',
        progressColor: '#7C3AED',
        height: 60,
        barWidth: 2,
        barGap: 2,
        responsive: true,
      });

      wavesurfer.loadBlob(audioBlob);
      wavesurferRef.current = wavesurfer;
    }
  }, [audioBlob]);

  // 녹음 시작
  const handleStartRecording = async () => {
    if (!isFFmpegLoaded) return alert("FFmpeg가 아직 로드되지 않았습니다.");

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });

      // 이전 상태 초기화
      setAudioBlob(null);
      setTimer(0);
      audioChunksRef.current = [];

      mediaRecorderRef.current = new MediaRecorder(stream, { mimeType: 'audio/webm' });

      mediaRecorderRef.current.ondataavailable = (e) => {
        audioChunksRef.current.push(e.data);
      };

      mediaRecorderRef.current.onstop = async () => {
        const webmBlob = new Blob(audioChunksRef.current, { type: 'audio/webm' });
        clearInterval(timerRef.current);

        try {
          const ffmpeg = ffmpegRef.current;
          const webmFile = new File([webmBlob], 'audio.webm', { type: 'audio/webm' });
          ffmpeg.FS('writeFile', 'input.webm', await fetchFile(webmFile));
          await ffmpeg.run('-i', 'input.webm', 'output.wav');
          const wavData = ffmpeg.FS('readFile', 'output.wav');
          const wavBlob = new Blob([wavData.buffer], { type: 'audio/wav' });
          setAudioBlob(wavBlob);
        } catch (err) {
          console.error("WAV 변환 실패:", err);
        }
      };

      mediaRecorderRef.current.start();
      setIsRecording(true);
      timerRef.current = setInterval(() => setTimer((prev) => prev + 1), 1000);
    } catch (err) {
      console.error('녹음 오류:', err);
    }
  };

  // 녹음 정지
  const handleStopRecording = () => {
    mediaRecorderRef.current?.stop();
    setIsRecording(false);
  };

  // 보이스팩 생성 API 호출
  const handleCreateVoicePack = async () => {
    if (!voicePackName.trim() || !audioBlob) return alert('이름과 녹음이 필요합니다.');

    const apiUrl = process.env.REACT_APP_VOICEPACK_API_URL;
    const formData = new FormData();
    formData.append('userId', sessionStorage.getItem('userId'));
    formData.append('name', voicePackName);
    formData.append('voiceFile', new File([audioBlob], 'voice.wav', { type: 'audio/wav' }));

    try {
      const res = await fetch(`${apiUrl}/convert`, {
        method: 'POST',
        body: formData,
        credentials: 'include',
      });
      if (!res.ok) throw new Error('서버 오류');
      alert('보이스팩 생성 완료!');
      navigate('/voicestore');
    } catch (err) {
      console.error('업로드 실패:', err);
    }
  };

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-[#f5f4ff] px-4 py-8">
      <div className="w-full max-w-2xl bg-white shadow-lg rounded-xl p-8">
        <div className="mb-8">
          <label className="block text-gray-700 text-xl font-semibold mb-2">보이스팩 이름</label>
          <input
            value={voicePackName}
            onChange={(e) => setVoicePackName(e.target.value)}
            className="w-full px-4 py-2 border rounded-md"
            placeholder="보이스팩 이름 입력"
          />
        </div>

        <h2 className="text-xl font-bold text-gray-900 mb-2">보이스팩 샘플 녹음</h2>
        <p className="text-sm text-gray-500 mb-4">녹음 버튼을 누르고 각 문장을 따라 읽어주세요.</p>

        <div className="bg-[#f5f4ff] rounded-xl p-6">
          <p className="text-lg font-medium text-gray-800 mb-4">
            “안녕하세요. 목소리를 제공합니다. 잘 들리시나요? 감사합니다.”
          </p>

          <div className="flex items-center space-x-4">
            {/* 🎤 녹음 버튼 */}
            <button
              onClick={isRecording ? handleStopRecording : handleStartRecording}
              className={`w-12 h-12 rounded-full flex items-center justify-center text-white text-lg transition-colors duration-300 ${
                isRecording ? 'bg-[#7C3AED]' : 'bg-gray-300'
              }`}
              disabled={!isFFmpegLoaded}
            >
              🎤
            </button>

            {/* 📈 오디오 파형 */}
            <div className="flex-1 bg-white rounded-lg overflow-hidden relative h-16 px-2 py-1">
              {audioBlob ? (
                <div ref={waveformContainerRef} className="w-full h-full" />
              ) : (
                <p className="text-gray-400 text-sm flex items-center h-full">녹음 중 파형이 표시됩니다</p>
              )}
            </div>

            {/* ⏱ 타이머 */}
            <span className="text-sm w-20 text-right text-[#7C3AED]">
              {String(Math.floor(timer / 60)).padStart(2, '0')} : {String(timer % 60).padStart(2, '0')}
            </span>
          </div>
        </div>

        {/* 📤 보이스팩 생성 */}
        <button
          onClick={handleCreateVoicePack}
          className="mt-6 w-full py-3 bg-[#7C3AED] text-white rounded-md text-sm font-semibold disabled:opacity-50"
          disabled={!voicePackName.trim() || !audioBlob}
        >
          보이스팩 생성
        </button>
      </div>
    </div>
  );
}

export default VoiceCreate;
