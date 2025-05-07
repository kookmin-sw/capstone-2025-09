import axiosInstance from '../utils/axiosInstance';

export const fetchUserInfo = async (userId) => {
  const response = await axiosInstance.get(`/users/me/${userId}`);
  return response.data;
};
