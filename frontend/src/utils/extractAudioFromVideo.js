// utils/extractAudioFromVideo.js
import { FFmpeg } from '@ffmpeg/ffmpeg';

const ffmpeg = new FFmpeg();

let isLoaded = false;

export const extractAudioFromVideo = async (videoFile) => {
  if (!isLoaded) {
    await ffmpeg.load(); // ✅ coreURL 생략
    isLoaded = true;
  }

  const data = await videoFile.arrayBuffer();
  ffmpeg.writeFile('input.mp4', new Uint8Array(data));

  await ffmpeg.exec([
    '-i',
    'input.mp4',
    '-vn',
    '-acodec',
    'pcm_s16le',
    '-ar',
    '44100',
    '-ac',
    '2',
    'output.wav',
  ]);

  const output = await ffmpeg.readFile('output.wav');
  const audioBlob = new Blob([output], { type: 'audio/wav' });
  const audioUrl = URL.createObjectURL(audioBlob);

  return audioUrl;
};
