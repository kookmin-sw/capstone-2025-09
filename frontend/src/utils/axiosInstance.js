import axios from 'axios';

const axiosInstance = axios.create({

  baseURL: process.env.REACT_APP_BASE_URL,
  withCredentials: true, // 쿠키 전송 허용
});

export default axiosInstance;
