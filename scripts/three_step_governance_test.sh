#!/bin/bash

# 3단계 거버넌스 제안 테스트 스크립트
# 1. 거버넌스 제안 저장
# 2. 수수료 충전
# 3. 블록체인 브로드캐스트

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 로그 함수들
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
BASE_URL="http://localhost:8080"
API_BASE="$BASE_URL/api"

echo "=========================================="
echo "🚀 3단계 거버넌스 제안 테스트 시작"
echo "=========================================="

# 1단계: 사용자 생성 및 로그인
log_info "1️⃣ 사용자 생성 및 로그인 중..."

USERNAME="test_user_$(date +%s)"
EMAIL="${USERNAME}@example.com"
PASSWORD="password123"

# 사용자 가입
SIGNUP_RESPONSE=$(curl -s -X POST "$API_BASE/users/signup" \
    -H "Content-Type: application/json" \
    -d "{
        \"username\": \"$USERNAME\",
        \"email\": \"$EMAIL\",
        \"password\": \"$PASSWORD\"
    }")

USER_ID=$(echo "$SIGNUP_RESPONSE" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)

if [ -z "$USER_ID" ]; then
    log_error "사용자 생성 실패: $SIGNUP_RESPONSE"
    exit 1
fi

log_success "사용자 생성 성공: $USERNAME ($USER_ID)"

# 로그인
LOGIN_RESPONSE=$(curl -s -X POST "$API_BASE/users/login" \
    -H "Content-Type: application/json" \
    -d "{
        \"username\": \"$USERNAME\",
        \"password\": \"$PASSWORD\"
    }")

TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    log_error "로그인 실패: $LOGIN_RESPONSE"
    exit 1
fi

log_success "로그인 성공"

# 2단계: 무료 포인트 받기
log_info "2️⃣ 무료 포인트 받는 중..."

POINT_RESPONSE=$(curl -s -X POST "$API_BASE/points/receive-free" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"userId\": \"$USER_ID\",
        \"amount\": 100.00
    }")

if echo "$POINT_RESPONSE" | grep -q "포인트가 성공적으로 지급되었습니다"; then
    log_success "무료 포인트 지급 성공"
else
    log_error "무료 포인트 지급 실패: $POINT_RESPONSE"
    exit 1
fi

# 3단계: 이더리움 지갑 생성
log_info "3️⃣ 이더리움 지갑 생성 중..."

WALLET_RESPONSE=$(curl -s -X POST "$API_BASE/wallets" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"userId\": \"$USER_ID\",
        \"networkType\": \"ETHEREUM\"
    }")

WALLET_ADDRESS=$(echo "$WALLET_RESPONSE" | grep -o '"walletAddress":"[^"]*"' | cut -d'"' -f4)

if [ -z "$WALLET_ADDRESS" ]; then
    log_error "지갑 생성 실패: $WALLET_RESPONSE"
    exit 1
fi

log_success "이더리움 지갑 생성 성공: $WALLET_ADDRESS"

# 4단계: Exchange 요청 생성
log_info "4️⃣ Exchange 요청 생성 중..."

EXCHANGE_RESPONSE=$(curl -s -X POST "$API_BASE/exchange/request" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"userId\": \"$USER_ID\",
        \"pointAmount\": 50.00,
        \"walletAddress\": \"$WALLET_ADDRESS\",
        \"networkType\": \"ETHEREUM\"
    }")

EXCHANGE_ID=$(echo "$EXCHANGE_RESPONSE" | grep -o '"exchangeRequestId":"[^"]*"' | cut -d'"' -f4)

if [ -z "$EXCHANGE_ID" ]; then
    log_error "Exchange 요청 생성 실패: $EXCHANGE_RESPONSE"
    exit 1
fi

log_success "Exchange 요청 생성 성공: $EXCHANGE_ID"

# 5단계: Exchange 처리
log_info "5️⃣ Exchange 처리 중..."

PROCESS_RESPONSE=$(curl -s -X POST "$API_BASE/exchange/$EXCHANGE_ID/process" \
    -H "Content-Type: application/json")

if echo "$PROCESS_RESPONSE" | grep -q "교환이 처리되었습니다"; then
    log_success "Exchange 처리 성공!"
else
    log_error "Exchange 처리 실패: $PROCESS_RESPONSE"
    exit 1
fi

# 6단계: Exchange 완료 대기 및 txHash 확인
log_info "6️⃣ Exchange 완료 대기 중... (최대 30초)"
max_attempts=10
attempt=1
tx_hash=""

while [[ $attempt -le $max_attempts ]]; do
    log_info "Exchange 상태 확인 시도 $attempt/$max_attempts"
    
    status_response=$(curl -s -X GET "$API_BASE/exchange/$EXCHANGE_ID" \
        -H "Authorization: Bearer $TOKEN")
    
    if echo "$status_response" | grep -q '"status"'; then
        status=$(echo "$status_response" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
        tx_hash=$(echo "$status_response" | grep -o '"transactionSignature":"[^"]*"' | sed 's/.*:"\([^"]*\)"/\1/')
        
        if [[ "$status" == "COMPLETED" ]]; then
            log_success "🎉 Exchange가 성공적으로 완료되었습니다!"
            if [[ -n "$tx_hash" && "$tx_hash" != "null" ]]; then
                log_info "  트랜잭션 해시: $tx_hash"
                break
            else
                log_warning "Exchange 트랜잭션 해시가 없습니다."
                break
            fi
        elif [[ "$status" == "FAILED" ]]; then
            log_error "Exchange가 실패했습니다."
            exit 1
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
    log_error "Exchange 완료 대기 시간 초과."
    exit 1
fi

# 7단계: 1단계 - 거버넌스 제안 저장
log_info "7️⃣ 1단계: 거버넌스 제안 저장 중..."

PROPOSAL_TITLE="3단계 테스트 거버넌스 제안"
PROPOSAL_DESCRIPTION="이 제안은 3단계 거버넌스 제안 프로세스를 테스트하기 위해 생성되었습니다."
VOTING_START_DATE=$(date -v+1d +%Y-%m-%dT%H:%M:%S)
VOTING_END_DATE=$(date -v+7d +%Y-%m-%dT%H:%M:%S)
REQUIRED_QUORUM=100
PROPOSAL_FEE=10.00

SAVE_PROPOSAL_RESPONSE=$(curl -s -X POST "$API_BASE/governance/proposals/save" \
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

PROPOSAL_ID=$(echo "$SAVE_PROPOSAL_RESPONSE" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)

if [ -n "$PROPOSAL_ID" ]; then
    log_success "🎉 1단계: 거버넌스 제안 저장 성공!"
    log_info "  제안 ID: $PROPOSAL_ID"
    log_info "  제안 제목: $PROPOSAL_TITLE"
    log_info "  제안자: $USERNAME"
    log_info "  지갑 주소: $WALLET_ADDRESS"
else
    log_error "1단계: 거버넌스 제안 저장 실패: $SAVE_PROPOSAL_RESPONSE"
    exit 1
fi

# 8단계: 2단계 - 수수료 충전
log_info "8️⃣ 2단계: 수수료 충전 중..."

CHARGE_FEE_RESPONSE=$(curl -s -X POST "$API_BASE/governance/proposals/$PROPOSAL_ID/charge-fee" \
    -H "Content-Type: application/json" \
    -d "{
        \"creatorWalletAddress\": \"$WALLET_ADDRESS\",
        \"proposalFee\": $PROPOSAL_FEE,
        \"networkType\": \"ETHEREUM\"
    }")

FEE_SUCCESS=$(echo "$CHARGE_FEE_RESPONSE" | grep -o '"success":true')

if [ -n "$FEE_SUCCESS" ]; then
    FEE_TX_HASH=$(echo "$CHARGE_FEE_RESPONSE" | grep -o '"feeTransactionHash":"[^"]*"' | cut -d'"' -f4)
    log_success "🎉 2단계: 수수료 충전 성공!"
    log_info "  수수료 트랜잭션 해시: $FEE_TX_HASH"
    log_info "  충전된 수수료: $PROPOSAL_FEE"
else
    log_error "2단계: 수수료 충전 실패: $CHARGE_FEE_RESPONSE"
    exit 1
fi

# 9단계: 3단계 - 블록체인 브로드캐스트
log_info "9️⃣ 3단계: 블록체인 브로드캐스트 중..."

BROADCAST_RESPONSE=$(curl -s -X POST "$API_BASE/governance/proposals/$PROPOSAL_ID/broadcast" \
    -H "Content-Type: application/json" \
    -d "{
        \"creatorWalletAddress\": \"$WALLET_ADDRESS\",
        \"proposalFee\": $PROPOSAL_FEE,
        \"networkType\": \"ETHEREUM\"
    }")

BROADCAST_SUCCESS=$(echo "$BROADCAST_RESPONSE" | grep -o '"success":true')

if [ -n "$BROADCAST_SUCCESS" ]; then
    GOVERNANCE_TX_HASH=$(echo "$BROADCAST_RESPONSE" | grep -o '"governanceTransactionHash":"[^"]*"' | cut -d'"' -f4)
    log_success "🎉 3단계: 블록체인 브로드캐스트 성공!"
    log_info "  거버넌스 트랜잭션 해시: $GOVERNANCE_TX_HASH"
    log_info "  사용된 수수료: $PROPOSAL_FEE"
else
    log_error "3단계: 블록체인 브로드캐스트 실패: $BROADCAST_RESPONSE"
    exit 1
fi

echo ""
echo "=========================================="
echo "🎯 3단계 거버넌스 제안 테스트 결과 요약"
echo "=========================================="
echo "✅ 사용자 생성: $USERNAME ($USER_ID)"
echo "✅ 지갑 생성: $WALLET_ADDRESS"
echo "✅ Exchange 완료: $tx_hash"
echo "✅ 1단계 - 제안 저장: $PROPOSAL_ID"
echo "✅ 2단계 - 수수료 충전: $FEE_TX_HASH"
echo "✅ 3단계 - 블록체인 브로드캐스트: $GOVERNANCE_TX_HASH"
echo ""
echo "🎊 3단계 거버넌스 제안 프로세스 완료!"
echo "🚀 이제 블록체인에서 실제 거버넌스 제안이 활성화되었습니다!" 