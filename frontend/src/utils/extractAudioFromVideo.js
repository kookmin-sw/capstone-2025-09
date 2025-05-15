import { FFmpeg } from '@ffmpeg/ffmpeg';

const ffmpeg = new FFmpeg();
let isLoaded = false;

export const extractAudioFromVideo = async (videoFile) => {
  if (!isLoaded) {
    await ffmpeg.load();
    isLoaded = true;
  }

  const data = await videoFile.arrayBuffer();
  await ffmpeg.writeFile('input.mp4', new Uint8Array(data));

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
  const audioFile = new File([audioBlob], 'output.wav', { type: 'audio/wav' });

  return audioFile;
};
