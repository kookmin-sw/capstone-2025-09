import axiosInstance from '../utils/axiosInstance';

export const getVoicepacksByUserId = async (userId, filter = 'available') => {
  const response = await axiosInstance.get('/voicepack', {
    params: { userId, filter },
  });
  return response.data;
};
