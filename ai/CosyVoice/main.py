from fastapi import FastAPI, UploadFile, File, Form
from services.tts_service import TtsService
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()
tts_service = TtsService()

@app.post("/register_speaker")
async def register_speaker_endpoint(
    speaker_id: str = Form(...),
    prompt_text: str = Form(...),
    prompt_audio: UploadFile = File(...)
):
    """화자 등록 API 엔드포인트"""
    return {"speaker_id": await tts_service.extract_speaker_features(
        speaker_id=speaker_id,
        prompt_text=prompt_text,
        prompt_audio=prompt_audio
    )}

@app.post("/synthesize")
async def synthesize_endpoint(
    text: str = Form(...),
    speaker_id: str = Form(...),
    speed: float = Form(1.0)
):
    """음성 합성 API 엔드포인트"""
    result = await tts_service.generate_speech(
        text=text,
        speaker_id=speaker_id,
        speed=speed
    )
    return result

@app.get("/health")
def health_check():
    """서비스 상태 확인"""
    return {"status": "healthy"} 

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8080)