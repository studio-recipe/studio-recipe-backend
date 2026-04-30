#!/bin/bash

echo "Spring Boot 애플리케이션 빌드 시작..."
# Gradle 프로젝트인 경우:
./gradlew bootJar

if [ $? -ne 0 ]; then
  echo "애플리케이션 빌드 실패. 스크립트를 종료합니다."
  exit 1
fi
echo "Spring Boot 애플리케이션 빌드 완료."

# .env 파일이 존재하는지 확인하고 로드
if [ -f .env ]; then
  echo "'.env' 파일 로드 중..."
  # .env 파일을 읽어서 환경 변수로 설정
  export $(grep -v '^#' .env | xargs)
else
  echo "경고: '.env' 파일이 존재하지 않습니다. 환경 변수가 설정되지 않을 수 있습니다."
  echo "민감 정보를 포함한 '.env' 파일을 docker-compose.yml과 같은 위치에 생성하세요."

fi

# Docker Compose 빌드 및 실행
echo "Docker Compose 서비스 빌드 및 실행 시작..."
# --build: Dockerfile이 변경되거나 이미지가 없으면 재빌드
# -d: 백그라운드에서 실행
docker compose up --build -d

if [ $? -ne 0 ]; then
  echo "Docker Compose 실행 실패. 스크립트를 종료합니다."
  exit 1
fi
echo "Docker Compose 서비스가 백그라운드에서 실행 중입니다."
echo "Spring Boot 앱은 호스트의 8080 포트, MariaDB는 3306 포트로 접근 가능합니다."
echo "로그 확인: docker compose logs -f"
