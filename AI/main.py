from fastapi import FastAPI, UploadFile, File, Form, HTTPException, BackgroundTasks
from utils.voice_synthesizer import VoiceSynthesizer
from utils.callback_handler import send_callback
import logging
from fastapi.responses import JSONResponse

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()
voice_synthesizer = VoiceSynthesizer()


async def process_voice_registration(
    voicepackId: str,
    file_content: bytes,
    voicepackRequestId: int
):
    """비동기로 화자 등록 처리 및 콜백 전송"""
    logger.info(f"화자 등록 처리 시작: voicepackId={voicepackId}, voicepackRequestId={voicepackRequestId}")
    
    try:
        # 화자 특징 추출
        logger.info(f"화자 특징 추출 시작: voicepackId={voicepackId}")
        features_result = await voice_synthesizer.extract_speaker_features(
            voicepackId=voicepackId,
            file_content=file_content
        )
        
        if not features_result:
            logger.error(f"화자 특징 추출 실패: voicepackId={voicepackId}")
            raise Exception("화자 특징 추출 실패")
        
        logger.info(f"화자 특징 추출 성공: voicepackId={voicepackId}")
        
        # 성공 콜백
        await send_callback(
            voicepackId=voicepackId,
            voicepackRequestId=voicepackRequestId,
            status="success"
        )

        logger.info(f"화자 등록 성공: voicepackId={voicepackId}, voicepackRequestId={voicepackRequestId}")

    except Exception as e:
        logger.error(f"화자 등록 실패: {str(e)}", exc_info=True)
        # 실패 콜백
        try:
            await send_callback(
                voicepackId=voicepackId,
                voicepackRequestId=voicepackRequestId,
                status="failed"
            )
        except Exception as callback_error:
            logger.error(f"콜백 전송 실패: {str(callback_error)}", exc_info=True)

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
        logger.error(f"요청 처리 실패: {str(e)}")
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