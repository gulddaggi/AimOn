# 1단계: 의존성 설치 및 앱 빌드
FROM node:18 AS builder

# 1. 루트에서 모노레포 전체 복사 및 설치
WORKDIR /app
COPY . .
RUN yarn install

# 2. 프론트엔드 앱 빌드
WORKDIR /app/apps/frontend
RUN yarn build

# 2단계: 경량 이미지에서 SSR standalone 실행
FROM node:18-alpine AS runner
WORKDIR /app

# Standalone 빌드 결과물만 복사
COPY --from=builder /app/apps/frontend/.next/standalone ./
COPY --from=builder /app/apps/frontend/.next/static ./apps/frontend/.next/static
COPY --from=builder /app/apps/frontend/public ./apps/frontend/public
COPY --from=builder /app/apps/frontend/package.json ./apps/frontend/package.json

EXPOSE 3000

# Next.js standalone SSR 실행
CMD ["node", "apps/frontend/server.js"]
