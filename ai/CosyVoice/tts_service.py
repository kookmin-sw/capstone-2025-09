import sys
sys.path.append('third_party/Matcha-TTS')
from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.responses import Response
import torch
import torchaudio
import numpy as np
from cosyvoice.cli.cosyvoice import CosyVoice2
from cosyvoice.utils.file_utils import load_wav
from cosyvoice.utils.speaker_manager import SpeakerManager
import logging
import io
import time
from modelscope import snapshot_download
import os


# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()

class TtsService:
    def __init__(self, model_path: str):
        """CosyVoice2 모델 초기화"""
        self.model = CosyVoice2(model_path, load_jit=False, load_trt=False, fp16=False)
        self.sample_rate = 16000
        self.speaker_manager = SpeakerManager("./speaker_embeddings")
        logger.info("모델 로딩 완료")

    def extract_speaker_features(
        self,
        speaker_id: str,
        prompt_text: str,
        prompt_audio: UploadFile,
    ) -> str:
        """화자의 음성에서 특징을 추출하고 저장"""
        try:
            # 1. 프롬프트 오디오 로드
            prompt_speech = load_wav(prompt_audio.file, self.sample_rate)

            # prompt_text 전처리
            prompt_text = self.model.frontend.text_normalize(prompt_text, split=False)
            
            # 2. 화자 특징 추출
            features = self.model.frontend.frontend_zero_shot(
                tts_text="",  # 빈 텍스트로 전달
                prompt_text=prompt_text,
                prompt_speech_16k=prompt_speech,
                resample_rate=self.model.sample_rate
            )
                        
            # 4. 특징 저장
            self.speaker_manager.save_speaker(speaker_id, features)
            
            return speaker_id
            
        except Exception as e:
            logger.error(f"화자 특징 추출 실패: {str(e)}")
            raise

    async def generate_speech(
        self,
        text: str,
        speaker_id: str,
        speed: float = 1.0
    ) -> bytes:
        """저장된 화자 특징을 사용하여 음성 합성"""
        try:
            # 1. 화자 특징 로드
            if not self.speaker_manager.has_speaker(speaker_id):
                raise HTTPException(status_code=404, detail="Speaker not found")
            
            features = self.speaker_manager.get_speaker(speaker_id)

            # 3. 음성 합성 실행
            outputs = self.model.inference_zero_shot(
                tts_text=text,
                features=features,  # 이미 저장된 토큰 사용
                speed=speed,
                stream=False
            )
            
            # 4. 결과 처리
            all_audio_data = []
            
            # outputs를 순회하면서 오디오 데이터 수집 및 개별 저장
            for idx, output in enumerate(outputs):
                audio_data = output['tts_speech']
                all_audio_data.append(audio_data)
            
            # 모든 오디오 데이터를 시간 축으로 연결
            combined_audio = torch.cat(all_audio_data, dim=1)
            
            # 통합된 WAV 파일명 생성
            wav_filename = f"combined_speech_{time.strftime('%Y%m%d_%H%M%S')}.wav"
            
            # 버퍼에 저장
            buffer = io.BytesIO()
            torchaudio.save(buffer, combined_audio, self.model.sample_rate, format="wav")
            
            # 오디오 정보 로깅
            buffer_size = buffer.tell()
            total_duration = combined_audio.shape[1] / self.sample_rate
            logger.info(f"생성된 통합 오디오 크기: {buffer_size / 1024:.2f} KB")
            logger.info(f"총 재생 시간: {total_duration:.2f}초")
            del all_audio_data

            return Response(
                content=buffer.getvalue(),
                media_type="audio/wav",
                headers={
                    "Content-Disposition": f"attachment; filename={wav_filename}"
                }
            )

            
        except Exception as e:
            logger.error(f"음성 합성 실패: {str(e)}")
            raise

# 전역 서비스 인스턴스 생성
modelPath = "./pretrained_models/CosyVoice2-0.5B"

if not os.path.exists(modelPath):
    snapshot_download('iic/CosyVoice2-0.5B', local_dir=modelPath)

tts_service = TtsService(modelPath)

@app.post("/register_speaker")
async def register_speaker_endpoint(
    speaker_id: str = Form(...),
    prompt_text: str = Form(...),
    prompt_audio: UploadFile = File(...)
):
    """화자 등록 API 엔드포인트"""
    try:
        speaker_id = tts_service.extract_speaker_features(
            speaker_id=speaker_id,
            prompt_text=prompt_text,
            prompt_audio=prompt_audio
        )
        
        return {"speaker_id": speaker_id}
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/synthesize")
async def synthesize_endpoint(
    text: str = Form(...),
    speaker_id: str = Form(...),
    speed: float = Form(1.0)
):
    """음성 합성 API 엔드포인트"""
    try:
        logger.info(f"Received text: {text}")
        
        audio_bytes = await tts_service.generate_speech(
            text=text,
            speaker_id=speaker_id,
            speed=speed
        )
        
        return audio_bytes
        
    except HTTPException as e:
        raise e
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
def health_check():
    """서비스 상태 확인"""
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8080) 