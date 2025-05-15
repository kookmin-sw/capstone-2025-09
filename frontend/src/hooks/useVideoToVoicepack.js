import axiosInstance from '../utils/axiosInstance';
import axios from 'axios';
import useUserStore from '../utils/userStore';
import useVoicepackSynthesis from './useVoicepackSynthesis';

const generateVoicepackName = (userId) => {
  const now = new Date();
  const pad = (n) => n.toString().padStart(2, '0');
  return `${userId}_${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}_${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}`;
};

const useVideoToVoicepack = () => {
  const user = useUserStore((state) => state.user);
  const voicepackName = generateVoicepackName(user.id);
  const { synthesize } = useVoicepackSynthesis();

  const startConversion = async ({ audioFile, prompt, emotionIndex }) => {
    const presignedRes = await axiosInstance.get(
      'video2voicepack/presigned-url'
    );
    const presignedUrl = presignedRes.data;

    await axios.put(presignedUrl, audioFile, {
      headers: { 'Content-Type': audioFile.type },
    });

    const convertRes = await axiosInstance.post(
      'video2voicepack/convert',
      null,
      {
        params: {
          userId: user.id,
          name: voicepackName,
          putUrl: presignedUrl,
        },
      }
    );

    const voicePackId = convertRes.data.id;

    // 1차 polling: 보이스팩 생성 상태 확인
    const pollConversion = async (retry = 0) => {
      if (retry > 30) throw new Error('보이스팩 생성 polling 시간 초과');
      await new Promise((r) => setTimeout(r, 3000));
      const { data } = await axiosInstance.get(
        `voicepack/convert/status/${voicePackId}`
      );
      if (data.status === 'COMPLETED') return data.voicepackId;
      if (data.status === 'FAILED') throw new Error('보이스팩 생성 실패');
      return pollConversion(retry + 1);
    };

    const processedAudioId = await pollConversion();

    // 합성 요청
    const statusUrl = await synthesize({
      voicepackId: processedAudioId,
      prompt,
      emotionIndex,
    });

    // 2차 polling: 합성된 오디오 status 확인
    const pollSynthesis = async (retry = 0) => {
      if (retry > 30) throw new Error('음성 합성 polling 시간 초과');
      await new Promise((r) => setTimeout(r, 3000));

      const res = await fetch(statusUrl, {
        method: 'GET',
        credentials: 'include',
      });
      const result = await res.json();

      if (result.status === 'COMPLETED' && result.resultUrl) {
        const audioRes = await fetch(result.resultUrl);
        const audioBlob = await audioRes.blob();
        return URL.createObjectURL(audioBlob);
      }

      if (result.status === 'FAILED') throw new Error('음성 합성 실패');

      return await pollSynthesis(retry + 1);
    };

    const finalAudioUrl = await pollSynthesis();
    return finalAudioUrl;
  };

  return { startConversion };
};

export default useVideoToVoicepack;
