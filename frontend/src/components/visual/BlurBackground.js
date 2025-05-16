import React from 'react';
import blur100 from '../../assets/blur100.svg';

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
      {/* 왼쪽 위에 크게 */}
      <img
        src={blur100}
        alt=""
        className="absolute top-[2200px] left-[-700px] w-[1600px] pointer-events-none z-0"
      />
      {/* 오른쪽 아래에 크게 */}
      <img
        src={blur100}
        alt=""
        className="absolute top-[2800px] right-[-550px] w-[1500px] pointer-events-none z-0"
      />
      {/* 왼쪽 위에 크게 */}
      <img
        src={blur100}
        alt=""
        className="absolute top-[3400px] left-[-700px] w-[1600px] pointer-events-none z-0"
      />
    </>
  );
};

export default BlurBackgrounds;
