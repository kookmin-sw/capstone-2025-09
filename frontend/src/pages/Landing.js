import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import logo from '../assets/logo-new.svg';
import BlurBackgrounds from '../components/visual/BlurBackground';
import WaveAninmation from '../components/visual/WaveAninmation';
import { Canvas } from '@react-three/fiber';
import { OrbitControls } from '@react-three/drei';
import WaveSphere from '../components/visual/WaveSphere';
import GradientButton from '../components/common/GradientButton';
import useUserStore from '../utils/userStore';
import { LogOut } from 'lucide-react';
import { motion } from 'framer-motion';
import { useInView } from 'react-intersection-observer';

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
      title: '함께 성장해요',
      description:
        '서로의 배움과 도전을 응원하며, 더 나은 동료가 되어가요. 넥스터즈는 혼자 성장하는 곳이 아닌 함께 성장하는 곳이에요.',
    },
    {
      title: '실제로 만들어봐요',
      description:
        '아이디어를 현실로! 프로젝트를 통해 진짜 서비스를 만들어보고 서비스가 세상과 만나도록 도와줘요.',
    },
    {
      title: '재밌게 즐겨요',
      description:
        '다양한 사람들과 협업하며, 서로 다른 관점과 취향을 존중하고, 함께 즐기면서 일해요.',
    },
  ];
  const contents = [
    {
      badge: '베이직',
      title: '텍스트를 바로 AI 보이스로',
      description:
        '보이스팩을 선택하고 원하는 문장을 입력하면\n즉시 나만의 음성으로 변환해줍니다.\n\n광고 멘트, 스크립트 연습 등 다양한 활용 가능!',
      image: '/images/feature_basic.jpg',
    },
    {
      badge: 'AI 리포터',
      title: '맞춤 정보를 매일 음성으로',
      description:
        '날씨, 일정, 뉴스, 주식 등 설정한 정보를\n내가 원할 때마다 선택한 보이스팩으로 들려줍니다.\n\n나만의 맞춤형 리포터를 경험해 보세요.',
      image: '/images/feature_reporter.jpg',
    },
    {
      badge: '오늘의 명언',
      title: '영감을 주는 하루 한 마디',
      description:
        '매일 아침, 오늘의 명언을 당신의 AI 보이스로 들어보세요.\n\n출근길, 공부 시간, 잠들기 전…\n당신의 하루를 따뜻한 목소리로 시작하고 마무리해보세요.',
      image: '/images/feature_quote.jpg',
    },
    {
      badge: '리멤버 보이스',
      title: '기억 속 목소리를 다시 만나다',
      description:
        '소중한 사람의 목소리를 AI로 되살릴 수 있어요.\n짧은 음성만 있으면 고인의 목소리나 추억의 목소리를\n보이스팩으로 만들어보세요.',
      image: '/images/feature_remember.jpg',
    },
  ];

  const Card = ({ item, isActive, innerRef }) => {
    return (
      <div
        ref={innerRef}
        className={`flex flex-col md:flex-row items-center bg-gradient-to-br from-gray-800 via-gray-700 to-gray-800 rounded-3xl px-6 py-10 md:p-10 shadow-xl transition-all duration-500 ${
          isActive ? 'scale-110' : 'scale-100 opacity-70'
        }`}
      >
        <img
          src={item.image}
          alt={item.title}
          className="w-full md:w-1/2 rounded-xl object-cover mb-2 md:mb-0 md:mr-8"
        />
        <div className="text-white space-y-6">
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
      </div>
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

  return (
    <div className="relative  overflow-hidden">
      {/* Blur background를 절대 위치로 */}
      <div className="absolute inset-0 z-0 pointer-events-none">
        <BlurBackgrounds />
      </div>

      {/* Header */}
      <header className="flex justify-between items-end h-[96px] px-28 z-10 relative">
        <img src={logo} alt="COVOS" width={150} />

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
        className="relative flex flex-col justify-center items-center"
        style={{ minHeight: 'calc(100vh - 96px)' }}
      >
        <div className="flex justify-center items-center h-full w-full">
          <div className="w-[50%] h-[80vh] flex justify-end items-end pl-20">
            <Canvas shadows camera={{ position: [0, 0, 6], fov: 50 }}>
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
            </Canvas>
          </div>

          <div className="w-[50%] flex flex-col items-start justify-start text-left px-4 text-black">
            <h1 className="text-4xl font-bold mb-4">나만의 AI 보이스를</h1>
            <h1 className="text-4xl font-bold mb-10 ">
              만들고 공유하고 활용하세요
            </h1>
            <GradientButton
              onClick={() => navigate(user ? '/voice-store' : '/sign-in')}
              className="text-lg py-3 px-8"
            >
              COVOS 시작하기
            </GradientButton>
          </div>
        </div>
      </section>

      <section className="bg-black py-20">
        <div className="max-w-5xl mx-auto px-4">
          <h2 className="text-white text-3xl font-bold text-center mb-20">
            COVOS만의 기능
          </h2>
          <div className="space-y-10">
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

      {/* Section 2 */}
      <section className="h-screen flex flex-col justify-center items-center text-black mt-10">
        <h2 className="text-2xl mb-2 font-semibold">🛍 마켓 플레이스</h2>
        <p className="mb-10">나만의 보이스팩을 업로드하고 수익을 창출하세요</p>
        <div className="flex justify-center items-center w-full h-2/3 bg-gray-400 opacity-30">
          LP 컴포넌트 구역
        </div>
      </section>
      <section className="bg-white py-20" id="why-join-nexters">
        <div className="max-w-5xl mx-auto px-4 text-center">
          <h2 className="text-4xl font-bold mb-16">
            왜 NEXTERS에 합류해야 할까요?
          </h2>
          <div className="grid md:grid-cols-3 gap-12">
            {benefits.map((benefit, index) => (
              <motion.div
                key={index}
                initial={{ opacity: 0, y: 50 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.6, delay: index * 0.2 }}
                className="p-8 rounded-2xl shadow-lg hover:shadow-2xl transition-shadow bg-gray-50"
              >
                <h3 className="text-2xl font-semibold mb-4">{benefit.title}</h3>
                <p className="text-gray-700 text-base">{benefit.description}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>
      {/* Section 3 */}
      <section className="h-screen flex flex-col justify-center items-center text-black">
        <h2 className="text-2xl mb-2 font-semibold">COVOS만의 기능</h2>
        <div className="flex justify-center gap-16 items-center w-full h-1/2 mt-14">
          <div className="card w-1/5 h-full bg-blue-200 rounded-lg flex items-center justify-center opacity-30">
            보이스팩 1
          </div>
          <div className="card w-1/5 h-full bg-blue-200 rounded-lg flex items-center justify-center opacity-30">
            보이스팩 2
          </div>
          <div className="card w-1/5 h-full bg-blue-200 rounded-lg flex items-center justify-center opacity-30">
            보이스팩 3
          </div>
        </div>
      </section>

      {/* Section 4 */}
      <section className="pt-40 pb-40 bg-black text-white text-center">
        <h2 className="text-4xl mb-8 font-semibold">
          지금,
          <br />
          당신의 AI 보이스를 만들어보세요.
        </h2>
        <button
          onClick={() => navigate(user ? '/voice-store' : '/sign-in')}
          className="bg-white text-blue-500 px-12 py-2 rounded font-semibold  relative z-10"
        >
          시작하기
        </button>
        <div className="absolute bottom-0  w-full z-0">
          <WaveAninmation />
        </div>
      </section>
    </div>
  );
};

export default Landing;
