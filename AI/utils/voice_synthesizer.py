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
from .text_converter import convert_text

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
            sentences = convert_text(text)
            audio_tensors = []
            total_duration = 0
            
            # 정적(silence) 설정
            sample_rate = self.model.autoencoder.sampling_rate
            silence_duration_seconds = 0.3
            num_silence_samples = int(silence_duration_seconds * sample_rate)
            # 정적 텐서 생성
            silence_tensor = torch.zeros((1, num_silence_samples)) 

            for i, sentence in enumerate(sentences):
                if i == 0:
                    seed = torch.randint(0, 2**32 - 1, (1,)).item()
                    logger.info(f"seed: {seed}")
                    
                torch.manual_seed(seed)                
                # 컨디셔닝 생성, 추가 파라미터는 이곳에 추가
                logger.info(f'{i+1}/{len(sentences)} 번째 문장 합성: {sentence}')
                cond_dict = make_cond_dict(text=sentence, speaker=features, language=language)
                conditioning = self.model.prepare_conditioning(cond_dict)

                # 음성 생성
                codes = self.model.generate(conditioning)
                wavs = self.model.autoencoder.decode(codes).cpu()

                sentence_tensor = wavs[0]
                sentence_duration = sentence_tensor.shape[1] / sample_rate

                if i > 0:
                    audio_tensors.append(silence_tensor)
                    total_duration += silence_duration_seconds
                
                audio_tensors.append(sentence_tensor)
                total_duration += sentence_duration

            # 모든 오디오 텐서 합치기
            if not audio_tensors:
                # 생성된 오디오가 없는 경우 빈 바이트와 0초 반환
                return b"", 0.0
                
            combined_wav = torch.cat(audio_tensors, dim=1)

            # 최종 WAV 데이터를 저장할 버퍼 생성
            final_buffer = io.BytesIO()
            torchaudio.save(final_buffer, combined_wav, self.model.autoencoder.sampling_rate, format="wav")
            final_buffer.seek(0)

            return final_buffer.getvalue(), total_duration

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

            # 텍스트 전처리 후 음성 합성
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