import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { createFFmpeg, fetchFile } from '@ffmpeg/ffmpeg';
import Logo from '../icons/covosLogo.svg';

function VoiceCreate() {
  const [isRecording, setIsRecording] = useState(false);
  const [voicePackName, setVoicePackName] = useState('');
  const [timer, setTimer] = useState(0);
  const [audioBlob, setAudioBlob] = useState(null);
  const [isFFmpegLoaded, setIsFFmpegLoaded] = useState(false);
  const [isPlaying, setIsPlaying] = useState(false);

  const ffmpegRef = useRef(null);
  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);
  const timerRef = useRef(null);
  const navigate = useNavigate();

  const audioContextRef = useRef(null);
  const analyserRef = useRef(null);
  const animationRef = useRef(null);
  const volumeCanvasRef = useRef(null);
  const audioPlayerRef = useRef(null);

  useEffect(() => {
    const loadFFmpeg = async () => {
      const ffmpeg = createFFmpeg({ log: false });
      await ffmpeg.load();
      ffmpegRef.current = ffmpeg;
      setIsFFmpegLoaded(true);
    };
    loadFFmpeg();
  }, []);

  // 볼륨 막대 그리기
  const drawVolumeMeter = () => {
    const canvas = volumeCanvasRef.current;
    const ctx = canvas.getContext('2d');
    const analyser = analyserRef.current;
    const dataArray = new Uint8Array(analyser.frequencyBinCount);

    const draw = () => {
      analyser.getByteFrequencyData(dataArray);
      const volume = dataArray.reduce((sum, val) => sum + val, 0) / dataArray.length;

      const width = canvas.width;
      const height = canvas.height;

      ctx.clearRect(0, 0, width, height);
      ctx.fillStyle = '#7C3AED';
      ctx.fillRect(0, 0, (volume / 255) * width, height);

      animationRef.current = requestAnimationFrame(draw);
    };

    draw();
  };

  const stopVolumeMeter = () => {
    if (animationRef.current) {
      cancelAnimationFrame(animationRef.current);
    }
    const ctx = volumeCanvasRef.current.getContext('2d');
    ctx.clearRect(0, 0, volumeCanvasRef.current.width, volumeCanvasRef.current.height);
  };

  const handleStartRecording = async () => {
    if (!isFFmpegLoaded) return alert('FFmpeg 로딩 중입니다.');

    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    setAudioBlob(null);
    setTimer(0);
    audioChunksRef.current = [];

    // 오디오 분석기 연결
    audioContextRef.current = new AudioContext();
    const source = audioContextRef.current.createMediaStreamSource(stream);
    analyserRef.current = audioContextRef.current.createAnalyser();
    analyserRef.current.fftSize = 256;
    source.connect(analyserRef.current);

    drawVolumeMeter();

    mediaRecorderRef.current = new MediaRecorder(stream, { mimeType: 'audio/webm' });
    mediaRecorderRef.current.ondataavailable = (e) => {
      audioChunksRef.current.push(e.data);
    };
    mediaRecorderRef.current.onstop = async () => {
      clearInterval(timerRef.current);
      stopVolumeMeter();
      audioContextRef.current?.close();

      const webmBlob = new Blob(audioChunksRef.current, { type: 'audio/webm' });

      try {
        const ffmpeg = ffmpegRef.current;
        const file = new File([webmBlob], 'input.webm', { type: 'audio/webm' });
        ffmpeg.FS('writeFile', 'input.webm', await fetchFile(file));
        await ffmpeg.run('-i', 'input.webm', 'output.wav');
        const data = ffmpeg.FS('readFile', 'output.wav');
        const wavBlob = new Blob([data.buffer], { type: 'audio/wav' });
        setAudioBlob(wavBlob);
      } catch (err) {
        console.error('WAV 변환 오류:', err);
      }
    };

    mediaRecorderRef.current.start();
    setIsRecording(true);
    timerRef.current = setInterval(() => setTimer((prev) => prev + 1), 1000);
  };

  const handleStopRecording = () => {
    mediaRecorderRef.current?.stop();
    setIsRecording(false);
  };

  const handleCreateVoicePack = async () => {
    if (!voicePackName.trim() || !audioBlob) return alert('이름과 녹음이 필요합니다.');

    const formData = new FormData();
    formData.append('userId', sessionStorage.getItem('userId'));
    formData.append('name', voicePackName);
    formData.append('voiceFile', new File([audioBlob], 'voice.wav', { type: 'audio/wav' }));

    try {
      const res = await fetch(`${process.env.REACT_APP_VOICEPACK_API_URL}/convert`, {
        method: 'POST',
        body: formData,
        credentials: 'include',
      });
      if (!res.ok) throw new Error();
      alert('보이스팩 생성 완료!');
      navigate('/voicestore');
    } catch (err) {
      console.error('업로드 실패:', err);
    }
  };

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-[#f5f4ff] px-4 py-8">
      <div className="mb-8 cursor-pointer" onClick={() => navigate('/landing')}>
        <img src={Logo} alt="Logo"/>
      </div>
      <div className="w-full max-w-2xl bg-white shadow-lg rounded-xl p-8">
        <label className="block text-gray-700 text-xl font-semibold mb-2">보이스팩 이름</label>
        <input
          value={voicePackName}
          onChange={(e) => setVoicePackName(e.target.value)}
          className="w-full px-4 py-2 border rounded-md mb-6"
          placeholder="보이스팩 이름 입력"
        />

        <h2 className="text-xl font-bold text-gray-900 mb-2">보이스팩 샘플 녹음</h2>
        <p className="text-sm text-gray-500 mb-4">녹음 버튼을 누르고 각 문장을 따라 읽어주세요.</p>

        <div className="bg-[#f5f4ff] rounded-xl p-6">
          <p className="text-lg font-medium text-gray-800 mb-4">
            “안녕하세요. 목소리를 제공합니다. 잘 들리시나요? 감사합니다.”
          </p>

          <div className="flex items-center space-x-4">
            <button
              onClick={isRecording ? handleStopRecording : handleStartRecording}
              className={`w-12 h-12 rounded-full flex items-center justify-center text-white text-lg transition-colors duration-300 ${
                isRecording ? 'bg-[#7C3AED]' : 'bg-gray-300'
              }`}
              disabled={!isFFmpegLoaded}
            >
              🎤
            </button>

            <canvas
              ref={volumeCanvasRef}
              width={300}
              height={20}
              className="bg-white rounded border"
            />

            <span className="text-sm w-20 text-right text-[#7C3AED]">
              {String(Math.floor(timer / 60)).padStart(2, '0')} : {String(timer % 60).padStart(2, '0')}
            </span>
          </div>

          {/* ▶️ 녹음 완료 후 재생 버튼 */}
          {audioBlob && (
            <div className="mt-4 text-right">
              <audio
                ref={audioPlayerRef}
                src={URL.createObjectURL(audioBlob)}
                controls
                onEnded={() => setIsPlaying(false)}
                className="w-full mt-2"
              />
            </div>
          )}
        </div>

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


