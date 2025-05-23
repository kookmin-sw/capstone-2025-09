import React, { useEffect, useRef, useState } from 'react';
import WaveSurfer from 'wavesurfer.js';
import { Play, Pause } from 'lucide-react';

const AudioListPlayer = ({ audioUrls }) => {
  const waveformRef = useRef(null);
  const wavesurfer = useRef(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState('00:00');
  const [duration, setDuration] = useState('00:00');

  const formatTime = (seconds) => {
    if (isNaN(seconds)) return '00:00';
    const mins = String(Math.floor(seconds / 60)).padStart(2, '0');
    const secs = String(Math.floor(seconds % 60)).padStart(2, '0');
    return `${mins}:${secs}`;
  };

  const encodeWav = (audioBuffer) => {
    const numChannels = audioBuffer.numberOfChannels;
    const sampleRate = audioBuffer.sampleRate;
    const length = audioBuffer.length * numChannels * 2;
    const buffer = new ArrayBuffer(44 + length);
    const view = new DataView(buffer);

    const writeString = (offset, str) => {
      for (let i = 0; i < str.length; i++) {
        view.setUint8(offset + i, str.charCodeAt(i));
      }
    };

    let offset = 0;
    writeString(offset, 'RIFF');
    offset += 4;
    view.setUint32(offset, 36 + length, true);
    offset += 4;
    writeString(offset, 'WAVE');
    offset += 4;
    writeString(offset, 'fmt ');
    offset += 4;
    view.setUint32(offset, 16, true);
    offset += 4;
    view.setUint16(offset, 1, true);
    offset += 2; // PCM
    view.setUint16(offset, numChannels, true);
    offset += 2;
    view.setUint32(offset, sampleRate, true);
    offset += 4;
    view.setUint32(offset, sampleRate * numChannels * 2, true);
    offset += 4;
    view.setUint16(offset, numChannels * 2, true);
    offset += 2;
    view.setUint16(offset, 16, true);
    offset += 2; // bit depth
    writeString(offset, 'data');
    offset += 4;
    view.setUint32(offset, length, true);
    offset += 4;

    const interleaved = new Int16Array(length / 2);
    for (let i = 0; i < audioBuffer.length; i++) {
      for (let ch = 0; ch < numChannels; ch++) {
        const sample = audioBuffer.getChannelData(ch)[i] || 0;
        const s = Math.max(-1, Math.min(1, sample));
        interleaved[i * numChannels + ch] = s < 0 ? s * 0x8000 : s * 0x7fff;
      }
    }

    for (let i = 0; i < interleaved.length; i++) {
      view.setInt16(44 + i * 2, interleaved[i], true);
    }

    return new Blob([view], { type: 'audio/wav' });
  };

  const loadMergedAudio = async () => {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    const decodedBuffers = [];

    for (const url of audioUrls) {
      try {
        const res = await fetch(url);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const arrayBuffer = await res.arrayBuffer();
        const decoded = await ctx.decodeAudioData(arrayBuffer);
        if (decoded.numberOfChannels > 0) {
          decodedBuffers.push(decoded);
        } else {
          console.warn(`⚠️ Skipped (0 channel): ${url}`);
        }
      } catch (e) {
        console.warn(`❌ Failed to decode ${url}`, e);
      }
    }

    if (decodedBuffers.length === 0) {
      console.error('❌ No valid audio to merge.');
      return;
    }

    const totalLength = decodedBuffers.reduce((sum, b) => sum + b.length, 0);
    const channels = Math.max(...decodedBuffers.map((b) => b.numberOfChannels));
    if (!channels) return;

    const merged = ctx.createBuffer(channels, totalLength, ctx.sampleRate);
    let offset = 0;
    for (const buf of decodedBuffers) {
      for (let ch = 0; ch < channels; ch++) {
        const input = buf.getChannelData(
          Math.min(ch, buf.numberOfChannels - 1)
        );
        merged.getChannelData(ch).set(input, offset);
      }
      offset += buf.length;
    }

    const blob = encodeWav(merged);
    const wavUrl = URL.createObjectURL(blob);

    // 안전하게 DOM 렌더 이후에 WaveSurfer 생성
    setTimeout(() => {
      if (!waveformRef.current) {
        console.warn('Waveform container not ready');
        return;
      }

      if (wavesurfer.current) wavesurfer.current.destroy();

      wavesurfer.current = WaveSurfer.create({
        container: waveformRef.current,
        waveColor: '#c7d2fe',
        progressColor: '#6366f1',
        cursorColor: '#6366f1',
        height: 80,
        barWidth: 2,
        barGap: 1.5,
        responsive: true,
      });

      wavesurfer.current.load(wavUrl);

      wavesurfer.current.on('ready', () => {
        setDuration(formatTime(wavesurfer.current.getDuration()));
        setCurrentTime('00:00');
      });

      wavesurfer.current.on('audioprocess', () => {
        const t = wavesurfer.current.getCurrentTime();
        setCurrentTime(formatTime(t));
      });

      wavesurfer.current.on('finish', () => {
        setIsPlaying(false);
      });
    }, 0);
  };

  const togglePlay = () => {
    if (!wavesurfer.current) return;
    if (isPlaying) {
      wavesurfer.current.pause();
      setIsPlaying(false);
    } else {
      wavesurfer.current.play();
      setIsPlaying(true);
    }
  };

  useEffect(() => {
    loadMergedAudio();
    return () => wavesurfer.current?.destroy();
  }, [audioUrls]);

  return (
    <div className="w-full flex flex-col space-y-4 px-6 py-5">
      <div className="flex items-center justify-between space-x-4">
        <button
          onClick={togglePlay}
          className="w-12 h-12 rounded-full bg-indigo-500 text-white text-xl flex items-center justify-center shadow-md hover:bg-indigo-300 transition"
        >
          {isPlaying ? <Pause /> : <Play />}
        </button>
        <div className="flex-1 mx-4">
          <div
            ref={waveformRef}
            className="w-full h-[80px] overflow-hidden scrollbar-hide"
          />
        </div>
        <span className="text-indigo-500 font-semibold whitespace-nowrap w-[120px] text-right">
          {currentTime} / {duration}
        </span>
      </div>
    </div>
  );
};

export default AudioListPlayer;
