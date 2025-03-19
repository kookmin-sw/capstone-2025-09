import sys
sys.path.append('third_party/Matcha-TTS')
from fastapi import HTTPException, UploadFile
from fastapi.responses import Response
import torch
import torchaudio
import logging
import io
import time
from modelscope import snapshot_download
import os
from cosyvoice.cli.cosyvoice import CosyVoice2
from cosyvoice.utils.file_utils import load_wav
from .s3_service import S3Service
from config.settings import MODEL_CONFIG

logger = logging.getLogger(__name__)

class TtsService:
    def __init__(self):
        self._initialize_model()
        self.s3_service = S3Service()
        self.sample_rate = MODEL_CONFIG['sample_rate']

    def _initialize_model(self):
        """모델 초기화 및 로드"""
        model_path = MODEL_CONFIG['path']
        if not os.path.exists(model_path):
            snapshot_download(MODEL_CONFIG['model_id'], local_dir=model_path)
        
        self.model = CosyVoice2(model_path, load_jit=False, load_trt=False, fp16=False)
        logger.info("모델 로딩 완료")

    async def extract_speaker_features(
        self,
        voicepackId: str,
        voiceFile: UploadFile
    ) -> str:
        """화자의 음성에서 특징을 추출하고 S3에 저장"""
        try:
            prompt_speech = load_wav(voiceFile.file, self.sample_rate)

            features = self.model.frontend.frontend_zero_shot(
                prompt_speech_16k=prompt_speech,
                resample_rate=self.model.sample_rate
            )
            
            if self.s3_service.save_speaker_features(voicepackId, features):
                return voicepackId
            else:
                raise HTTPException(status_code=500, detail="Failed to save speaker features")
            
        except Exception as e:
            logger.error(f"화자 특징 추출 실패: {str(e)}")
            raise

    async def generate_speech(
        self,
        prompt: str,
        voicepackId: str,
        userId: int,
        speed: float = 1.0,
    ) -> dict:
        """음성 합성 및 S3 저장"""
        try:
            if not self.s3_service.speaker_exists(voicepackId):
                raise HTTPException(status_code=404, detail="Speaker not found")
            
            features = self.s3_service.get_speaker_features(voicepackId)
            if features is None:
                raise HTTPException(status_code=500, detail="Failed to load speaker features")

            outputs = self.model.inference_zero_shot(
                tts_text=prompt,
                features=features,
                speed=speed,
                stream=False
            )
            
            # 오디오 데이터 처리
            all_audio_data = [output['tts_speech'] for output in outputs]
            combined_audio = torch.cat(all_audio_data, dim=1)
            
            # WAV 파일로 변환
            buffer = io.BytesIO()
            torchaudio.save(buffer, combined_audio, self.model.sample_rate, format="wav")
            buffer.seek(0)
            
            # 파일명 생성
            timestamp = time.strftime('%Y%m%d_%H%M%S')
            filename = f"speech_{timestamp}.wav"
            
            # S3에 저장
            audio_url = self.s3_service.save_generated_audio(
                voicepackId=voicepackId,
                audio_data=buffer.getvalue(),
                filename=filename,
                userId=userId
            )
            
            if not audio_url:
                raise HTTPException(status_code=500, detail="Failed to save generated audio")

            return {
                "audio_url": audio_url,
                "duration": combined_audio.shape[1] / self.sample_rate
            }
            
        except Exception as e:
            logger.error(f"음성 합성 실패: {str(e)}")
            raise 