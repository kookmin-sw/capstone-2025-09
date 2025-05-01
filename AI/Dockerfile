FROM python:3.10-slim

# uv 설치
RUN pip install uv

RUN apt update && \
    apt install -y gcc g++ espeak-ng && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY . ./

RUN uv pip install --system -e . && uv pip install --system -e .[compile]
RUN uv pip install --upgrade openai

EXPOSE 8080
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8080"]
