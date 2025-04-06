from fastapi import FastAPI, UploadFile, File, Form, HTTPException, BackgroundTasks
from utils.voice_synthesizer import VoiceSynthesizer
from utils.voice_registration_handler import process_voice_registration
import logging
from fastapi.responses import JSONResponse
from utils import voice_synthesizer

logging.basicConfig(level=logging.INFO)
logging.getLogger('cosyvoice').setLevel(logging.WARNING)

logger = logging.getLogger(__name__)

app = FastAPI()
logging.info("Starting server... ----------------------------------------------------------------------------")
voice_synthesizer = VoiceSynthesizer()

@app.post("/register_speaker")
async def register_speaker_endpoint(
    background_tasks: BackgroundTasks,
    voicepackId: str = Form(...),
    voiceFile: UploadFile = File(...),
    voicepackRequestId: int = Form(...)
):
    """화자 등록 API 엔드포인트"""
    try:
        # 파일 데이터 읽기
        file_content = await voiceFile.read()
        
        # 백그라운드로 처리 시작
        background_tasks.add_task(
            process_voice_registration,
            voicepackId=voicepackId,
            file_content=file_content,
            voicepackRequestId=voicepackRequestId
        )
        
        return JSONResponse(
            status_code=202,
            content={
                "status": "processing",
                "message": "화자 등록이 시작되었습니다.",
                "voicepackRequestId": voicepackRequestId
            }
        )
        
    except Exception as e:
        logger.error(f"failed to register speaker: {str(e)}")
        raise HTTPException(status_code=500, detail="화자 등록 요청 처리 중 오류가 발생했습니다.")

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