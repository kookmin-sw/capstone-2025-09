import React from 'react';
import blur50 from '../assets/blur50.svg';
import blur100 from '../assets/blur100.svg';
import blurGray from '../assets/blur-gray.svg';

const BlurBackgrounds = () => {
  return (
    <>
      {/* 왼쪽 위에 크게 */}
      <img
        src={blur100}
        alt=""
        className="absolute top-[-400px] left-[-600px] w-[1600px] pointer-events-none z-0"
      />
      <img
        src={blur100}
        alt=""
        className="absolute top-[-50px] right-[-550px] w-[1300px] pointer-events-none z-0"
      />
      {/* 왼쪽 위에 크게 */}
      <img
        src={blur100}
        alt=""
        className="absolute top-[900px] left-[-700px] w-[1600px] pointer-events-none z-0"
      />
      {/* 오른쪽 아래에 크게 */}
      <img
        src={blur100}
        alt=""
        className="absolute top-[1800px] right-[-550px] w-[1000px] pointer-events-none z-0"
      />
      {/* 랜딩 중간쯤 추가 */}
      <img
        src={blur100}
        alt=""
        className="absolute top-[100%] left-1/2 w-[600px] pointer-events-none z-0"
      />

      {/* 랜딩 마지막 근처 추가 */}
      <img
        src={blur100}
        alt=""
        className="absolute top-[240%] left-1/2 w-[750px] pointer-events-none z-0"
      />
    </>
  );
};

export default BlurBackgrounds;
