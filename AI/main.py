from fastapi import FastAPI, UploadFile, File, Form, HTTPException
import logging
from fastapi.responses import JSONResponse
from utils.voice_registration_handler import process_voice_registration
from utils.synthesis_handler import process_synthesis_request, process_assistant_request
from contextlib import asynccontextmanager
from utils.voice_synthesizer import VoiceSynthesizer
from zonos.utils import DEFAULT_DEVICE as device
import torch

logging.basicConfig(level=logging.INFO)

logger = logging.getLogger(__name__)

synthesizer_instance = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    global synthesizer_instance
    logger.info("Application startup...")
    try:
        logger.info("Initializing VoiceSynthesizer and performing initial synthesis...")
        synthesizer_instance = VoiceSynthesizer()

        initial_text = "애플리케이션 시작 초기화 음성입니다."
        embedding_dim = 128
        dummy_features = torch.randn(1, embedding_dim, dtype=torch.bfloat16).to(device)
        initial_emotion_index = 0

        audio_data, duration = synthesizer_instance._synthesize_speech_internal(
            text=initial_text,
            features=dummy_features,
            speed=1.0,
            language="ko",
            emotionIndex=initial_emotion_index
        )
        if audio_data and duration > 0:
            logger.info(f"Initial voice synthesis successful. Duration: {duration}s. Audio data length: {len(audio_data)}")
        else:
            logger.warning("Initial voice synthesis may have failed or produced no audio.")
        logger.info("VoiceSynthesizer initialized and initial synthesis complete.")
    except Exception as e:
        logger.error(f"Error during application startup (initial synthesis): {e}")
    
    yield

    logger.info("Application shutdown...")
    synthesizer_instance = None

app = FastAPI(lifespan=lifespan)
logging.info("Starting server... ----------------------------------------------------------------------------")

@app.post("/register_speaker")
async def register_speaker_endpoint(
    voicepackId: str = Form(...),
    voiceFile: UploadFile = File(...),
    voicepackRequestId: int = Form(...)
):
    """화자 등록 API 엔드포인트"""
    try:
        # 파일 데이터 읽기
        file_content = await voiceFile.read()

        await process_voice_registration(
            voicepackId=voicepackId,
            file_content=file_content,
            voicepackRequestId=voicepackRequestId
        )

        return JSONResponse(
            status_code=200, 
            content={
                "status": "completed",
                "message": "화자 등록이 완료되었습니다.",
                "voicepackRequestId": voicepackRequestId
            }
        )

    except Exception as e:
        logger.error(f"failed to register speaker: {str(e)}")
        raise HTTPException(status_code=500, detail="화자 등록 요청 처리 중 오류가 발생했습니다.")

@app.post("/synthesize")
async def synthesize_endpoint(
    prompt: str = Form(...),
    voicepackName: str = Form(...),
    userId: int = Form(...),
    jobId: int = Form(...),
    speed: float = Form(1.0),
    emotionIndex: int = Form(0)
):
    """음성 합성 API 엔드포인트"""
    try:
        await process_synthesis_request(
            prompt=prompt,
            voicepackName=voicepackName,
            userId=userId,
            jobId=jobId,
            speed=speed,
            emotionIndex=emotionIndex
        )

        return JSONResponse(
            status_code=200,
            content={
                "status": "completed",
                "message": "음성 합성이 완료되었습니다.",
                "jobId": jobId
            }
        )

    except Exception as e:
        logger.error(f"failed to synthesize: {str(e)}")
        raise HTTPException(status_code=500, detail="음성 합성 요청 처리 중 오류가 발생했습니다.")

@app.post("/assistant")
async def assistant_endpoint(
    prompt: str = Form(...),
    voicepackName: str = Form(...),
    jobId: int = Form(...),
    category: str = Form(...),
    writingStyle: str = Form(...),
    nowTime: str = Form(...),
    speed: float = Form(1.0)
):
    """AI 비서용 음성 합성 API 엔드포인트"""
    try:
        await process_assistant_request(
            prompt=prompt,
            voicepackName=voicepackName,
            jobId=jobId,
            speed=speed,
            category=category,
            writingStyle=writingStyle,
            nowTime=nowTime
        )

        return JSONResponse(
            status_code=200,
            content={
                "status": "completed",
                "message": "AI 비서 음성 합성이 완료되었습니다.",
                "jobId": jobId
            }
        )

    except Exception as e:
        logger.error(f"failed to synthesize: {str(e)}")
        raise HTTPException(status_code=500, detail="AI 비서 음성 합성 요청 처리 중 오류가 발생했습니다.")

@app.get("/health")
def health_check():
    """서비스 상태 확인"""
    return {"status": "healthy"} 

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8080)