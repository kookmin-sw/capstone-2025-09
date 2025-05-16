import useUserStore from '../utils/userStore';
import axiosInstance from '../utils/axiosInstance';

const useAssistantSetup = () => {
  const user = useUserStore((state) => state.user);

  const postSettings = async (config) => {
    if (!user || !user.id) {
      console.error('userId가 없습니다.');
      return;
    }

    try {
      const response = await axiosInstance.post(
        `ai-assistant/settings`,
        {
          userId: user.id,
          voicepackId: config.voicepackId,
          writingStyle: config.writingStyle,
          categories: config.categories,
        },
        {
          headers: {
            'Content-Type': 'application/json',
          },
        }
      );
    } catch (error) {
      console.error('설정 저장 실패:', error);
    }
  };

  return postSettings;
};

export default useAssistantSetup;
