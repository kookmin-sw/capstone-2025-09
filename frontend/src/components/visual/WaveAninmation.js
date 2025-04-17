import React, { useEffect, useRef } from 'react';

const WaveAninmation = () => {
  const canvasRef = useRef(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');
    let animationId;
    let phase = 0;
    let fallbackTime = 0;
    let currentVolume = 0.3;

    const draw = () => {
      const { width, height } = canvas;
      ctx.clearRect(0, 0, width, height);

      const waves = 3;
      const colors = ['#5255EA', '#7852EA', '#C273FF'];

      const baseFreq = 0.009;
      const baseAmp = 45;
      const speed = 0.03;

      const floatPulse = 0.8 + Math.sin(phase * 0.5) * 0.3;

      for (let i = 0; i < waves; i++) {
        const freq = baseFreq + i * 0.0025;
        const amp = baseAmp * (1 + i * 0.3) * currentVolume * floatPulse;
        const offset = (i * Math.PI) / 2;

        ctx.beginPath();
        ctx.moveTo(0, height);

        for (let x = 0; x <= width; x++) {
          const y =
            height / 2 +
            Math.sin(x * freq + phase + offset) *
              amp *
              Math.cos((x + phase) * 0.004);
          ctx.lineTo(x, y);
        }

        ctx.lineTo(width, height);
        ctx.closePath();
        ctx.fillStyle = colors[i];
        ctx.globalAlpha = 0.25 - i * 0.05;
        ctx.fill();
      }

      ctx.globalAlpha = 1;
      phase += speed;
    };

    const fallbackRender = () => {
      fallbackTime += 0.03;
      currentVolume = 0.3 + Math.abs(Math.sin(fallbackTime)) * 0.3;
      draw();
      animationId = requestAnimationFrame(fallbackRender);
    };

    fallbackRender();

    return () => {
      cancelAnimationFrame(animationId);
    };
  }, []);

  return (
    <canvas
      ref={canvasRef}
      width={window.innerWidth}
      height={200}
      className="w-full z-10 flex justify-end"
    />
  );
};

export default WaveAninmation;
