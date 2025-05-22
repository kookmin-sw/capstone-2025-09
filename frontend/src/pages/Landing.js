import React, { Suspense, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import BlurBackgrounds from '../components/visual/BlurBackground';
import WaveAninmation from '../components/visual/WaveAninmation';
import { Canvas } from '@react-three/fiber';
import { OrbitControls } from '@react-three/drei';
import WaveSphere from '../components/visual/WaveSphere';
import GradientButton from '../components/common/GradientButton';
import LandingpageVoicepack from '../components/common/LandingpageVoicepack';
import useUserStore from '../utils/userStore';
import { LogOut } from 'lucide-react';
import { motion } from 'framer-motion';

const Landing = () => {
  const navigate = useNavigate();
  const user = useUserStore((state) => state.user);
  const clearUser = useUserStore((state) => state.clearUser);

  const handleLogout = () => {
    clearUser();
    sessionStorage.removeItem('userInfo');
    navigate('/');
  };

  const benefits = [
    {
      title: '목소리를 자산으로',
      description:
        '나의 목소리를 AI로 학습해\n보이스팩으로 만들고 마켓에서 거래하며\n수익을 창출할 수 있어요.\n이제 목소리도 디지털 자산이 됩니다.',
      badge: 'VALUE',
      color: 'bg-violet-100',
    },
    {
      title: '텍스트를 콘텐츠로',
      description:
        '원하는 문장을 입력하면\n보이스팩으로 음성을 생성해\n즉시 오디오 콘텐츠로 활용할 수 있어요.\n광고, 영상, 나레이션까지 바로 가능해요.',
      badge: 'CREATION',
      color: 'bg-indigo-200',
    },
    {
      title: '크리에이터와 함께 성장',
      description:
        'COVOS는 함께하는 창작을 지향합니다.\n\n다양한 보이스 크리에이터들과 연결되고,\n서로의 아이디어에 영감을 받아보세요.',
      badge: 'COMMUNITY',
      color: 'bg-indigo-300',
    },
  ];

  const contents = [
    {
      badge: '베이직',
      title: '텍스트를 바로 AI 보이스로',
      description:
        '보이스팩을 선택하고 원하는 문장을 입력하면\n즉시 나만의 음성으로 변환해줍니다.\n\n광고 멘트, 스크립트 연습 등 다양한 활용 가능!',
      image: '/assets/landing-basicVoice.png',
    },
    {
      badge: 'AI 리포터',
      title: '맞춤 정보를 매일 음성으로',
      description:
        '날씨, 일정, 뉴스, 주식 등 설정한 정보를\n내가 원할 때마다 선택한 보이스팩으로 들려줍니다.\n\n나만의 맞춤형 리포터를 경험해 보세요.',
      image: '/assets/landing-reporter.png',
    },
    {
      badge: '오늘의 명언',
      title: '영감을 주는 하루 한 마디',
      description:
        '매일 아침, 오늘의 명언을 당신의 AI 보이스로 들어보세요.\n\n출근길, 공부 시간, 잠들기 전…\n당신의 하루를 따뜻한 목소리로 시작하고 마무리해보세요.',
      image: '/assets/landing-quote.png',
    },
    {
      badge: '리멤버 보이스',
      title: '기억 속 목소리를 다시 만나다',
      description:
        '소중한 사람의 목소리를 AI로 되살릴 수 있어요.\n\n다시는 들을 수 없을 줄 알았던 그 목소리,\n짧은 영상만으로도 그리운 사람의 추억을 생생하게 간직해보세요.',
      image: '/assets/landing-rememberVoice.png',
    },
  ];

  const Card = ({ item, isActive, innerRef }) => {
    return (
      <motion.div
        ref={innerRef}
        className="flex flex-col md:flex-row items-center rounded-[20px] px-6 py-10 md:p-10 shadow-xl"
        initial={{ opacity: 0.5, scale: 0.95, y: 20 }}
        animate={{
          opacity: isActive ? 1 : 0.5,
          scale: isActive ? 1.05 : 0.95,
          y: isActive ? 0 : 20,
        }}
        transition={{ duration: 0.6, ease: 'easeOut' }}
        style={{
          background: 'rgba(244, 245, 247, 0.2)',
          boxShadow: 'rgba(255, 255, 255, 0.2) 0px 0px 40px 0px inset',
          backdropFilter: 'blur(4px)',
          WebkitBackdropFilter: 'blur(4px)',
          zIndex: isActive ? 10 : 1,
        }}
      >
        <img
          src={item.image}
          alt={item.title}
          className="w-full md:w-1/2 rounded-xl object-cover mb-2 md:mb-0 md:mr-8"
        />
        <div className="text-white space-y-6 py-8">
          <span className="inline-block bg-violet-300 text-black text-sm font-bold px-3 py-1 rounded-full">
            {item.badge}
          </span>
          <h1 className="text-3xl font-bold whitespace-pre-line">
            {item.title}
          </h1>
          <p className="text-gray-300 whitespace-pre-line">
            {item.description}
          </p>
        </div>
      </motion.div>
    );
  };

  const containerRefs = useRef([]);
  const [activeIndex, setActiveIndex] = useState(-1);

  // 중앙 기준 activeIndex 잡기 로직 (핵심)
  useEffect(() => {
    const handleScroll = () => {
      const viewportCenter = window.innerHeight / 2;

      const distances = containerRefs.current.map((ref) => {
        if (!ref) return Number.MAX_VALUE;
        const rect = ref.getBoundingClientRect();
        const elementCenter = rect.top + rect.height / 2;
        return Math.abs(elementCenter - viewportCenter);
      });

      const closestIndex = distances.indexOf(Math.min(...distances));
      setActiveIndex(closestIndex);
    };

    window.addEventListener('scroll', handleScroll);
    handleScroll(); // 초기 실행

    return () => {
      window.removeEventListener('scroll', handleScroll);
    };
  }, []);

  const topVoicepacks = [
    '감성 보이스 연우',
    '카리스마 보이스 준혁',
    '애자일 마스터 종민',
    '시크한 인공지능 준교',
    '밝은 안내 음성 민재',
    '잔잔한 톤의 소민',
    '논리적인 설명 도윤',
    '감정 담은 책읽는 나연',
    '맑고 또렷한 수연',
    '센스있는 성우 찬우',
  ];

  const bottomVoicepacks = [
    '설렘 가득 ASMR 유리',
    '뉴스 전하는 강호',
    '에너지 넘치는 소민',
    '영어 리딩 보이스 에린',
    '연기력 좋은 래온',
    '깊은 울림의 진우',
    '달콤한 이야기 지유',
    '빠른 스포츠 톤 태민',
    '중성 AI 음성 성원',
    '부드러운 성우 성호',
  ];

  const allGradients = [
    'from-indigo-500 to-purple-500',
    'from-pink-400 to-red-500',
    'from-green-400 to-emerald-500',
    'from-yellow-400 to-orange-500',
    'from-blue-400 to-sky-500',
    'from-fuchsia-400 to-rose-500',
    'from-teal-400 to-cyan-500',
    'from-lime-400 to-green-500',
    'from-amber-400 to-yellow-500',
    'from-sky-500 to-indigo-400',
    'from-rose-500 to-pink-400',
    'from-purple-500 to-blue-500',
    'from-orange-400 to-amber-500',
    'from-emerald-400 to-teal-500',
    'from-cyan-400 to-sky-500',
    'from-lime-300 to-green-400',
    'from-violet-400 to-fuchsia-500',
    'from-yellow-300 to-orange-400',
    'from-blue-300 to-indigo-400',
    'from-zinc-400 to-slate-500',
  ];

  // Fisher-Yates 셔플 함수
  const shuffle = (arr) => {
    const array = [...arr];
    for (let i = array.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [array[i], array[j]] = [array[j], array[i]];
    }
    return array;
  };

  const totalCount = (topVoicepacks.length + bottomVoicepacks.length) * 2;

  // 그냥 공통으로 하나만 씀
  const gradientList = shuffle(allGradients).slice(0, totalCount);

  return (
    <div className="relative overflow-hidden">
      {/* Blur background를 절대 위치로 */}
      <div className="absolute inset-0 z-0 pointer-events-none">
        <BlurBackgrounds />
      </div>

      {/* Header */}
      <header className="flex justify-between items-end h-[96px] px-28 z-10 relative">
        <img src="/assets/logo-new.svg" alt="COVOS" width={150} />

        {user ? (
          <button
            onClick={handleLogout}
            className="flex items-center space-x-2 p-2 rounded text-gray-400 font-bold"
          >
            <LogOut size={20} />
            <span>로그아웃</span>
          </button>
        ) : (
          <GradientButton
            onClick={() => navigate('/sign-in')}
            className="py-2 px-8 text-base"
          >
            로그인하기
          </GradientButton>
        )}
      </header>

      {/* Section 1 - Hero*/}
      <section
        className="relative flex flex-col justify-center items-center overflow-hidden"
        style={{ minHeight: 'calc(100vh - 96px)' }}
      >
        {/* Animated gradient background layer */}
        <div className="absolute inset-0  animate-gradient-slow z-0" />

        <div className="flex justify-center items-center h-full w-full relative z-10">
          {/* 좌측 - 3D 구체 */}
          <motion.div
            className="w-[50%] h-[80vh] flex justify-end items-end pl-20"
            initial={{ opacity: 0, x: -50 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 1 }}
          >
            <Canvas shadows camera={{ position: [0, 0, 6], fov: 50 }}>
              <Suspense fallback={null}>
                <ambientLight intensity={0.3} />
                <directionalLight
                  position={[5, 5, 5]}
                  intensity={1.2}
                  shadow-mapSize-width={1024}
                  shadow-mapSize-height={1024}
                />
                <WaveSphere />
                <OrbitControls
                  enableZoom={false}
                  autoRotate
                  autoRotateSpeed={0.5}
                />
              </Suspense>
            </Canvas>
          </motion.div>

          {/* 우측 - 텍스트 + 버튼 */}
          <motion.div
            className="w-[50%] flex flex-col items-start justify-start text-left px-4 text-black"
            initial={{ opacity: 0, x: 50 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 1 }}
          >
            <motion.h1
              className="text-4xl font-bold mb-4"
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.3, duration: 0.6 }}
            >
              나만의 AI 보이스를
            </motion.h1>
            <motion.h1
              className="text-4xl font-bold mb-10"
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.5, duration: 0.6 }}
            >
              만들고 공유하고 활용하세요
            </motion.h1>

            <motion.div
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ delay: 0.8, duration: 0.5 }}
            >
              <GradientButton
                onClick={() => navigate(user ? '/voice-store' : '/sign-in')}
                className="text-lg py-3 px-8 hover:scale-105 transition-transform"
              >
                COVOS 시작하기
              </GradientButton>
            </motion.div>
          </motion.div>
        </div>
      </section>

      {/* Section 2 - 무한 슬라이딩 보이스팩 */}
      <section className="flex flex-col items-center text-black mt-10 overflow-hidden relative py-20">
        {/* 상단 슬라이드 */}
        <div className="w-screen overflow-hidden py-20">
          <h2 className="text-3xl mb-2 font-semibold text-center">
            마켓 플레이스
          </h2>
          <p className="mb-20 text-center">
            나만의 보이스팩을 업로드하고 수익을 창출하세요
          </p>

          {['top', 'bottom'].map((position) => {
            const voicepackList =
              position === 'top' ? topVoicepacks : bottomVoicepacks;

            return (
              <div key={position} className="w-full overflow-hidden mb-6">
                <div
                  className="flex animate-marquee gap-10 min-w-fit"
                  style={{
                    animationDirection:
                      position === 'bottom' ? 'reverse' : 'normal',
                  }}
                >
                  {[...Array(2)].flatMap((_, groupIdx) =>
                    voicepackList.map((name, i) => (
                      <LandingpageVoicepack
                        key={`${position}-${groupIdx}-${i}`}
                        name={name}
                        gradient={
                          gradientList[groupIdx * voicepackList.length + i]
                        }
                      />
                    ))
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </section>

      {/* Section 3 */}

      <section className="bg-black py-32">
        <div className="max-w-5xl mx-auto px-4">
          <h2 className="text-white text-3xl font-bold text-center mb-24">
            COVOS만의 기능
          </h2>
          <div className="space-y-10 z-10">
            {contents.map((item, index) => (
              <Card
                key={index}
                item={item}
                isActive={activeIndex === index}
                innerRef={(el) => (containerRefs.current[index] = el)}
              />
            ))}
          </div>
        </div>
      </section>

      {/* Section 4 */}
      <section className="h-screen flex flex-col justify-center items-center text-black mt-10 py-20">
        <div className="max-w-7xl mx-auto px-4 text-center">
          <h2 className="text-3xl font-bold mb-24">
            COVOS, 당신의 목소리에 새로운 가치를 더하다
          </h2>
          <div className="grid md:grid-cols-3 z-10 gap-12">
            {benefits.map((benefit, index) => (
              <motion.div
                key={index}
                initial={{ opacity: 0, y: 50 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.6, delay: index * 0.2 }}
                className={`p-10 rounded-3xl shadow-xl hover:shadow-2xl transition-all hover:scale-[1.02] ${benefit.color}`}
              >
                <span className="inline-block mb-4 px-3 py-[2px] text-xs font-bold text-gray-800 bg-white/60 rounded-full shadow">
                  {benefit.badge}
                </span>
                <h3 className="text-2xl font-semibold text-gray-900 mb-4">
                  {benefit.title}
                </h3>
                <p className="text-gray-700 text-base whitespace-pre-line">
                  {benefit.description}
                </p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Section 5 */}
      <section className="pt-40 pb-40 bg-black text-white text-center">
        <h2 className="text-4xl mb-8 font-semibold">
          지금,
          <br />
          당신의 AI 보이스를 만들어보세요.
        </h2>
        <button
          onClick={() => navigate(user ? '/voice-store' : '/sign-in')}
          className="bg-white text-indigo-500 px-12 py-2 rounded font-semibold  relative z-10"
        >
          시작하기
        </button>
        <div className="absolute bottom-0 w-full z-0">
          <WaveAninmation />
        </div>
      </section>
    </div>
  );
};

export default Landing;
