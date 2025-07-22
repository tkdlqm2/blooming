#!/bin/bash

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

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 설정
BASE_URL="http://localhost:8081"
API_BASE="$BASE_URL/api"

# 사용법
if [[ $# -ne 3 ]]; then
    echo "사용법: $0 <userId> <walletAddress> <amount>"
    echo "예시: $0 838d2b51-6784-438c-99ca-24a63f6fe1a4 0x944ca271d69eb0cb19ac91bf1d92a5552efbace6 100.00"
    exit 1
fi

USER_ID="$1"
WALLET_ADDRESS="$2"
AMOUNT="$3"

log_info "토큰 추가 시작"
log_info "  사용자 ID: $USER_ID"
log_info "  지갑 주소: $WALLET_ADDRESS"
log_info "  추가할 토큰: $AMOUNT"

# 1. 토큰 계정 조회
log_info "1️⃣ 토큰 계정 조회"
TOKEN_ACCOUNT_RESPONSE=$(curl -s -X GET "$API_BASE/token-accounts/wallet/$WALLET_ADDRESS")

if [[ $TOKEN_ACCOUNT_RESPONSE == *"[]"* ]]; then
    log_error "토큰 계정이 존재하지 않습니다."
    exit 1
fi

log_success "토큰 계정 조회 성공"
log_info "  응답: $TOKEN_ACCOUNT_RESPONSE"

# 2. 토큰 민팅 API 호출
log_info "2️⃣ 토큰 민팅 API 호출"
MINT_RESPONSE=$(curl -s -X POST "$API_BASE/tokens/mint" \
    -H "Content-Type: application/json" \
    -d "{
        \"userId\": \"$USER_ID\",
        \"walletAddress\": \"$WALLET_ADDRESS\",
        \"amount\": $AMOUNT,
        \"description\": \"테스트용 토큰 추가\",
        \"networkType\": \"ETHEREUM\"
    }")

log_info "민팅 응답: $MINT_RESPONSE"

if [[ $MINT_RESPONSE == *"successfully"* ]]; then
    log_success "토큰 민팅 성공!"
else
    log_error "토큰 민팅 실패"
    log_info "  응답: $MINT_RESPONSE"
    exit 1
fi

# 3. 토큰 계정 재조회
log_info "3️⃣ 토큰 계정 재조회"
UPDATED_ACCOUNT_RESPONSE=$(curl -s -X GET "$API_BASE/token-accounts/wallet/$WALLET_ADDRESS")

log_success "토큰 추가 완료!"
log_info "  업데이트된 계정: $UPDATED_ACCOUNT_RESPONSE" 