import React, { useState, useRef, useEffect } from 'react';
import { Mic, Play, Pause } from 'lucide-react';
import { FFmpeg } from '@ffmpeg/ffmpeg';
import WaveSurfer from 'wavesurfer.js';
import MicrophonePlugin from 'wavesurfer.js/dist/plugin/wavesurfer.microphone';

function VoiceCreate() {
  const [isRecording, setIsRecording] = useState(false);
  const [voicePackName, setVoicePackName] = useState('');
  const [timer, setTimer] = useState(0);
  const [audioBlob, setAudioBlob] = useState(null);
  const [isFFmpegLoaded, setIsFFmpegLoaded] = useState(false);
  const [isPlaying, setIsPlaying] = useState(false);
  const [duration, setDuration] = useState('00:00');
  const [currentTime, setCurrentTime] = useState(0);

  const ffmpegRef = useRef(null);
  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);
  const timerRef = useRef(null);

  const waveformRef = useRef(null);
  const wavesurferRef = useRef(null);
  const audioStreamRef = useRef(null);

  useEffect(() => {
    const loadFFmpeg = async () => {
      const ffmpeg = new FFmpeg();
      await ffmpeg.load();
      ffmpegRef.current = ffmpeg;
      setIsFFmpegLoaded(true);
    };
    loadFFmpeg();
  }, []);

  useEffect(() => {
    if (!waveformRef.current) return;

    wavesurferRef.current = WaveSurfer.create({
      container: waveformRef.current,
      waveColor: '#6366F1',
      progressColor: '#6366F1',
      cursorColor: '#6366F1',
      barWidth: 3,
      height: 60,
      responsive: true,
      plugins: [MicrophonePlugin.create()],
    });

    wavesurferRef.current.on('finish', () => {
      setIsPlaying(false);
      setCurrentTime(wavesurferRef.current.getDuration());
    });

    return () => {
      wavesurferRef.current?.destroy();
    };
  }, []);

  useEffect(() => {
    if (!wavesurferRef.current || !audioBlob) return;

    const ws = wavesurferRef.current;
    const updateTime = () => setCurrentTime(ws.getCurrentTime());

    ws.on('audioprocess', updateTime);
    return () => {
      ws.un('audioprocess', updateTime);
    };
  }, [audioBlob]);

  const handleStartRecording = async () => {
    if (!isFFmpegLoaded) return alert('FFmpeg 로딩 중입니다.');

    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    audioStreamRef.current = stream;
    setAudioBlob(null);
    setTimer(0);
    audioChunksRef.current = [];

    wavesurferRef.current.microphone.start();

    mediaRecorderRef.current = new MediaRecorder(stream, { mimeType: 'audio/webm' });
    mediaRecorderRef.current.ondataavailable = (e) => {
      audioChunksRef.current.push(e.data);
    };
    mediaRecorderRef.current.onstop = async () => {
      clearInterval(timerRef.current);
      audioStreamRef.current?.getTracks().forEach((track) => track.stop());
      wavesurferRef.current.microphone.stop();

      const webmBlob = new Blob(audioChunksRef.current, { type: 'audio/webm' });

      try {
        const ffmpeg = ffmpegRef.current;
        const arrayBuffer = await webmBlob.arrayBuffer();

        await ffmpeg.writeFile('input.webm', new Uint8Array(arrayBuffer));
        await ffmpeg.exec(['-i', 'input.webm', 'output.wav']);
        const outputData = await ffmpeg.readFile('output.wav');

        const wavBlob = new Blob([outputData.buffer], { type: 'audio/wav' });
        setAudioBlob(wavBlob);

        const audioUrl = URL.createObjectURL(wavBlob);
        wavesurferRef.current.load(audioUrl);

        const tempAudio = new Audio(audioUrl);
        tempAudio.onloadedmetadata = () => {
          const dur = tempAudio.duration;
          setDuration(!isNaN(dur) ? formatTime(dur) : '00:00');
          setCurrentTime(0);
        };
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

  const togglePlay = () => {
    if (!wavesurferRef.current) return;
    wavesurferRef.current.playPause();
    setIsPlaying((prev) => !prev);
  };

  const handleCreateVoicePack = () => {
    if (!voicePackName.trim() || !audioBlob) {
      alert('이름과 녹음이 필요합니다.');
      return;
    }

    console.log('✅ 보이스팩 이름:', voicePackName);
    console.log('✅ WAV Blob:', audioBlob);
    alert('보이스팩이 준비되었습니다. (콘솔 확인)');
  };

  const formatTime = (time) => {
    if (typeof time !== 'number' || isNaN(time)) return '00:00';
    const mins = String(Math.floor(time / 60)).padStart(2, '0');
    const secs = String(Math.floor(time % 60)).padStart(2, '0');
    return `${mins}:${secs}`;
  };

  return (
    <>
      <h1 className="text-xl font-bold text-gray-900 mb-6">보이스팩 생성</h1>

      <h1 className="text-l font-bold text-gray-900 mb-2">보이스팩 이름 *</h1>
      <input
        value={voicePackName}
        onChange={(e) => setVoicePackName(e.target.value)}
        className="w-full px-4 py-2 border rounded-md mb-6 bg-slate-50"
        placeholder="보이스팩 이름 입력"
      />

      <h2 className="text-l font-bold text-gray-900 mb-2">보이스팩 샘플 녹음 *</h2>
      <p className="text-sm text-gray-500 mb-4">녹음 버튼을 누르고 각 문장을 따라 읽어주세요.</p>

      <div className="bg-slate-50 rounded-md p-6">
        <p className="text-lg font-medium text-gray-800 mb-4">
          “안녕하세요. 목소리를 제공합니다. 잘 들리시나요? 감사합니다.”
        </p>

        <div className="flex items-center space-x-4">
          <button
            onClick={isRecording ? handleStopRecording : handleStartRecording}
            className={`w-12 h-12 rounded-full flex items-center justify-center text-white text-lg transition-colors duration-300 ${
              isRecording ? 'bg-indigo-500 ' : 'bg-gray-300 hover:bg-indigo-300'
            }`}
            disabled={!isFFmpegLoaded}
          >
            <Mic />
          </button>

          <button
            onClick={togglePlay}
            className="w-12 h-12 rounded-full bg-indigo-500 text-white text-xl flex items-center justify-center shadow-md hover:bg-indigo-300 transition disabled:bg-gray-300"
            disabled={!audioBlob}
          >
            {isPlaying ? <Pause /> : <Play />}
          </button>

          <div ref={waveformRef} className="flex-1 h-[60px]" />

          <span className="text-sm w-24 text-right text-indigo-500">
            {audioBlob ? `${formatTime(currentTime)} / ${duration}` : formatTime(timer)}
          </span>
        </div>
      </div>

      <button
        onClick={handleCreateVoicePack}
        className="mt-6 w-1/6 py-3 text-white rounded-md text-sm font-semibold disabled:opacity-50 bg-gradient-to-r from-violet-400 to-indigo-500"
      >
        생성하기
      </button>
    </>
  );
}

export default VoiceCreate;
