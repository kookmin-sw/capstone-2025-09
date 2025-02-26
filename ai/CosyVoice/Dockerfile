FROM pytorch/pytorch:2.0.1-cuda11.7-cudnn8-runtime

WORKDIR /app

# 필수 패키지 설치 (git, gcc, g++)
RUN apt-get update && apt-get install -y git gcc g++ && apt-get clean

# 파이썬 패키지 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 애플리케이션 코드 복사
COPY . .

# Cloud Run은 8080 포트 사용
EXPOSE 8080

# FastAPI 서버 실행
CMD ["uvicorn", "tts_service:app", "--host", "0.0.0.0", "--port", "8080"] 