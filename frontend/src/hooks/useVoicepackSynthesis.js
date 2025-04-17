import axiosInstance from "../utils/axiosInstance";
import useUserStore from '../utils/userStore';

const useVoicepackSynthesis = () => {
  const user = useUserStore((state) => state.user);

  const synthesize = async ({ voicepackId, prompt }) => {
    if (!user || !user.id) {
      throw new Error("유저 정보가 없습니다.");
    }

    console.log(user.id)
    const res = await axiosInstance.post(`voicepack/synthesis?userId=${user.id}`, {
      voicepackId,
      prompt,
    });

    const location = res.headers["location"];
    return location?.replace("http://", "https://");
  };

  return { synthesize };
};

export default useVoicepackSynthesis;
