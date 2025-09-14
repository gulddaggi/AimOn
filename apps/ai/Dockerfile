FROM python:3.11-slim

# 기본 런타임
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1 \
    PORT=8082 \
    # 실제 Django WSGI 엔트리모듈
    APP_MODULE=chatbot.wsgi:application

WORKDIR /app

# 헬스체크용 curl만 설치하여 이미지 경량화
RUN apt-get update && apt-get install -y --no-install-recommends \
      curl \
    && rm -rf /var/lib/apt/lists/*

# 의존성 설치 (pip 캐시 미사용)
COPY requirements.txt ./
RUN pip install --upgrade pip && pip install -r requirements.txt

# 비루트 사용자
RUN useradd -m appuser
USER appuser

# 앱 소스 복사
COPY . .

# Django 프로젝트 루트로 작업 디렉토리 이동
WORKDIR /app/server

EXPOSE 8082

# gunicorn으로 서비스 기동
# - ASGI(Channels 등) 사용 시 .env.dev에 ASGI=1 주고 uvicorn worker로 전환
CMD ["sh", "-c", "\
  gunicorn ${APP_MODULE} \
  --bind 0.0.0.0:${PORT} \
  --workers ${GUNICORN_WORKERS:-2} \
  --timeout ${GUNICORN_TIMEOUT:-60} \
  ${ASGI:+-k uvicorn.workers.UvicornWorker} \
 "]
