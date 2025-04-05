import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { createFFmpeg, fetchFile } from '@ffmpeg/ffmpeg';

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

  const audioContextRef = useRef(null);
  const analyserRef = useRef(null);
  const animationFrameRef = useRef(null);
  const canvasRef = useRef(null);

  useEffect(() => {
    const loadFFmpeg = async () => {
      const ffmpegInstance = createFFmpeg({ log: true });
      await ffmpegInstance.load();
      ffmpegRef.current = ffmpegInstance;
      setIsFFmpegLoaded(true);
    };
    loadFFmpeg();
  }, []);

  useEffect(() => {
    if (audioBlob) {
      console.log("✅ 변환된 WAV 오디오 타입:", audioBlob.type);
      console.log("✅ 변환된 WAV 오디오 크기:", audioBlob.size, "bytes");
    }
  }, [audioBlob]);

  const drawVisualizer = () => {
    const canvas = canvasRef.current;
    const ctx = canvas.getContext("2d");
    const analyser = analyserRef.current;

    const bufferLength = analyser.fftSize;
    const dataArray = new Uint8Array(bufferLength);

    const draw = () => {
      analyser.getByteTimeDomainData(dataArray);

      ctx.clearRect(0, 0, canvas.width, canvas.height);

      ctx.lineWidth = 2;
      ctx.strokeStyle = '#7C3AED';
      ctx.beginPath();

      const sliceWidth = canvas.width / bufferLength;
      let x = 0;

      for (let i = 0; i < bufferLength; i++) {
        const v = dataArray[i] / 128.0;
        const y = (v * canvas.height) / 2;

        if (i === 0) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);

        x += sliceWidth;
      }

      ctx.lineTo(canvas.width, canvas.height / 2);
      ctx.stroke();
      animationFrameRef.current = requestAnimationFrame(draw);
    };

    draw();
  };

  const handleStartRecording = async () => {
    if (!isFFmpegLoaded) return alert("FFmpeg가 아직 로드되지 않았습니다.");

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      mediaRecorderRef.current = new MediaRecorder(stream, { mimeType: 'audio/webm' });

      audioContextRef.current = new AudioContext();
      const source = audioContextRef.current.createMediaStreamSource(stream);
      analyserRef.current = audioContextRef.current.createAnalyser();
      analyserRef.current.fftSize = 2048;
      source.connect(analyserRef.current);

      drawVisualizer();

      audioChunksRef.current = [];
      setTimer(0);

      mediaRecorderRef.current.ondataavailable = (e) => audioChunksRef.current.push(e.data);
      mediaRecorderRef.current.onstop = async () => {
        const webmBlob = new Blob(audioChunksRef.current, { type: 'audio/webm' });
        clearInterval(timerRef.current);
        cancelAnimationFrame(animationFrameRef.current);
        audioContextRef.current?.close();

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

  const handleStopRecording = () => {
    mediaRecorderRef.current?.stop();
    setIsRecording(false);
  };

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
        <h2 className="text-xl font-bold text-gray-900 mb-2">보이스팩 샘플 녹음</h2>
        <p className="text-sm text-gray-500 mb-4">녹음 버튼을 누르고 각 문장을 따라 읽어주세요.</p>

        <div className="bg-[#f5f4ff] rounded-xl p-6">
          <p className="text-lg font-medium text-gray-800 mb-4">
            “안녕하세요. 목소리를 제공합니다. 잘 들리시나요? 감사합니다.”
          </p>

          <div className="flex items-center space-x-4">
            <button
              onClick={isRecording ? handleStopRecording : handleStartRecording}
              className="w-12 h-12 rounded-full flex items-center justify-center bg-[#7C3AED] text-white text-lg"
              disabled={!isFFmpegLoaded}
            >
              🎤
            </button>

            <div className="flex-1 bg-white rounded-full overflow-hidden relative h-14 flex items-center px-4">
              {audioBlob ? (
                <audio
                  src={URL.createObjectURL(audioBlob)}
                  controls
                  className="w-full h-8"
                />
              ) : (
                <canvas ref={canvasRef} width={500} height={40} className="w-full h-10" />
              )}
            </div>

            <span className="text-sm w-20 text-right text-[#7C3AED]">
              {String(timer).padStart(2, '0')} : 00
            </span>
          </div>
        </div>

        <div className="mt-8">
          <label className="block text-gray-700 text-sm font-semibold mb-2">보이스팩 이름</label>
          <input
            value={voicePackName}
            onChange={(e) => setVoicePackName(e.target.value)}
            className="w-full px-4 py-2 border rounded-md"
            placeholder="보이스팩 이름 입력"
          />
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