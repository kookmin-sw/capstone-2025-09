from fastapi import FastAPI, UploadFile, File, Form
from services.tts_service import TtsService
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()
tts_service = TtsService()

@app.post("/register_speaker")
async def register_speaker_endpoint(
    voicepackId: str = Form(...),
    voiceFile: UploadFile = File(...)
):
    """화자 등록 API 엔드포인트"""
    return {"voicepackId": await tts_service.extract_speaker_features(
        voicepackId=voicepackId,
        voiceFile=voiceFile
    )}

@app.post("/synthesize")
async def synthesize_endpoint(
    prompt: str = Form(...),
    voicepackId: str = Form(...),
    speed: float = Form(1.0),
    userId: int = Form(...)
):
    """음성 합성 API 엔드포인트"""
    result = await tts_service.generate_speech(
        prompt=prompt,
        voicepackId=voicepackId,
        speed=speed,
        userId=userId
    )
    return result

@app.get("/health")
def health_check():
    """서비스 상태 확인"""
    return {"status": "healthy"} 

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8080)