import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Mic, Play, Pause } from 'lucide-react';
import { FFmpeg } from '@ffmpeg/ffmpeg';
import WaveSurfer from 'wavesurfer.js';
import MicrophonePlugin from 'wavesurfer.js/dist/plugin/wavesurfer.microphone';
import useVoiceConvert from '../hooks/useVoicepackConvert';
import useVoicepackDetail from '../hooks/useVoicepackDetail';
import useVoicepackDelete from '../hooks/useVoicepackDelete';
import { ScaleLoader } from 'react-spinners';
import axiosInstance from '../utils/axiosInstance';
import GradientButton from '../components/common/GradientButton';
import SelectBox from '../components/common/SelectBox';
import useUserStore from '../utils/userStore';

const VoiceCreate = () => {
  const [isRecording, setIsRecording] = useState(false);
  const [voicePackName, setVoicePackName] = useState('');
  const [timer, setTimer] = useState(0);
  const [audioBlob, setAudioBlob] = useState(null);
  const [isFFmpegLoaded, setIsFFmpegLoaded] = useState(false);
  const [isPlaying, setIsPlaying] = useState(false);
  const [duration, setDuration] = useState('00:00');
  const [currentTime, setCurrentTime] = useState(0);
  const { convertVoice, loading } = useVoiceConvert();
  const { getVoicepackAudio, makePublic } = useVoicepackDetail();
  const { deleteVoicepack } = useVoicepackDelete();
  const [isPolling, setIsPolling] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [convertedAudioUrl, setConvertedAudioUrl] = useState('');
  const [voicepackId, setVoicepackId] = useState(null);
  const [imageFile, setImageFile] = useState(null);
  const [genderCategory, setGenderCategory] = useState('');
  const [customCategory, setCustomCategory] = useState('');
  const navigate = useNavigate();
  const user = useUserStore((state) => state.user);

  const ffmpegRef = useRef(null);
  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);
  const timerRef = useRef(null);

  const waveformRef = useRef(null);
  const wavesurferRef = useRef(null);
  const audioStreamRef = useRef(null);

  const categoryOptions = [
    { label: '차분한', value: '차분한' },
    { label: '귀여운', value: '귀여운' },
    { label: '카리스마', value: '카리스마' },
    { label: '가녀린', value: '가녀린' },
    { label: '중성적인', value: '중성적인' },
    { label: '도도한', value: '도도한' },
    { label: '따뜻한', value: '따뜻한' },
    { label: '지적인', value: '지적인' },
    { label: '상냥한', value: '상냥한' },
    { label: '명랑한', value: '명랑한' },
    { label: '낙천적인', value: '낙천적인' },
    { label: '단호한', value: '단호한' },
    { label: '시크한', value: '시크한' },
    { label: '감미로운', value: '감미로운' },
    { label: '활기찬', value: '활기찬' },
    { label: '열정적인', value: '열정적인' },
    { label: '묵직한', value: '묵직한' },
    { label: '신뢰가는', value: '신뢰가는' },
    { label: '유쾌한', value: '유쾌한' },
    { label: '세련된', value: '세련된' },
    { label: '포근한', value: '포근한' },
  ];

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
    setIsPlaying(false);
    audioChunksRef.current = [];

    wavesurferRef.current.microphone.start();

    mediaRecorderRef.current = new MediaRecorder(stream, {
      mimeType: 'audio/webm',
    });
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
    setIsPlaying(false);
  };

  const handleImageChange = (e) => {
    const file = e.target.files?.[0];
    if (file) setImageFile(file); // ✅ 추가된 부분
  };

  const togglePlay = () => {
    if (!wavesurferRef.current) return;
    wavesurferRef.current.playPause();
    setIsPlaying((prev) => !prev);
  };

  const pollStatus = async (id, interval = 2000, maxAttempts = 200) => {
    let attempts = 0;

    return new Promise((resolve, reject) => {
      const checkStatus = async () => {
        try {
          const { data } = await axiosInstance.get(
            `/voicepack/convert/status/${id}`
          );
          if (data.status === 'COMPLETED') {
            resolve(data);
          } else if (attempts >= maxAttempts) {
            reject(new Error('폴링 최대 횟수 초과'));
          } else {
            attempts++;
            setTimeout(checkStatus, interval);
          }
        } catch (err) {
          reject(err);
        }
      };

      checkStatus();
    });
  };

  const handleCreateVoicePack = async () => {
    if (!voicePackName.trim() || !audioBlob) {
      alert('이름과 녹음이 필요합니다.');
      return;
    }

    if (!genderCategory) {
      alert('성별을 선택해주세요..');
      return;
    }

    const regex = /[^a-zA-Z0-9가-힣\s]/;
    if (regex.test(voicePackName)) {
      alert('보이스팩 이름에 특수기호는 사용할 수 없습니다.');
      return; // 특수기호가 있으면 생성하지 않음
    }
    try {
      setIsPolling(true);
      const formData = new FormData();
      formData.append('userId', user.id);
      formData.append('name', voicePackName);
      formData.append('voiceFile', audioBlob);
      if (imageFile) {
        formData.append('imageFile', imageFile);
      }
      formData.append(
        'categories',
        [genderCategory, customCategory].filter(Boolean).join(',')
      );
      const res = await convertVoice(formData);

      if (res?.id) {
        const result = await pollStatus(res.id);
        setVoicepackId(result.voicepackId);
        const exampleUrl = await getVoicepackAudio(result.voicepackId);
        setConvertedAudioUrl(exampleUrl);
        setIsModalOpen(true);
      }
    } catch (error) {
      console.error('보이스팩 생성 오류:', error);
      alert('보이스팩 생성 실패');
    } finally {
      setIsPolling(false);
    }
  };

  const handleDelete = async () => {
    if (!voicepackId) {
      alert('보이스팩 ID가 존재하지 않습니다.');
      return;
    }

    try {
      const confirmed = window.confirm(
        '정말로 이 보이스팩을 삭제하시겠습니까?'
      );
      if (!confirmed) return;

      const success = await deleteVoicepack(voicepackId);
      if (success) {
        alert('보이스팩이 삭제되었습니다.');
        window.location.reload();
      }
    } catch (err) {
      alert('삭제 중 오류가 발생했습니다.');
    }
  };

  const formatTime = (time) => {
    if (typeof time !== 'number' || isNaN(time)) return '00:00';
    const mins = String(Math.floor(time / 60)).padStart(2, '0');
    const secs = String(Math.floor(time % 60)).padStart(2, '0');
    return `${mins}:${secs}`;
  };

  return (
    <>
      {(loading || isPolling) && (
        <div className="absolute inset-0 bg-violet-50 bg-opacity-40 backdrop-blur-sm flex flex-col items-center justify-center z-50 rounded-xl">
          <ScaleLoader
            color="#615FFF"
            height={40}
            width={4}
            radius={2}
            margin={3}
          />
          <p className="mt-4 text-indigo-500 font-semibold text-lg animate-pulse">
            보이스팩 생성 중...
          </p>
          <p className="mt-4 text-indigo-500 font-semibold text-lg animate-pulse">
            페이지를 벗어나면 보이스팩 생성이 취소될 수 있어요!
          </p>
        </div>
      )}

      {isModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-30 backdrop-blur-sm flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 shadow-lg w-[400px] max-w-full">
            <h2 className="text-lg font-bold mb-4 text-gray-900">
              🔊 보이스팩 생성 완료
            </h2>
            <p className="mb-2 text-gray-700">
              생성된 보이스팩을 들어볼 수 있어요.
            </p>
            {convertedAudioUrl ? (
              <audio
                controls
                src={convertedAudioUrl}
                className="w-full mt-4 mb-4"
              />
            ) : (
              <p className="text-sm text-gray-500">오디오 로딩 중...</p>
            )}
            <div className="flex justify-center space-x-2">
              <button
                onClick={handleDelete}
                className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition"
              >
                보이스 삭제
              </button>
              <button
                onClick={async () => {
                  try {
                    await makePublic(voicepackId);
                    alert('보이스팩이 생성되었습니다!');
                    navigate('/voice-store');
                  } catch (err) {
                    console.error('공개 실패:', err);
                    alert('보이스 공개에 실패했습니다.');
                  }
                }}
                className="px-4 py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 transition"
              >
                보이스 생성
              </button>
            </div>
          </div>
        </div>
      )}

      <h1 className="text-xl font-bold mb-6">보이스팩 생성</h1>

      {/* 보이스팩 이미지 업로드 */}
      <div className="mb-8">
        <div className="flex items-center text-gray-600 mb-4">
          <label className="block text-l font-bold text-gray-900">
            보이스팩 커버 이미지
          </label>
          <div className="relative group ml-2">
            <div className="w-4 h-4 flex items-center justify-center rounded-full bg-indigo-400 text-white text-xs cursor-default">
              !
            </div>
            <div className="absolute z-10 w-80 p-3 bg-slate-50 backdrop-blur-sm text-sm text-gray-700 border border-indigo-200 rounded-md opacity-0 group-hover:opacity-100 transition-opacity duration-300 top-1/2 left-full -translate-y-1/2 ml-2 pointer-events-none">
              이미지를 선택하지 않으면 기본 커버 이미지가 사용됩니다.
              <br />
              (1MB 미만의 이미지만 업로드 가능합니다.)
            </div>
          </div>
        </div>

        <div className="w-48 h-48 rounded-lg border-2 border-dashed border-gray-300 bg-gray-50 hover:border-indigo-400 transition p-2 relative flex flex-col items-center justify-center">
          {imageFile ? (
            <img
              src={URL.createObjectURL(imageFile)}
              alt="보이스팩 미리보기"
              className="w-full h-full object-cover rounded-md"
            />
          ) : (
            <>
              <span className="text-gray-400 text-sm mb-1">미리보기 없음</span>
              <span className="text-gray-300 text-xs">
                이미지를 선택해 주세요
              </span>
            </>
          )}

          <label
            htmlFor="imageUpload"
            className="absolute bottom-2 right-2 px-3 py-1 bg-indigo-500 text-white text-xs rounded-md cursor-pointer hover:bg-indigo-600 transition"
          >
            파일 선택
          </label>
          <input
            id="imageUpload"
            type="file"
            accept="image/*"
            onChange={handleImageChange}
            className="hidden"
          />
        </div>
      </div>

      <label className="text-l font-bold text-gray-900 mb-2 block">
        보이스팩 이름 <span className="text-red-500">*</span>
        <span className="text-gray-600 text-sm font-normal"></span>
      </label>
      <input
        value={voicePackName}
        onChange={(e) => setVoicePackName(e.target.value)}
        className="w-96 px-4 py-2 border-none rounded-md mb-6 bg-white focus:outline-none focus:ring-1 focus:ring-indigo-400"
        placeholder="보이스팩 이름을 입력해주세요."
      />

      <div className="flex flex-row space-x-8">
        {/* 성별 카테고리 선택 */}
        <div>
          <label className="text-l font-bold text-gray-900 mb-2 block">
            보이스팩 성별 <span className="text-red-500">*</span>
          </label>
          <div className="flex space-x-2 mb-6">
            {['남자', '여자', '기타'].map((gender) => (
              <button
                key={gender}
                type="button"
                onClick={() => setGenderCategory(gender)}
                className={`px-4 py-2 rounded-lg text-sm border transition ${
                  genderCategory === gender
                    ? 'bg-indigo-500 text-white border-indigo-500'
                    : 'bg-white text-gray-700 border-gray-300 hover:bg-indigo-100'
                }`}
              >
                {gender}
              </button>
            ))}
          </div>
        </div>

        {/* 사용자 지정 카테고리 입력 */}
        <div>
          <label className="text-l font-bold text-gray-900 mb-2 block">
            카테고리 태그{' '}
            <span className="text-gray-600 text-sm font-normal">
              (1개 선택)
            </span>
          </label>
          <div className="w-64 mb-6">
            <SelectBox
              value={customCategory}
              onChange={(value) => setCustomCategory(value)}
              options={categoryOptions}
              placeholder="카테고리를 선택해주세요."
            />
          </div>
        </div>
      </div>

      <div className="flex flex-col mb-2">
        <h2 className="text-l font-bold text-gray-900">
          보이스팩 샘플 녹음 <span className="text-red-500">*</span>
        </h2>
        <div className="flex items-center text-sm text-gray-600 mb-4">
          <p>
            녹음 가이드를 참고하여, 녹음 버튼을 누르고 아래 문장을 따라
            읽어주세요.
          </p>
          <div className="relative group ml-2">
            <div className="w-4 h-4 flex items-center justify-center rounded-full bg-indigo-400 text-white text-xs cursor-default">
              !
            </div>
            <div className="absolute z-10 w-80 p-3 bg-slate-50 backdrop-blur-sm text-sm text-gray-700 border border-indigo-200 rounded-md opacity-0 group-hover:opacity-100 transition-opacity duration-300 top-1/2 left-full -translate-y-1/2 ml-2 pointer-events-none">
              🎙️ <b>조용한 환경</b>에서 녹음해 주세요.
              <br />
              <br />
              💡 <b>이어폰이나 외부 마이크</b> 사용을 권장합니다.
              <br />
              <br />
              🔇 <b>TV, 음악, 대화 등</b> 소음을 줄여 주세요.
            </div>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-md p-6">
        <p className="text-lg font-medium text-gray-800 mb-4">
          “안녕하세요. 지금 제 목소리를 녹음하고 있어요. 또렷하게 들리시나요?
          감사합니다.”
        </p>

        <div className="flex items-center space-x-4">
          <button
            onClick={isRecording ? handleStopRecording : handleStartRecording}
            className={`w-12 h-12 rounded-full flex items-center justify-center text-white text-lg transition-colors duration-300 ${
              isRecording
                ? 'bg-indigo-500 hover:bg-indigo-300'
                : 'bg-gray-300 hover:bg-indigo-300'
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
            {audioBlob
              ? `${formatTime(currentTime)} / ${duration}`
              : formatTime(timer)}
          </span>
        </div>
      </div>

      <div className="mt-6 flex justify-end">
        <GradientButton
          className="px-6 py-3 disabled:opacity-50 disabled:cursor-not-allowed"
          onClick={handleCreateVoicePack}
          disabled={loading || !voicePackName.trim() || !audioBlob}
        >
          생성하기
        </GradientButton>
      </div>
    </>
  );
};

export default VoiceCreate;
