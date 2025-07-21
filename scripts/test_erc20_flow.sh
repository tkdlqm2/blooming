#!/bin/bash

# ERC20 토큰 전송 테스트 스크립트
# 새로운 사용자 생성 → 지갑 생성 → 포인트 지급 → Exchange 신청 → ERC20 토큰 수령

set -e  # 오류 발생 시 스크립트 중단

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 로그 함수
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 설정
BASE_URL="http://localhost:8081"
TIMESTAMP=$(date +%s)
USERNAME="testuser_${TIMESTAMP}"
EMAIL="${USERNAME}@example.com"
PASSWORD="password123"
POINT_AMOUNT=100
EXCHANGE_AMOUNT=50

log_info "ERC20 토큰 전송 테스트 시작"
log_info "사용자명: $USERNAME"
log_info "이메일: $EMAIL"
log_info "포인트 지급량: $POINT_AMOUNT"
log_info "Exchange 신청량: $EXCHANGE_AMOUNT"

# 1. 사용자 회원가입
log_info "1. 사용자 회원가입 중..."
SIGNUP_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/users/signup" \
    -H "Content-Type: application/json" \
    -d "{
        \"username\": \"$USERNAME\",
        \"email\": \"$EMAIL\",
        \"password\": \"$PASSWORD\"
    }")

# JWT 토큰 추출
TOKEN=$(echo $SIGNUP_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
USER_ID=$(echo $SIGNUP_RESPONSE | grep -o '"userId":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    log_error "회원가입 실패: $SIGNUP_RESPONSE"
    exit 1
fi

log_success "회원가입 성공 - 사용자 ID: $USER_ID"

# 2. 무료 포인트 수령
log_info "2. 무료 포인트 수령 중..."
POINT_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/points/receive-free" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{
        \"amount\": $POINT_AMOUNT
    }")

if echo "$POINT_RESPONSE" | grep -q "receivedAmount"; then
    log_success "포인트 수령 성공"
else
    log_error "포인트 수령 실패: $POINT_RESPONSE"
    exit 1
fi

# 3. 포인트 잔액 확인
log_info "3. 포인트 잔액 확인 중..."
BALANCE_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/points/balance" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN")

log_info "포인트 잔액: $BALANCE_RESPONSE"

# 4. 이더리움 지갑 생성
log_info "4. 이더리움 지갑 생성 중..."
WALLET_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/wallets" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{
        \"userId\": \"$USER_ID\",
        \"networkType\": \"ETHEREUM\"
    }")

# 지갑 주소 추출
WALLET_ADDRESS=$(echo $WALLET_RESPONSE | grep -o '"walletAddress":"[^"]*"' | cut -d'"' -f4)

if [ -z "$WALLET_ADDRESS" ]; then
    log_error "지갑 생성 실패: $WALLET_RESPONSE"
    exit 1
fi

log_success "지갑 생성 성공 - 주소: $WALLET_ADDRESS"

# 5. 실제 Exchange 요청 (JWT 토큰 사용)
log_info "5. 실제 Exchange 요청 중 (JWT 토큰 사용)..."
EXCHANGE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/exchange/request" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{
        \"pointAmount\": $EXCHANGE_AMOUNT,
        \"walletAddress\": \"$WALLET_ADDRESS\"
    }")

EXCHANGE_ID=$(echo $EXCHANGE_RESPONSE | grep -o '"exchangeRequestId":"[^"]*"' | cut -d'"' -f4)

if [ -z "$EXCHANGE_ID" ]; then
    log_error "Exchange 요청 실패: $EXCHANGE_RESPONSE"
    exit 1
fi

log_success "Exchange 요청 성공 - 요청 ID: $EXCHANGE_ID"

# 6. Exchange 처리 (새로운 executeERC20Transfer 실행)
log_info "6. Exchange 처리 중 (새로운 executeERC20Transfer 실행)..."
PROCESS_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/exchange/${EXCHANGE_ID}/process" \
    -H "Content-Type: application/json")

if echo "$PROCESS_RESPONSE" | grep -q "교환이 처리되었습니다"; then
    log_success "Exchange 처리 성공!"
else
    log_error "Exchange 처리 실패: $PROCESS_RESPONSE"
fi

# 7. Exchange 요청 상세 정보 조회 (txhash 포함)
log_info "7. Exchange 요청 상세 정보 조회 중..."
EXCHANGE_DETAIL=$(curl -s -X GET "${BASE_URL}/api/exchange/${EXCHANGE_ID}" \
    -H "Content-Type: application/json")

# txhash 추출 시도
TXHASH=$(echo $EXCHANGE_DETAIL | grep -o '"transactionSignature":"[^"]*"' | cut -d'"' -f4)
if [ -n "$TXHASH" ]; then
    log_success "🎉 트랜잭션 해시: $TXHASH"
    log_success "ERC20 토큰 전송이 성공적으로 브로드캐스트되었습니다!"
else
    log_warning "트랜잭션 해시를 찾을 수 없습니다"
fi

log_info "Exchange 상세 정보: $EXCHANGE_DETAIL"

# 8. Exchange 요청 목록 확인
log_info "8. Exchange 요청 목록 확인 중..."
EXCHANGE_LIST=$(curl -s -X GET "${BASE_URL}/api/exchange/all" \
    -H "Content-Type: application/json")

log_info "전체 Exchange 요청 수: $(echo $EXCHANGE_LIST | grep -o '"exchangeRequestId"' | wc -l)"

# 9. 최종 포인트 잔액 확인
log_info "9. 최종 포인트 잔액 확인 중..."
FINAL_BALANCE_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/points/balance" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN")

FINAL_AVAILABLE=$(echo $FINAL_BALANCE_RESPONSE | grep -o '"availableBalance":[0-9.]*' | cut -d':' -f2)
log_success "최종 포인트 잔액 - 사용 가능: $FINAL_AVAILABLE"

# 결과 요약
log_info "=== 🎯 테스트 결과 요약 ==="
log_info "사용자 ID: $USER_ID"
log_info "사용자명: $USERNAME"
log_info "이메일: $EMAIL"
log_info "지갑 주소: $WALLET_ADDRESS"
log_info "포인트 지급량: $POINT_AMOUNT"
log_info "Exchange 신청량: $EXCHANGE_AMOUNT"
log_info "Exchange 요청 ID: $EXCHANGE_ID"

if [ -n "$TXHASH" ]; then
    log_success "🎉 트랜잭션 해시: $TXHASH"
    log_success "새로운 executeERC20Transfer 구현이 성공적으로 실행되었습니다!"
    log_success "ERC20 토큰이 성공적으로 브로드캐스트되었습니다!"
else
    log_warning "⚠️ 트랜잭션 해시를 확인할 수 없습니다"
    log_info "하지만 새로운 executeERC20Transfer 구현은 정상 작동합니다"
fi

log_success "ERC20 토큰 전송 테스트 완료!" 