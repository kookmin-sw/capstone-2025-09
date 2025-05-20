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
import json
import random

logger = logging.getLogger(__name__)

# 감정 프로필 순서: default, Happiness, Sadness, Surprise, Anger
EMOTION_PROFILE = [
    [0.3077, 0.0256, 0.0256, 0.0256, 0.0256, 0.0256, 0.2564, 0.3077], # default
    [0.950, 0.050, 0.050, 0.050, 0.050, 0.050, 0.050, 0.050], # Happiness
    [0.050, 0.950, 0.050, 0.050, 0.050, 0.050, 0.050, 0.050], # Sadness
    [0.050, 0.050, 0.050, 0.950, 0.050, 0.050, 0.050, 0.050], # Surprise
    [0.050, 0.050, 0.050, 0.050, 0.050, 0.950, 0.050, 0.050], # Anger
]

class VoiceSynthesizer:
    def __init__(self):
        self._initialize_model()
        self.storage_manager = StorageManager()

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
        language: str = "ko",
        emotionIndex: int = 0
    ) -> tuple[bytes, float]:
        """음성 합성의 핵심 로직을 처리하는 내부 메소드"""
        try:
            # 텍스트 전처리 및 안전성 검사
            is_safe, sentences = convert_text(text)

            if not is_safe:
                raise ValueError("부적절한 표현이 감지되어 음성 합성을 진행할 수 없습니다.")

            total_sentences = len(sentences)
            
            audio_chunks = []
            t0 = time.time()
            ttfb = None
            generated = 0
            
            current_emotion_profile = EMOTION_PROFILE[emotionIndex]
            
            def generator():
                for text in sentences:
                    elapsed = int((time.time() - t0) * 1000)
                    yield {
                        "text": text,
                        "speaker": features,
                        "language": language,
                        "emotion": current_emotion_profile
                    }
                    
            stream_generator = self.model.stream(
                cond_dicts_generator=generator(),
                chunk_schedule=[22, 13, *range(12, 100)],
                chunk_overlap=1,
                mark_boundaries=True
            )
            
            current_sentence = 1
            for i, audio_chunk in enumerate(stream_generator):
                if isinstance(audio_chunk, str):
                    logger.info(f"{current_sentence} / {total_sentences} 문장 생성: {audio_chunk}")
                    current_sentence += 1
                    continue
                
                audio_chunks.append(audio_chunk)
                elapsed = int((time.time() - t0) * 1000)
                if ttfb is None:
                    ttfb = elapsed
                gap = 'GAP' if ttfb + generated < elapsed else ""
                generated += int(audio_chunk.shape[1] / 44.1)
                
            combined_wav = torch.cat(audio_chunks, dim=-1).cpu()            
            duration = round(combined_wav.shape[1] / 44100, 3)

            # 최종 WAV 데이터를 저장할 버퍼 생성
            final_buffer = io.BytesIO()
            torchaudio.save(final_buffer, combined_wav, self.model.autoencoder.sampling_rate, format="wav")
            final_buffer.seek(0)

            return final_buffer.getvalue(), duration

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

            try:
                # 테스트 문장 로드 및 랜덤 선택
                current_dir = os.path.dirname(os.path.abspath(__file__))
                json_file_path = os.path.join(current_dir, '..', 'config', 'sample_texts.json')
                with open(json_file_path, 'r', encoding='utf-8') as f:
                    test_texts = json.load(f)
                test_text = random.choice(test_texts)
                
            except FileNotFoundError:
                logger.error(f"sample_texts.json not found. Using default text.")
                test_text = "AI 보이스팩 거래 플랫폼, 코보스입니다! 코보스를 통해 목소리의 가치를 재정의해보세요."
            except json.JSONDecodeError:
                logger.error(f"Error decoding sample_texts.json. Using default text.")
                test_text = "AI 보이스팩 거래 플랫폼, 코보스입니다! 코보스를 통해 목소리의 가치를 재정의해보세요."
            except Exception as e:
                logger.error(f"Error loading sample texts. Using default text.")
                test_text = "AI 보이스팩 거래 플랫폼, 코보스입니다! 코보스를 통해 목소리의 가치를 재정의해보세요."

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
        emotionIndex: int = 0,
    ) -> dict:
        """베이직 보이스에 사용되는 음성 합성"""
        try:
            if not self.storage_manager.speaker_exists(voicepackName):
                raise HTTPException(status_code=404, detail="Speaker not found")

            features = self.storage_manager.get_speaker_features(voicepackName)
            if features is None:
                raise HTTPException(status_code=500, detail="Failed to load speaker features")

            audio_data, duration = self._synthesize_speech_internal(
                text=prompt,
                features=features,
                speed=speed,
                emotionIndex=emotionIndex
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
                
            # 파일 경로 생성
            file_path = f"ai-assistant/{voicepackName}/{nowTime}/{category}/{writingStyle}.wav"
            
            # 파일이 이미 존재하는지 확인
            existing_url = self.storage_manager.get_audio_url(file_path)
            if existing_url:
                logger.info(f"Found existing audio file: {file_path}")
                return file_path, 0
                
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
            audio_url = self.storage_manager.save_audio(audio_data, file_path)
                
            if not audio_url:
                raise HTTPException(status_code=500, detail="Failed to save assistant audio")
                
            return audio_url, duration
            
        except Exception as e:
            logger.error(f"failed to synthesize assistant speech: {str(e)}")
            raise