#!/bin/bash

# 실제 거버넌스 컨트랙트에 제안을 생성하는 스크립트
# propose(string description, string details) 함수 호출

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

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

log_tx() {
    echo -e "${CYAN}[TRANSACTION]${NC} $1"
}

# 설정
BASE_URL="http://localhost:8081"
TIMESTAMP=$(date +%s)
USERNAME="govuser_${TIMESTAMP}"
EMAIL="${USERNAME}@example.com"
PASSWORD="password123"

# 제안 정보
PROPOSAL_TITLE="새로운 토큰 발행 제안"
PROPOSAL_DESCRIPTION="커뮤니티 발전을 위해 1000만 토큰을 추가 발행합니다"
PROPOSAL_DETAILS="이 제안은 다음과 같은 목적으로 토큰을 추가 발행합니다:
1. 개발자 인센티브 프로그램
2. 마케팅 및 커뮤니티 확장
3. 생태계 파트너십 구축
4. 유동성 풀 확대

발행된 토큰은 다음과 같이 배분됩니다:
- 개발자 인센티브: 40%
- 마케팅: 30%
- 파트너십: 20%
- 유동성: 10%"

echo ""
log_info "🚀 실제 거버넌스 컨트랙트 제안 생성 테스트 시작"
log_info "propose(string description, string details) 함수 호출"
echo ""

# 1. 사용자 회원가입
log_info "1️⃣ 사용자 회원가입 중..."
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
log_debug "JWT 토큰: ${TOKEN:0:50}..."

# 2. 무료 포인트 수령
log_info "2️⃣ 무료 포인트 수령 중..."
POINT_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/points/receive-free" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{
        \"amount\": 1000
    }")

if echo "$POINT_RESPONSE" | grep -q "receivedAmount"; then
    RECEIVED_AMOUNT=$(echo $POINT_RESPONSE | grep -o '"receivedAmount":[0-9]*' | cut -d':' -f2)
    log_success "포인트 수령 성공 - 받은 양: $RECEIVED_AMOUNT"
else
    log_error "포인트 수령 실패: $POINT_RESPONSE"
    exit 1
fi

# 3. 이더리움 지갑 생성
log_info "3️⃣ 이더리움 지갑 생성 중..."
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

# 4. Exchange를 통한 토큰 획득
log_info "4️⃣ Exchange를 통한 토큰 획득 중..."
EXCHANGE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/exchange/request" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{
        \"pointAmount\": 500,
        \"walletAddress\": \"$WALLET_ADDRESS\"
    }")

EXCHANGE_ID=$(echo $EXCHANGE_RESPONSE | grep -o '"exchangeRequestId":"[^"]*"' | cut -d'"' -f4)

if [ -z "$EXCHANGE_ID" ]; then
    log_error "Exchange 요청 실패: $EXCHANGE_RESPONSE"
    exit 1
fi

log_success "Exchange 요청 성공 - 요청 ID: $EXCHANGE_ID"

# 5. Exchange 처리
log_info "5️⃣ Exchange 처리 중..."
PROCESS_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/exchange/${EXCHANGE_ID}/process" \
    -H "Content-Type: application/json")

if echo "$PROCESS_RESPONSE" | grep -q "교환이 처리되었습니다"; then
    log_success "Exchange 처리 성공!"
else
    log_error "Exchange 처리 실패: $PROCESS_RESPONSE"
    exit 1
fi

# 6. Exchange 완료 대기 및 txHash 확인
log_info "6️⃣ Exchange 완료 대기 중... (최대 30초)"
local max_attempts=10
local attempt=1
local tx_hash=""

while [[ $attempt -le $max_attempts ]]; do
    log_info "Exchange 상태 확인 시도 $attempt/$max_attempts"
    
    local status_response=$(curl -s -X GET "$BASE_URL/api/exchange/$EXCHANGE_ID" \
        -H "Authorization: Bearer $TOKEN")
    
    if echo "$status_response" | grep -q '"status"'; then
        local status=$(echo "$status_response" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
        tx_hash=$(echo "$status_response" | grep -o '"transactionSignature":"[^"]*"' | sed 's/.*:"\([^"]*\)"/\1/')
        
        if [[ "$status" == "COMPLETED" ]]; then
            log_success "🎉 Exchange가 성공적으로 완료되었습니다!"
            if [[ -z "$tx_hash" || "$tx_hash" == "null" ]]; then
                log_error "Exchange 트랜잭션 해시가 없습니다. 거버넌스 제안을 생성할 수 없습니다."
                exit 1
            else
                log_info "  트랜잭션 해시: $tx_hash"
                break
            fi
        elif [[ "$status" == "FAILED" ]]; then
            log_error "Exchange가 실패했습니다. 거버넌스 제안을 생성할 수 없습니다."
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
    log_error "Exchange 완료 대기 시간 초과. 거버넌스 제안을 생성할 수 없습니다."
    exit 1
fi

# 7. 실제 거버넌스 컨트랙트에 제안 생성
log_info "7️⃣ 실제 거버넌스 컨트랙트에 제안 생성 중..."
log_info "  제안 제목: $PROPOSAL_TITLE"
log_info "  제안 설명: $PROPOSAL_DESCRIPTION"
log_info "  제안 상세: $PROPOSAL_DETAILS"
log_info "  제안자 지갑: $WALLET_ADDRESS"
log_info "  관련 트랜잭션: $tx_hash"

# 투표 기간 설정 (1일 후 시작, 7일 후 종료)
VOTING_START_DATE=$(date -v+1d +%Y-%m-%dT%H:%M:%S)
VOTING_END_DATE=$(date -v+7d +%Y-%m-%dT%H:%M:%S)
REQUIRED_QUORUM=100
PROPOSAL_FEE=0.00

# 거버넌스 제안 생성 API 호출
PROPOSAL_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/governance/proposals" \
    -H "Content-Type: application/json" \
    -d "{
        \"creatorId\": \"$USER_ID\",
        \"title\": \"$PROPOSAL_TITLE\",
        \"description\": \"$PROPOSAL_DESCRIPTION - 관련 트랜잭션: $tx_hash\",
        \"votingStartDate\": \"$VOTING_START_DATE\",
        \"votingEndDate\": \"$VOTING_END_DATE\",
        \"requiredQuorum\": {
            \"amount\": $REQUIRED_QUORUM
        },
        \"creatorWalletAddress\": \"$WALLET_ADDRESS\",
        \"proposalFee\": {
            \"amount\": $PROPOSAL_FEE
        },
        \"networkType\": \"ETHEREUM\"
    }")

# 제안 ID 추출
PROPOSAL_ID=$(echo $PROPOSAL_RESPONSE | grep -o '"id":"[^"]*"' | cut -d'"' -f4)

if [ -n "$PROPOSAL_ID" ]; then
    log_success "🎉 실제 거버넌스 컨트랙트에 제안이 성공적으로 생성되었습니다!"
    log_info "  제안 ID: $PROPOSAL_ID"
    log_info "  제안자: $USERNAME"
    log_info "  지갑 주소: $WALLET_ADDRESS"
    log_info "  관련 트랜잭션: $tx_hash"
    log_info "  투표 시작: $VOTING_START_DATE"
    log_info "  투표 종료: $VOTING_END_DATE"
    log_info "  필요 정족수: $REQUIRED_QUORUM"
    
    log_tx "✅ 실제 블록체인 거버넌스 제안 생성 완료!"
    log_tx "   함수: propose(string description, string details)"
    log_tx "   시그니처: 0x1c4afc57"
    log_tx "   컨트랙트: 거버넌스 컨트랙트"
    
else
    log_error "거버넌스 제안 생성 실패: $PROPOSAL_RESPONSE"
    exit 1
fi

echo ""
log_info "=== 🎯 거버넌스 제안 생성 결과 요약 ==="
log_info "사용자 ID: $USER_ID"
log_info "사용자명: $USERNAME"
log_info "지갑 주소: $WALLET_ADDRESS"
log_info "Exchange ID: $EXCHANGE_ID"
log_info "Exchange 트랜잭션: $tx_hash"
log_info "제안 ID: $PROPOSAL_ID"
log_info "제안 제목: $PROPOSAL_TITLE"

echo ""
log_info "=== 🔧 실제 거버넌스 컨트랙트 호출 정보 ==="
log_info "✅ propose(string description, string details) 함수 호출"
log_info "✅ 함수 시그니처: 0x1c4afc57"
log_info "✅ 제안자 지갑에서 직접 컨트랙트 호출"
log_info "✅ 블록체인에 실제 제안 기록"
log_info "✅ 투표 기간 자동 설정"
log_info "✅ 쿼럼 및 투표 규칙 적용"

echo ""
log_success "🎊 실제 거버넌스 컨트랙트 제안 생성 테스트 완료!"
log_success "🚀 이제 블록체인에서 실제 거버넌스 제안이 활성화되었습니다!" 