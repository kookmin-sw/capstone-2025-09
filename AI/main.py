from fastapi import FastAPI, UploadFile, File, Form
from utils.voice_synthesizer import VoiceSynthesizer
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()
voice_synthesizer = VoiceSynthesizer()

@app.post("/register_speaker")
async def register_speaker_endpoint(
    voicepackId: str = Form(...),
    voiceFile: UploadFile = File(...)
):
    """화자 등록 API 엔드포인트"""
    result = await voice_synthesizer.extract_speaker_features(
        voicepackId=voicepackId,
        voiceFile=voiceFile
    )
    
    return {"sample_audio_url": result}

@app.post("/synthesize")
async def synthesize_endpoint(
    prompt: str = Form(...),
    voicepackId: str = Form(...),
    speed: float = Form(1.0),
    userId: int = Form(...)
):
    """음성 합성 API 엔드포인트"""
    result = await voice_synthesizer.generate_speech(
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