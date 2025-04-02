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
from .storage_manager import StorageManager
from config.settings import MODEL_CONFIG
import tempfile

logger = logging.getLogger(__name__)

class VoiceSynthesizer:
    def __init__(self):
        self._initialize_model()
        self.storage_manager = StorageManager()
        self.sample_rate = MODEL_CONFIG['sample_rate']

    def _initialize_model(self):
        """모델 초기화 및 로드"""
        model_path = MODEL_CONFIG['path']
        if not os.path.exists(model_path):
            snapshot_download(MODEL_CONFIG['model_id'], local_dir=model_path)
        
        self.model = CosyVoice2(model_path, load_jit=False, load_trt=False, fp16=False)
        logger.info("모델 로딩 완료")


    def _generate_speech_internal(
        self,
        text: str,
        features: dict,
        speed: float = 1.0
    ) -> tuple[bytes, float]:
        """음성 합성의 핵심 로직을 처리하는 내부 메소드"""
        outputs = self.model.inference_zero_shot(
            tts_text=text,
            features=features,
            speed=speed,
            stream=False
        )
        
        all_audio_data = [output['tts_speech'] for output in outputs]
        combined_audio = torch.cat(all_audio_data, dim=1)
        
        buffer = io.BytesIO()
        torchaudio.save(buffer, combined_audio, self.model.sample_rate, format="wav")
        buffer.seek(0)
        
        duration = combined_audio.shape[1] / self.sample_rate
        
        return buffer.getvalue(), duration


    async def extract_speaker_features(
        self,
        voicepackId: str,
        file_content: bytes
    ) -> str:
        try:
            """화자의 음성에서 특징을 추출하고 S3에 저장, 테스트 음성도 생성"""
            try:
                with tempfile.NamedTemporaryFile(suffix='.wav', delete=False) as temp_file:
                    temp_file.write(file_content)
                    temp_file.flush()
        
                    prompt_speech = load_wav(temp_file.name, self.sample_rate)
                    logger.info(f"음성 파일 로드 완료: {voicepackId}")

                os.unlink(temp_file.name)

            except Exception as e:
                    logger.error(f"음성 파일 처리 실패: {str(e)}")
                    raise HTTPException(
                        status_code=400,
                        detail="잘못된 음성 파일 형식입니다."
                    )

            features = self.model.frontend.frontend_zero_shot(
                prompt_speech_16k=prompt_speech,
                resample_rate=self.model.sample_rate
            )

            logger.info(f"화자 특징 추출 완료: {voicepackId}")
                
            # 특징을 s3에 저장
            if not self.storage_manager.save_speaker_features(voicepackId, features):
                logger.error(f"화자 특징 저장 실패: {voicepackId}")
                raise HTTPException(
                    status_code=503,
                    detail="S3 저장소 접근 오류"
                )
                
            logger.info(f"화자 특징 저장 완료: {voicepackId}")
                
            # 테스트 음성 생성
            test_text = "어제의 실패는 내일의 성공을 위한 발판입니다. 포기하지 않고 꾸준히 노력한다면 결국 원하는 목표에 도달할 수 있습니다."

            audio_data, _ = self._generate_speech_internal(
                text=test_text,
                features=features,
                speed=1.0
            )

            # 테스트 음성 저장
            test_filename = "sample_test.wav"
            file_path = f"speakers/{voicepackId}/{test_filename}"
            audio_url = self.storage_manager.save_audio(audio_data, file_path)
                
            if not audio_url:
                raise HTTPException(status_code=500, detail="Failed to save sample audio")
                
            return audio_url
        
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
        """기능 1: 베이직 기능에 사용되는 음성 생성"""
        try:
            if not self.storage_manager.speaker_exists(voicepackId):
                raise HTTPException(status_code=404, detail="Speaker not found")
            
            features = self.storage_manager.get_speaker_features(voicepackId)
            if features is None:
                raise HTTPException(status_code=500, detail="Failed to load speaker features")

            audio_data, duration = self._generate_speech_internal(
                text=prompt,
                features=features,
                speed=speed
            )
            
            # S3에 저장
            timestamp = time.strftime('%Y%m%d_%H%M%S')
            filename = f"speech_{timestamp}.wav"
            file_path = f"generated_audio/{userId}/{voicepackId}/{filename}"
            
            audio_url = self.storage_manager.save_audio(audio_data, file_path)
            
            if not audio_url:
                raise HTTPException(status_code=500, detail="Failed to save generated audio")

            return {
                "audio_url": audio_url,
                "duration": duration
            }
            
        except Exception as e:
            logger.error(f"음성 합성 실패: {str(e)}")
            raise 