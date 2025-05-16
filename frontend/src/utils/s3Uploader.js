import axios from 'axios';

export const uploadToS3 = async (file, presignedUrl) => {
  await axios.put(presignedUrl, file, {
    headers: {
      'Content-Type': file.type,
    },
  });
};
