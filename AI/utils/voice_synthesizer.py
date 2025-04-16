import torch
import torchaudio
import logging
import io
import time
from fastapi import HTTPException
from zonos.model import Zonos
from zonos.conditioning import make_cond_dict
from zonos.utils import DEFAULT_DEVICE as device
from .storage_manager import StorageManager
import tempfile
import os
from config.settings import MODEL_CONFIG

logger = logging.getLogger(__name__)

class VoiceSynthesizer:
    def __init__(self):
        self._initialize_model()
        self.storage_manager = StorageManager()
        self.sample_rate = MODEL_CONFIG['sample_rate']  # Zonos 모델의 샘플링 레이트에 맞게 설정

    def _initialize_model(self):
        """모델 초기화 및 로드"""
        try:
            model_path = MODEL_CONFIG['path']
            self.model = Zonos.from_pretrained(model_path, device=device)
            self.model.eval()  # 추론 모드로 설정
            logger.info("Zonos model loaded")
        except Exception as e:
            logger.error(f"Failed to load Zonos model: {e}")
            raise

    def _synthesize_speech_internal(
        self,
        text: str,
        features: torch.Tensor,
        speed: float = 1.0,
        language: str = "ko"
    ) -> tuple[bytes, float]:
        """음성 합성의 핵심 로직을 처리하는 내부 메소드"""
        try:
            # 컨디셔닝 생성, 추가 파라미터는 이곳에 추가가
            cond_dict = make_cond_dict(text=text, speaker=features, language=language)
            conditioning = self.model.prepare_conditioning(cond_dict)

            # 음성 생성
            codes = self.model.generate(conditioning)
            wavs = self.model.autoencoder.decode(codes).cpu()

            # 오디오 버퍼 생성 및 저장
            buffer = io.BytesIO()
            torchaudio.save(buffer, wavs[0], self.model.autoencoder.sampling_rate, format="wav")
            buffer.seek(0)

            duration = wavs[0].shape[1] / self.model.autoencoder.sampling_rate

            return buffer.getvalue(), duration

        except Exception as e:
            logger.error(f"Error during synthesis: {e}")
            raise

    async def extract_speaker_features(
        self,
        voicepackId: str,
        file_content: bytes
    ) -> str:
        """화자의 음성에서 특징을 추출하고 S3에 저장, 테스트 음성도 생성"""
        try:
            # 임시 파일 생성 및 사용
            with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as temp_file:
                temp_file.write(file_content)
                temp_file_path = temp_file.name

            # 음성 파일 로드
            wav, sampling_rate = torchaudio.load(temp_file_path)

            # 화자 임베딩 생성
            speaker_embedding = self.model.make_speaker_embedding(wav, sampling_rate)

            # 특징을 s3에 저장
            if not self.storage_manager.save_speaker_features(voicepackId, speaker_embedding):
                logger.error(f"failed to save speaker features: {voicepackId}")
                raise HTTPException(
                    status_code=503,
                    detail="S3 저장소 접근 오류"
                )

            logger.info(f"speaker features saved: {voicepackId}")

            # 테스트 음성 생성
            test_text = "어제의 실패는 내일의 성공을 위한 발판입니다. 포기하지 않고 꾸준히 노력한다면 결국 원하는 목표에 도달할 수 있습니다."

            audio_data, duration = self._synthesize_speech_internal(
                text=test_text,
                features=speaker_embedding,
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
            logger.error(f"failed to extract speaker features: {str(e)}")
            raise
        finally:
            # 임시 파일 삭제
            if os.path.exists(temp_file_path):
                os.remove(temp_file_path)


    async def synthesize_speech(
        self,
        prompt: str,
        voicepackName: str,
        userId: int,
        speed: float = 1.0,
    ) -> dict:
        """기능 1: 베이직 기능에 사용되는 음성 합성"""
        try:
            if not self.storage_manager.speaker_exists(voicepackName):
                raise HTTPException(status_code=404, detail="Speaker not found")

            features = self.storage_manager.get_speaker_features(voicepackName)
            if features is None:
                raise HTTPException(status_code=500, detail="Failed to load speaker features")

            audio_data, duration = self._synthesize_speech_internal(
                text=prompt,
                features=features,
                speed=speed
            )

            # S3에 저장
            timestamp = time.strftime('%Y%m%d_%H%M%S')
            filename = f"speech_{timestamp}.wav"
            file_path = f"generated_audio/{userId}/{voicepackName}/{filename}"

            audio_url = self.storage_manager.save_audio(audio_data, file_path)

            if not audio_url:
                raise HTTPException(status_code=500, detail="Failed to save generated audio")

            return audio_url, duration

        except Exception as e:
            logger.error(f"failed to generate speech: {str(e)}")
            raise
        
            
    async def synthesize_assistant(
        self,
        prompt: str,
        voicepackName: str,
        category: str,
        writingStyle: str,
        nowTime: str,
        speed: float = 1.0
        ):
            """AI 비서용 음성 합성"""
            try:
                if not self.storage_manager.speaker_exists(voicepackName):
                    raise HTTPException(status_code=404, detail="Speaker not found")
                
                features = self.storage_manager.get_speaker_features(voicepackName)
                if features is None:
                    raise HTTPException(status_code=500, detail="Failed to load speaker features")
                
                audio_data, duration = self._synthesize_speech_internal(
                    text=prompt,
                    features=features,
                    speed=speed
                )
                
                if audio_data is None:
                    raise ValueError("Failed to synthesize assistant speech")
                
                # S3에 저장
                file_path = f"ai_assistant/{voicepackName}/{nowTime}/{category}/{writingStyle}.wav"
                audio_url = self.storage_manager.save_audio(audio_data, file_path)
                
                if not audio_url:
                    raise HTTPException(status_code=500, detail="Failed to save assistant audio")
                
                return audio_url, duration
            
            except Exception as e:
                logger.error(f"failed to synthesize assistant speech: {str(e)}")
                raise