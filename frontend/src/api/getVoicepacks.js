import axiosInstance from '../utils/axiosInstance';

export const getVoicepacksByUserId = async (userId) => {
  const response = await axiosInstance.get('/voicepack/usage-right', {
    params: { userId },
  });
  return response.data;
};
