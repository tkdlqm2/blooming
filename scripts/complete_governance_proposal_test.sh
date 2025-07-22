#!/bin/bash

# ========================================
# 완전한 거버넌스 제안 생성 테스트 스크립트
# ========================================
# 이 스크립트는 다음 단계를 자동화합니다:
# 1. 새 사용자 생성
# 2. 이더리움 지갑 생성
# 3. 무료 포인트 지급
# 4. Exchange를 통한 토큰 획득
# 5. 거버넌스 제안 생성
# ========================================

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 로그 함수들
log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

log_tx() {
    echo -e "${PURPLE}🔗 $1${NC}"
}

# 설정
BASE_URL="http://localhost:8081"
API_BASE="$BASE_URL/api"

# 랜덤 사용자 정보 생성
TIMESTAMP=$(date +%s)
USERNAME="testuser_$TIMESTAMP"
EMAIL="test_$TIMESTAMP@example.com"
PASSWORD="password123"

# 변수 초기화
USER_ID=""
USER_TOKEN=""
WALLET_ADDRESS=""
EXCHANGE_REQUEST_ID=""
PROPOSAL_ID=""

echo "=========================================="
echo "🚀 완전한 거버넌스 제안 생성 테스트 시작"
echo "=========================================="
echo "  사용자: $USERNAME"
echo "  이메일: $EMAIL"
echo "  시간: $(date)"
echo "=========================================="

# 1. 사용자 생성
log_info "1️⃣ 사용자 생성 중..."
log_info "  사용자명: $USERNAME"
log_info "  이메일: $EMAIL"

SIGNUP_RESPONSE=$(curl -s -X POST "$API_BASE/users/signup" \
    -H "Content-Type: application/json" \
    -d "{
        \"username\": \"$USERNAME\",
        \"email\": \"$EMAIL\",
        \"password\": \"$PASSWORD\"
    }")

if echo "$SIGNUP_RESPONSE" | grep -q '"token"'; then
    USER_TOKEN=$(echo "$SIGNUP_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    USER_ID=$(echo "$SIGNUP_RESPONSE" | grep -o '"userId":"[^"]*"' | cut -d'"' -f4)
    log_success "🎉 사용자 생성 성공!"
    log_info "  사용자 ID: $USER_ID"
    log_info "  토큰: ${USER_TOKEN:0:20}..."
else
    log_error "사용자 생성 실패: $SIGNUP_RESPONSE"
    exit 1
fi

# 2. 이더리움 지갑 생성
log_info "2️⃣ 이더리움 지갑 생성 중..."

WALLET_RESPONSE=$(curl -s -X POST "$API_BASE/wallets" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -d "{
        \"userId\": \"$USER_ID\",
        \"networkType\": \"ETHEREUM\"
    }")

if echo "$WALLET_RESPONSE" | grep -q '"walletAddress"'; then
    WALLET_ADDRESS=$(echo "$WALLET_RESPONSE" | grep -o '"walletAddress":"[^"]*"' | cut -d'"' -f4)
    log_success "🎉 이더리움 지갑 생성 성공!"
    log_info "  지갑 주소: $WALLET_ADDRESS"
else
    log_error "지갑 생성 실패: $WALLET_RESPONSE"
    exit 1
fi

# 3. 무료 포인트 지급
log_info "3️⃣ 무료 포인트 지급 중..."

POINT_RESPONSE=$(curl -s -X POST "$API_BASE/points/receive-free" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -d '{"amount":1000.00}')

if echo "$POINT_RESPONSE" | grep -q '"newBalance"'; then
    POINT_BALANCE=$(echo "$POINT_RESPONSE" | grep -o '"newBalance":[0-9.]*' | cut -d':' -f2)
    log_success "🎉 무료 포인트 지급 성공!"
    log_info "  포인트 잔액: $POINT_BALANCE"
else
    log_error "포인트 지급 실패: $POINT_RESPONSE"
    exit 1
fi

# 4. Exchange 요청 생성
log_info "4️⃣ Exchange 요청 생성 중..."

EXCHANGE_RESPONSE=$(curl -s -X POST "$API_BASE/exchange/request" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $USER_TOKEN" \
    -d "{
        \"pointAmount\": 500.00,
        \"walletAddress\": \"$WALLET_ADDRESS\"
    }")

if echo "$EXCHANGE_RESPONSE" | grep -q '"exchangeRequestId"'; then
    EXCHANGE_REQUEST_ID=$(echo "$EXCHANGE_RESPONSE" | grep -o '"exchangeRequestId":"[^"]*"' | cut -d'"' -f4)
    log_success "🎉 Exchange 요청 생성 성공!"
    log_info "  Exchange ID: $EXCHANGE_REQUEST_ID"
else
    log_error "Exchange 요청 생성 실패: $EXCHANGE_RESPONSE"
    exit 1
fi

# 5. Exchange 처리
log_info "5️⃣ Exchange 처리 중..."

PROCESS_RESPONSE=$(curl -s -X POST "$API_BASE/exchange/$EXCHANGE_REQUEST_ID/process" \
    -H "Authorization: Bearer $USER_TOKEN")

if echo "$PROCESS_RESPONSE" | grep -q "교환이 처리되었습니다"; then
    log_success "🎉 Exchange 처리 시작 성공!"
else
    log_error "Exchange 처리 실패: $PROCESS_RESPONSE"
    exit 1
fi

# 6. Exchange 완료 대기 및 트랜잭션 해시 확인
log_info "6️⃣ Exchange 완료 대기 중..."

local max_attempts=10
local attempt=1
local tx_hash=""

while [[ $attempt -le $max_attempts ]]; do
    log_info "Exchange 상태 확인 시도 $attempt/$max_attempts"

    local status_response=$(curl -s -X GET "$API_BASE/exchange/$EXCHANGE_REQUEST_ID" \
        -H "Authorization: Bearer $USER_TOKEN")

    if echo "$status_response" | grep -q '"status"'; then
        local status=$(echo "$status_response" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
        tx_hash=$(echo "$status_response" | grep -o '"transactionSignature":"[^"]*"' | cut -d'"' -f4)

        if [[ "$status" == "COMPLETED" ]]; then
            log_success "🎉 Exchange가 성공적으로 완료되었습니다!"
            if [[ -z "$tx_hash" || "$tx_hash" == "null" ]]; then
                log_error "Exchange 트랜잭션 해시가 없습니다. 토큰 지급이 실패한 것으로 간주합니다."
                log_info "  Exchange ID: $EXCHANGE_REQUEST_ID"
                log_info "  상태: $status"
                log_info "  transactionSignature: $tx_hash"
                exit 1
            else
                log_info "  트랜잭션 해시: $tx_hash"
                break
            fi
        elif [[ "$status" == "FAILED" ]]; then
            log_error "Exchange가 실패했습니다. 거버넌스 제안을 생성할 수 없습니다."
            exit 1
        elif [[ "$status" == "PROCESSING" ]]; then
            log_info "Exchange가 처리 중입니다. 3초 후 다시 확인합니다..."
            sleep 3
        else
            log_info "Exchange 상태: $status. 3초 후 다시 확인합니다..."
            sleep 3
        fi
    else
        log_error "Exchange 상태 조회 실패"
        exit 1
    fi

    ((attempt++))
done

if [[ $attempt -gt $max_attempts ]]; then
    log_error "Exchange 완료 대기 시간 초과. 거버넌스 제안을 생성할 수 없습니다."
    exit 1
fi

# 7. 거버넌스 제안 생성
log_info "7️⃣ 거버넌스 제안 생성 중..."

# 제안 정보 설정
PROPOSAL_TITLE="자동 생성된 거버넌스 제안"
PROPOSAL_DESCRIPTION="이 제안은 자동화된 테스트 스크립트에 의해 생성되었습니다. Exchange 트랜잭션 해시: $tx_hash"
VOTING_START_DATE=$(date -v+1d +%Y-%m-%dT%H:%M:%S)
VOTING_END_DATE=$(date -v+7d +%Y-%m-%dT%H:%M:%S)
REQUIRED_QUORUM=100
PROPOSAL_FEE=0.00

# 거버넌스 제안 생성 API 호출
PROPOSAL_RESPONSE=$(curl -s -X POST "$API_BASE/governance/proposals" \
    -H "Content-Type: application/json" \
    -d "{
        \"creatorId\": \"$USER_ID\",
        \"title\": \"$PROPOSAL_TITLE\",
        \"description\": \"$PROPOSAL_DESCRIPTION\",
        \"votingStartDate\": \"$VOTING_START_DATE\",
        \"votingEndDate\": \"$VOTING_END_DATE\",
        \"requiredQuorum\": $REQUIRED_QUORUM,
        \"creatorWalletAddress\": \"$WALLET_ADDRESS\",
        \"proposalFee\": $PROPOSAL_FEE,
        \"networkType\": \"ETHEREUM\"
    }")

# 제안 ID 추출
PROPOSAL_ID=$(echo "$PROPOSAL_RESPONSE" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)

if [[ -n "$PROPOSAL_ID" ]]; then
    log_success "🎉 거버넌스 제안 생성 성공!"
    log_info "  제안 ID: $PROPOSAL_ID"
    log_info "  제안자: $USERNAME"
    log_info "  지갑 주소: $WALLET_ADDRESS"
    log_info "  관련 트랜잭션: $tx_hash"
    log_info "  투표 시작: $VOTING_START_DATE"
    log_info "  투표 종료: $VOTING_END_DATE"
    log_info "  필요 정족수: $REQUIRED_QUORUM"

    log_tx "✅ 완전한 거버넌스 제안 생성 플로우 완료!"
    log_tx "   사용자 생성 → 지갑 생성 → 포인트 지급 → Exchange → 거버넌스 제안"
    log_tx "   모든 단계가 성공적으로 완료되었습니다."

else
    log_error "거버넌스 제안 생성 실패: $PROPOSAL_RESPONSE"
    exit 1
fi

echo ""
echo "=========================================="
echo "🎯 테스트 결과 요약"
echo "=========================================="
echo "✅ 사용자 생성: $USERNAME ($USER_ID)"
echo "✅ 지갑 생성: $WALLET_ADDRESS"
echo "✅ 포인트 지급: $POINT_BALANCE 포인트"
echo "✅ Exchange 완료: $EXCHANGE_REQUEST_ID"
echo "✅ 트랜잭션 해시: $tx_hash"
echo "✅ 거버넌스 제안: $PROPOSAL_ID"
echo "=========================================="
echo "🎉 모든 단계가 성공적으로 완료되었습니다!"
echo "==========================================" 