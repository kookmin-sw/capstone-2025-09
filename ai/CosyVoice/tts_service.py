import sys
sys.path.append('third_party/Matcha-TTS')
from fastapi import FastAPI, UploadFile, File, Form
from fastapi.responses import Response
import torch
import torchaudio
import numpy as np
from cosyvoice.cli.cosyvoice import CosyVoice2
from cosyvoice.utils.file_utils import load_wav
import logging
import io
import time


# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()

class TTSService:
    def __init__(self, model_path: str):
        """CosyVoice2 모델 초기화"""
        self.model = CosyVoice2(model_path, load_jit=False, load_trt=False, fp16=False)
        self.sample_rate = 16000
        logger.info("모델 로딩 완료")

    async def generate_speech(
        self,
        text: str,
        prompt_text: str,
        prompt_audio: UploadFile,
        speed: float = 1.0
    ) -> bytes:
        """음성 합성 실행"""
        try:
            # 1. 프롬프트 오디오 로드
            prompt_speech = load_wav(prompt_audio.file, self.sample_rate)
            
            # 2. 음성 합성 실행
            outputs = self.model.inference_zero_shot(
                tts_text=text,
                prompt_text=prompt_text,
                prompt_speech_16k=prompt_speech,
                speed=speed,
                stream=False
            )
            
            # 3. 결과 처리
            audio_data = next(outputs)['tts_speech']
            
            # 4. WAV 파일로 저장
            # buffer = io.BytesIO()
            torchaudio.save(f"test_{time.time()}.wav", audio_data, self.model.sample_rate, format="wav")
            
            return None
            
        except Exception as e:
            logger.error(f"음성 합성 실패: {str(e)}")
            raise

# 전역 서비스 인스턴스 생성
MODEL_PATH = "./pretrained_models/CosyVoice2-0.5B"
tts_service = TTSService(MODEL_PATH)

@app.post("/synthesize")
async def synthesize_endpoint(
    text: str = Form(...),
    prompt_text: str = Form(...),
    prompt_audio: UploadFile = File(...),
    speed: float = Form(1.0)
):
    """음성 합성 API 엔드포인트"""
    try:
        audio_bytes = await tts_service.generate_speech(
            text=text,
            prompt_text=prompt_text,
            prompt_audio=prompt_audio,
            speed=speed
        )
        
        return Response(
            content=audio_bytes,
            media_type="audio/wav",
            headers={
                "Content-Disposition": "attachment; filename=synthesized_speech.wav"
            }
        )
        
    except Exception as e:
        logger.error(f"API 오류: {str(e)}")
        return {"error": str(e)}, 500

@app.get("/health")
def health_check():
    """서비스 상태 확인"""
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8080) 