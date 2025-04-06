FROM python:3.10-slim

WORKDIR /app

# 필수 시스템 패키지 설치
RUN apt-get update && apt-get install -y git git-lfs gcc g++ && rm -rf /var/lib/apt/lists/*

# 파이썬 패키지 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 모델 다운로드 및 관련 설정
RUN mkdir -p pretrained_models
RUN git lfs install
RUN git clone https://www.modelscope.cn/iic/CosyVoice2-0.5B.git pretrained_models/CosyVoice2-0.5B
RUN git clone https://www.modelscope.cn/iic/CosyVoice-ttsfrd.git pretrained_models/CosyVoice-ttsfrd

WORKDIR /app/pretrained_models/CosyVoice-ttsfrd
RUN pip install --no-cache-dir ttsfrd_dependency-0.1-py3-none-any.whl
RUN pip install --no-cache-dir ttsfrd-0.4.2-cp310-cp310-linux_x86_64.whl
WORKDIR /app

# 애플리케이션 코드 복사
COPY . .

# Cloud Run은 8080 포트 사용
EXPOSE 8080

WORKDIR /app

ENV UVICORN_BUFFER_SIZE=16777216
ENV TZ=Asia/Seoul      

# FastAPI 서버 실행
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8080"] 
