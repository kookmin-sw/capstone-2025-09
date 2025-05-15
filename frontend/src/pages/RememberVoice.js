import React, { useState } from 'react';
import GradientButton from '../components/common/GradientButton';
import { ScaleLoader } from 'react-spinners';
import AudioPlayer from '../components/common/AudioPlayer';
import { motion, AnimatePresence } from 'framer-motion';
import { Maximize2 } from 'lucide-react';
import { extractAudioFromVideo } from '../utils/extractAudioFromVideo';
import useVedioToVoicepack from '../hooks/useVideoToVoicepack';
import SelectBox from '../components/common/SelectBox';

const steps = ['영상 업로드', '텍스트 입력', '결과 확인'];

const RememberVoice = () => {
  const [step, setStep] = useState(1);
  const [direction, setDirection] = useState(1);
  const [videoFile, setVideoFile] = useState(null);
  const [audioFile, setAudioFile] = useState(null);
  const [videoUrl, setVideoUrl] = useState(null);
  const [videoRatio, setVideoRatio] = useState('landscape');
  const [scriptText, setScriptText] = useState('');
  const [emotionType, setEmotionType] = useState(0);
  const [audioUrl, setAudioUrl] = useState(null);
  const [isGenerating, setIsGenerating] = useState(false);
  const { startConversion } = useVedioToVoicepack();

  const handleVideoUpload = async (e) => {
    const file = e.target.files?.[0];
    if (file) {
      setVideoFile(file);
      const tempUrl = URL.createObjectURL(file);
      setVideoUrl(tempUrl);

      const video = document.createElement('video');
      video.preload = 'metadata';
      video.src = tempUrl;

      video.onloadedmetadata = async () => {
        if (video.duration > 15) {
          alert('15초 이하의 영상만 업로드할 수 있습니다.');
          setVideoFile(null);
          setVideoUrl(null);
          return;
        }

        setVideoRatio(
          video.videoHeight > video.videoWidth ? 'portrait' : 'landscape'
        );

        try {
          const extractedAudioFile = await extractAudioFromVideo(file);
          setAudioFile(extractedAudioFile);
          const audioTempUrl = URL.createObjectURL(extractedAudioFile);
          setAudioUrl(audioTempUrl);
        } catch (err) {
          console.error(err);
          alert('오디오 추출에 실패했습니다.');
          setVideoFile(null);
          setVideoUrl(null);
        }
      };
    }
  };

  const emotionOptions = [
    { label: '기본', value: 0 },
    { label: '기쁨', value: 1 },
    { label: '슬픔', value: 2 },
    { label: '놀람', value: 3 },
    { label: '화남', value: 4 },
  ];

  const handleRestore = async () => {
    if (!audioFile || !scriptText) return;
    setIsGenerating(true);
    try {
      const resultUrl = await startConversion({
        audioFile,
        prompt: scriptText,
        emotionIndex: emotionType,
      });
      setAudioUrl(resultUrl);
      setStep(3);
    } catch (err) {
      alert(err.message);
    }
    setIsGenerating(false);
  };

  const handleFullscreen = () => {
    const video = document.getElementById('preview-video');
    if (video?.requestFullscreen) video.requestFullscreen();
  };

  const variants = {
    enter: (direction) => ({ x: direction > 0 ? 100 : -100, opacity: 0 }),
    center: { x: 0, opacity: 1 },
    exit: (direction) => ({ x: direction > 0 ? -100 : 100, opacity: 0 }),
  };

  const progressPercent = (step / steps.length) * 100;

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold">리멤버 보이스</h1>
      <div className="space-y-8 max-w-2xl mx-auto px-4">
        {isGenerating && (
          <div className="absolute inset-0 bg-violet-50 bg-opacity-40 backdrop-blur-sm flex flex-col items-center justify-center z-50 rounded-xl">
            <ScaleLoader
              color="#615FFF"
              height={40}
              width={4}
              radius={2}
              margin={3}
            />
            <p className="mt-4 text-indigo-500 font-semibold text-lg animate-pulse">
              보이스 복원 중...
            </p>
            <p className="mt-2 text-indigo-500 font-semibold text-lg animate-pulse">
              페이지를 벗어나면 복원이 취소될 수 있어요!
            </p>
          </div>
        )}

        <div className="relative w-full h-2 rounded-full bg-gray-200 overflow-hidden">
          <motion.div
            className="absolute left-0 top-0 h-full bg-indigo-500 rounded-full"
            initial={{ width: 0 }}
            animate={{ width: `${progressPercent}%` }}
            transition={{ duration: 0.5, ease: 'easeInOut' }}
          />
        </div>

        <AnimatePresence custom={direction} mode="wait">
          {step === 1 && (
            <section className="space-y-6">
              <div className="space-y-1">
                <label className="block font-semibold text-gray-700 text-base">
                  영상 업로드
                </label>
                <p className="text-sm text-gray-500">
                  소중한 사람의 음성이 담긴 영상을 업로드해 주세요.
                </p>
              </div>

              {!videoUrl && (
                <label className="block w-full cursor-pointer border border-dashed border-indigo-300 rounded-lg bg-indigo-50 hover:bg-indigo-100 transition p-6 text-center text-indigo-600 font-medium shadow-sm">
                  <span>+ 영상 파일 업로드하기</span>
                  <input
                    type="file"
                    accept="video/*"
                    onChange={handleVideoUpload}
                    className="hidden"
                  />
                </label>
              )}

              {videoUrl && (
                <>
                  <div className="w-40 mx-auto relative group">
                    <div
                      className={`rounded-lg shadow-md overflow-hidden ${videoRatio === 'portrait' ? 'aspect-[9/16]' : 'aspect-[16/9]'}`}
                    >
                      <video
                        id="preview-video"
                        src={videoUrl}
                        controls
                        className="w-full h-full object-cover"
                      />
                    </div>
                    <button
                      onClick={handleFullscreen}
                      className="absolute top-1 right-1 p-1 bg-white bg-opacity-80 rounded-full hover:bg-opacity-100"
                    >
                      <Maximize2 size={16} className="text-gray-600" />
                    </button>
                  </div>

                  <div className="flex justify-center mt-2">
                    <button
                      onClick={() => {
                        setVideoFile(null);
                        setVideoUrl(null);
                      }}
                      className="text-sm text-indigo-600 hover:underline hover:text-indigo-700 transition"
                    >
                      영상 다시 선택하기
                    </button>
                  </div>
                </>
              )}

              {videoFile && (
                <div className="w-full flex justify-end mt-2">
                  <GradientButton
                    className="px-3 py-1"
                    onClick={() => {
                      setDirection(1);
                      setStep(2);
                    }}
                  >
                    다음
                  </GradientButton>
                </div>
              )}
            </section>
          )}

          {step === 2 && (
            <motion.section
              key="step2"
              custom={direction}
              variants={variants}
              initial="enter"
              animate="center"
              exit="exit"
              transition={{ duration: 0.4 }}
              className="space-y-6"
            >
              <div className="space-y-1">
                <label className="block font-semibold text-gray-700 text-base">
                  감정 및 텍스트 설정
                </label>
                <p className="text-sm text-gray-500">
                  그 사람의 목소리로 듣고 싶은 말을 입력해 주세요.
                </p>
              </div>
              <SelectBox
                label="감정"
                value={emotionType}
                onChange={(value) => setEmotionType(Number(value))}
                options={emotionOptions}
                placeholder="감정을 선택해주세요."
              />
              <textarea
                value={scriptText}
                onChange={(e) => setScriptText(e.target.value)}
                placeholder="예: 사랑하는 딸아, 항상 응원하고 있어."
                className="w-full h-40 p-4 bg-white text-gray-600 placeholder-gray-400 rounded-md shadow-sm resize-none focus:outline-none focus:ring-2 focus:ring-indigo-400"
              />

              <div className="flex justify-between">
                <GradientButton
                  className="px-3 py-1"
                  onClick={() => {
                    setDirection(-1);
                    setStep(1);
                  }}
                >
                  이전
                </GradientButton>
                <GradientButton
                  className="px-3 py-1"
                  onClick={handleRestore}
                  disabled={!scriptText}
                >
                  복원하기
                </GradientButton>
              </div>
            </motion.section>
          )}

          {step === 3 && audioUrl && (
            <motion.section
              key="step3"
              custom={direction}
              variants={variants}
              initial="enter"
              animate="center"
              exit="exit"
              transition={{ duration: 0.4 }}
              className="space-y-6"
            >
              <h2 className="text-lg font-semibold text-gray-700">
                복원된 목소리
              </h2>
              <p className="text-sm text-gray-500">
                AI가 복원한 목소리를 들어보세요.
              </p>
              <div className="mt-12 px-2 py-2 bg-white backdrop-blur-sm rounded-xl">
                {/* 한 줄로 재생바 + 다운로드 버튼 정렬 */}
                <div className="flex items-center space-x-4 px-6 py-5">
                  <div className="flex-1">
                    <AudioPlayer audioUrl={audioUrl} />
                  </div>
                  <GradientButton
                    className="px-6 py-3"
                    onClick={() => {
                      const fileName =
                        prompt('저장할 파일 이름을 입력해주세요.');
                      if (!fileName) return;

                      const link = document.createElement('a');
                      link.href = audioUrl;
                      link.download = `${fileName}.mp3`;
                      link.click();
                    }}
                  >
                    다운로드
                  </GradientButton>
                </div>
              </div>
              <div className="flex justify-center space-x-4 mt-6">
                <GradientButton
                  className="px-4 py-2"
                  onClick={() => {
                    // 모든 상태 초기화
                    setStep(1);
                    setDirection(-1);
                    setVideoFile(null);
                    setVideoUrl(null);
                    setScriptText('');
                    setAudioUrl(null);
                  }}
                >
                  처음부터 다시하기
                </GradientButton>
                <GradientButton
                  className="px-4 py-2"
                  onClick={() => {
                    setStep(2);
                    setDirection(-1);
                  }}
                >
                  텍스트 수정하기
                </GradientButton>
              </div>
            </motion.section>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
};

export default RememberVoice;
