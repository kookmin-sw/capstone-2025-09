import React from 'react';
import { useNavigate } from 'react-router-dom';
import logo from '../assets/logo-new.svg';
import BlurBackgrounds from '../components/BlurBackground';
import WaveAninmation from '../components/WaveAninmation';
import { Canvas } from '@react-three/fiber';
import { OrbitControls } from '@react-three/drei';
import WaveSphere from '../components/WaveSphere';

const Landing = () => {
  const navigate = useNavigate();

  return (
    <div className="relative  overflow-hidden">
      {/* Blur background를 절대 위치로 */}
      <div className="absolute inset-0 z-0 pointer-events-none">
        <BlurBackgrounds />
      </div>

      {/* Header */}
      <header className="flex justify-between items-end h-[96px] px-28 z-10 relative ">
        <img src={logo} alt="COVOS" width={150} />
        <button
          onClick={() => navigate('/sign-in')}
          className="bg-gradient-to-r from-purple-400 to-blue-500 text-white font-semibold py-2 px-8 rounded-lg shadow-sm hover:opacity-90 transition"
        >
          로그인하기
        </button>
      </header>

      {/* Hero Section */}
      <section
        className="relative flex flex-col justify-center items-center"
        style={{ minHeight: 'calc(100vh - 96px)' }}
      >
        <div className="flex justify-center items-center h-full w-full">
          <div className="w-[80vh] h-[80vh] min-h-[300px] flex items-end ml-32">
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

          <div className="flex-1 flex flex-col items-start justify-start text-left px-4 text-black">
            <h1 className="text-4xl font-bold mb-4">나만의 AI 보이스를</h1>
            <h1 className="text-4xl font-bold mb-20 ">
              만들고 공유하고 활용하세요
            </h1>
            <button
              onClick={() => navigate('/sign-in')}
              className="bg-gradient-to-r from-purple-400 to-blue-500 text-white text-lg font-semibold py-3 px-12 rounded-lg shadow-sm hover:opacity-90 transition"
            >
              보이스팩 제작 시작하기
            </button>
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

      <section className="pt-40 pb-40 bg-black text-white text-center">
        <h2 className="text-4xl mb-8 font-semibold">
          지금,
          <br />
          당신의 AI 보이스를 만들어보세요.
        </h2>
        <button
          onClick={() => navigate('/sign-in')}
          className="bg-white text-blue-500 px-12 py-2 rounded font-semibold"
        >
          시작하기
        </button>
        <div className="absolute bottom-0">
          <WaveAninmation />
        </div>
      </section>
    </div>
  );
};

export default Landing;
