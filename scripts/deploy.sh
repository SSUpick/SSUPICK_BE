#!/bin/bash
set -e

BLUE_PORT=8080
GREEN_PORT=8081
NGINX_CONF="/etc/nginx/sites-available/default"
IMAGE="pooreumjung/ssupick-backend:latest"
ENV_FILE="/home/ubuntu/app/.env.prod"

echo "=== Blue-Green Deploy Start ==="

# 0. 배포 전 디스크 용량 확인
echo "=== 디스크 상태 ==="
df -h /
docker system df

# 1. Docker 네트워크 생성 (없으면)
docker network create ssupick-net 2>/dev/null || true

# 2. Redis 실행 여부 확인
if ! docker ps | grep -q ssupick-redis; then
  echo "Redis 컨테이너가 실행 중이 아닙니다. Redis를 먼저 실행합니다."
  docker run -d \
    --name ssupick-redis \
    --network ssupick-net \
    --restart unless-stopped \
    redis:7-alpine
fi

# 3. 현재 nginx가 바라보는 포트 확인
CURRENT_PORT=$(grep -oP 'set \$service_port \K[0-9]+' $NGINX_CONF | head -n 1)
if [ -z "$CURRENT_PORT" ]; then
  CURRENT_PORT=$BLUE_PORT
fi

if [ "$CURRENT_PORT" = "$BLUE_PORT" ]; then
  NEW_PORT=$GREEN_PORT
else
  NEW_PORT=$BLUE_PORT
fi

echo "현재 포트: $CURRENT_PORT"
echo "새 포트: $NEW_PORT"

# 4. 이전 이미지 ID 저장 (pull 전)
OLD_IMAGE_ID=$(docker inspect --format='{{.Id}}' $IMAGE 2>/dev/null || true)

# 5. 최신 이미지 pull
echo "이미지 pull 중..."
docker pull $IMAGE

# 6. 기존 NEW_PORT 컨테이너 제거
docker rm -f ssupick-backend-${NEW_PORT} 2>/dev/null || true

# 7. 새 컨테이너 실행
echo "새 컨테이너 실행 (${NEW_PORT})"
docker run -d \
  --name ssupick-backend-${NEW_PORT} \
  --network ssupick-net \
  -p ${NEW_PORT}:8080 \
  --env-file ${ENV_FILE} \
  --memory="600m" \
  --restart unless-stopped \
  $IMAGE

# 8. 헬스체크 (최대 120초)
echo "헬스체크 확인 중..."
SUCCESS=false
for i in {1..24}
do
  if curl -fsS --connect-timeout 3 --max-time 5 http://localhost:${NEW_PORT}/actuator/health > /dev/null 2>&1; then
    SUCCESS=true
    echo "헬스체크 성공"
    break
  fi
  echo "헬스체크 재시도 ($i/24)"
  sleep 5
done

if [ "$SUCCESS" != "true" ]; then
  echo "헬스체크 최종 실패 → 롤백"
  docker logs --tail 50 ssupick-backend-${NEW_PORT}
  docker stop ssupick-backend-${NEW_PORT}
  docker rm ssupick-backend-${NEW_PORT}
  exit 1
fi

# 9. nginx 포트 전환
echo "nginx 전환 중..."
sudo sed -i "s/set \$service_port .*/set \$service_port $NEW_PORT;/" $NGINX_CONF
sudo nginx -t
sudo systemctl reload nginx
echo "nginx 전환 완료"

# 10. 기존 컨테이너 종료
echo "기존 컨테이너 종료 (${CURRENT_PORT})"
docker stop ssupick-backend-${CURRENT_PORT} 2>/dev/null || true
docker rm ssupick-backend-${CURRENT_PORT} 2>/dev/null || true

# 11. 이전 이미지 삭제 + 전체 미사용 리소스 정리
echo "Docker 리소스 정리 중..."
if [ -n "$OLD_IMAGE_ID" ]; then
  NEW_IMAGE_ID=$(docker inspect --format='{{.Id}}' $IMAGE 2>/dev/null || true)
  if [ "$OLD_IMAGE_ID" != "$NEW_IMAGE_ID" ]; then
    docker rmi $OLD_IMAGE_ID 2>/dev/null || true
    echo "이전 이미지 삭제 완료"
  fi
fi
docker system prune -f

echo "=== 배포 후 디스크 상태 ==="
df -h /
docker system df

echo "=== 배포 완료 (포트: $NEW_PORT) ==="
