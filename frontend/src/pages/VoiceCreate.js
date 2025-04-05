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
      console.log("✅ FFmpeg 로드 완료!");
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
      ctx.strokeStyle = '#6B21A8'; // 진한 보라색
      ctx.beginPath();

      const sliceWidth = canvas.width / bufferLength;
      let x = 0;

      for (let i = 0; i < bufferLength; i++) {
        const v = dataArray[i] / 128.0;
        const y = (v * canvas.height) / 2;

        if (i === 0) {
          ctx.moveTo(x, y);
        } else {
          ctx.lineTo(x, y);
        }

        x += sliceWidth;
      }

      ctx.lineTo(canvas.width, canvas.height / 2);
      ctx.stroke();

      animationFrameRef.current = requestAnimationFrame(draw);
    };

    draw();
  };

  const handleStartRecording = async () => {
    if (!isFFmpegLoaded) {
      alert("FFmpeg가 아직 로드되지 않았습니다. 잠시만 기다려주세요.");
      return;
    }

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

      mediaRecorderRef.current.ondataavailable = (event) => {
        audioChunksRef.current.push(event.data);
      };

      mediaRecorderRef.current.onstop = async () => {
        const webmBlob = new Blob(audioChunksRef.current, { type: 'audio/webm' });
        clearInterval(timerRef.current);
        cancelAnimationFrame(animationFrameRef.current);
        if (audioContextRef.current) {
          audioContextRef.current.close();
        }

        try {
          const ffmpeg = ffmpegRef.current;
          const webmFile = new File([webmBlob], 'audio.webm', { type: 'audio/webm' });

          ffmpeg.FS('writeFile', 'input.webm', await fetchFile(webmFile));
          await ffmpeg.run('-i', 'input.webm', 'output.wav');
          const wavData = ffmpeg.FS('readFile', 'output.wav');

          const wavBlob = new Blob([wavData.buffer], { type: 'audio/wav' });
          setAudioBlob(wavBlob);
          console.log("✅ WAV 변환 완료!");
        } catch (error) {
          console.error("❌ WAV 변환 실패:", error);
          alert("WAV 변환에 실패했습니다.");
        }
      };

      mediaRecorderRef.current.start();
      setIsRecording(true);
      timerRef.current = setInterval(() => setTimer((prev) => prev + 1), 1000);
    } catch (error) {
      console.error('❌ 오디오 녹음 오류:', error);
      alert('오디오 녹음 중 오류가 발생했습니다.');
    }
  };

  const handleStopRecording = () => {
    if (mediaRecorderRef.current) {
      mediaRecorderRef.current.stop();
      setIsRecording(false);
    }
  };

  const handleCreateVoicePack = async () => {
    if (!voicePackName.trim() || !audioBlob) {
      alert('보이스팩 이름과 녹음을 완료해야 합니다.');
      return;
    }

    const apiUrl = process.env.REACT_APP_VOICEPACK_API_URL;
    const endpoint = `${apiUrl}/convert`;

    try {
      const audioFile = new File([audioBlob], 'voice.wav', { type: 'audio/wav' });
      const userId = sessionStorage.getItem('userId');

      const formData = new FormData();
      formData.append('userId', userId);
      formData.append('name', voicePackName);
      formData.append('voiceFile', audioFile);

      const response = await fetch(endpoint, {
        method: 'POST',
        body: formData,
        credentials: 'include',
      });

      if (!response.ok) throw new Error('서버 응답 실패');

      const data = await response.json();
      alert('보이스팩 생성이 완료되었습니다!');
      navigate('/voicestore');
    } catch (error) {
      console.error('❌ 보이스팩 생성 오류:', error);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-white p-4">
      <h1 className="text-3xl font-bold mb-8">보이스팩 생성</h1>

      <div className="w-full max-w-md">
        <label htmlFor="voicepackName" className="block text-gray-700 text-sm font-bold mb-2">
          보이스팩 이름 <span className="text-red-500">*</span>
        </label>
        <input
          id="voicepackName"
          type="text"
          value={voicePackName}
          onChange={(e) => setVoicePackName(e.target.value)}
          placeholder="보이스팩 이름 입력"
          className="shadow border rounded w-full py-2 px-3 text-gray-700"
        />
      </div>

      <div className="w-full max-w-md mt-6">
        <h2 className="text-lg font-semibold mb-2">
          보이스팩 샘플 녹음 <span className="text-red-500">*</span>
        </h2>
        <p className="text-sm text-gray-500 mb-4">
          안녕하세요. 목소리를 녹음합니다. 잘 들리시나요? 감사합니다..
        </p>

        <div className="mb-4 flex items-center space-x-4">
          <button
            onClick={isRecording ? handleStopRecording : handleStartRecording}
            className={`p-2 rounded-full ${isRecording ? 'bg-red-500 text-white' : 'bg-gray-200'}`}
            disabled={!isFFmpegLoaded}
          >
            🎤
          </button>

          {audioBlob && <audio src={URL.createObjectURL(audioBlob)} controls className="mr-2"/>}
          {isRecording && <span className="text-sm">{timer}s</span>}

          {/* 🎯 파형을 작게 오른쪽에 표시 */}
          <canvas
            ref={canvasRef}
            width={100}
            height={40}
            className="border rounded bg-white"
          />
        </div>
      </div>

      <button
        onClick={handleCreateVoicePack}
        className="bg-purple-500 text-white font-bold py-2 px-4 rounded mt-6 disabled:opacity-50"
        disabled={!voicePackName.trim() || !audioBlob}
      >
        생성
      </button>
    </div>
  );
}

export default VoiceCreate;
