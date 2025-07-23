#!/bin/bash

# 어드민 계정으로 찬성 투표를 실행하는 스크립트
# 사용법: ./admin_vote_for.sh <제안_ID>

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

log_debug() {
    echo -e "${PURPLE}[DEBUG]${NC} $1"
}

# 설정
BASE_URL="http://localhost:8081"
ADMIN_USER_ID="f2aec616-1dcb-4e56-923d-16e07a58ae3c"
ADMIN_WALLET_ADDRESS="0x55D5c49e36f8A89111687C9DC8355121068f0cD8"
NETWORK_TYPE="ETHEREUM"

# 제안 ID 확인
if [ $# -eq 0 ]; then
    log_error "제안 ID를 입력해주세요."
    log_info "사용법: ./admin_vote_for.sh <제안_ID>"
    exit 1
fi

PROPOSAL_ID="$1"

log_info "️ 어드민 찬성 투표 시작"
log_info "제안 ID: $PROPOSAL_ID"
log_info "어드민 ID: $ADMIN_USER_ID"
log_info "어드민 지갑: $ADMIN_WALLET_ADDRESS"

# ===== 1단계: 제안 존재 확인 =====
log_info "1️⃣ 제안 존재 확인 중..."

PROPOSAL_CHECK_CURL="curl -s -X GET \"${BASE_URL}/api/governance/proposals/${PROPOSAL_ID}\""
log_debug "실행되는 curl 명령어:"
echo "$PROPOSAL_CHECK_CURL"

PROPOSAL_RESPONSE=$(eval $PROPOSAL_CHECK_CURL)

if echo "$PROPOSAL_RESPONSE" | grep -q "error"; then
    log_error "제안을 찾을 수 없습니다!"
    log_error "제안 응답: $PROPOSAL_RESPONSE"
    exit 1
fi

log_success "제안이 존재합니다!"

# ===== 2단계: 제안 상태 확인 =====
log_info "2️⃣ 제안 상태 확인 중..."

PROPOSAL_STATUS=$(echo "$PROPOSAL_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
log_info "현재 제안 상태: $PROPOSAL_STATUS"

# ===== 3단계: 제안 상태에 따른 처리 =====
log_info "3️⃣ 제안 상태에 따른 처리 중..."

if [ "$PROPOSAL_STATUS" = "DRAFT" ]; then
    log_info "제안이 DRAFT 상태입니다. 활성화를 진행합니다..."
    
    ACTIVATE_CURL="curl -s -X POST \"${BASE_URL}/api/governance/proposals/${PROPOSAL_ID}/activate\""
    log_debug "실행되는 curl 명령어:"
    echo "$ACTIVATE_CURL"
    
    ACTIVATE_RESPONSE=$(eval $ACTIVATE_CURL)
    log_info "활성화 응답: $ACTIVATE_RESPONSE"
    
    # 응답 확인 로직 개선
    if echo "$ACTIVATE_RESPONSE" | grep -q '"success":true'; then
        log_success "제안이 성공적으로 활성화되었습니다!"
        PROPOSAL_STATUS="ACTIVE"
    else
        log_error "제안 활성화에 실패했습니다!"
        log_error "활성화 응답: $ACTIVATE_RESPONSE"
        exit 1
    fi
    
    # 활성화 후 상태 재확인
    log_info "활성화 후 제안 상태 재확인 중..."
    PROPOSAL_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/governance/proposals/${PROPOSAL_ID}")
    PROPOSAL_STATUS=$(echo "$PROPOSAL_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
    log_info "활성화 후 제안 상태: $PROPOSAL_STATUS"
fi

if [ "$PROPOSAL_STATUS" = "ACTIVE" ]; then
    log_info "제안이 ACTIVE 상태입니다. 투표 시작을 진행합니다..."
    
    # 투표 시작일을 현재 시간으로 수정
    log_info "투표 시작일을 현재 시간으로 수정 중..."
    
    START_VOTING_CURL="curl -s -X POST \"${BASE_URL}/api/governance/proposals/${PROPOSAL_ID}/start-voting\""
    log_debug "실행되는 curl 명령어:"
    echo "$START_VOTING_CURL"
    
    START_VOTING_RESPONSE=$(eval $START_VOTING_CURL)
    log_info "투표 시작 응답: $START_VOTING_RESPONSE"
    
    # 응답 확인 로직 개선
    if echo "$START_VOTING_RESPONSE" | grep -q '"success":true'; then
        log_success "제안 투표가 성공적으로 시작되었습니다!"
        PROPOSAL_STATUS="VOTING"
    else
        log_error "제안 투표 시작에 실패했습니다!"
        log_error "투표 시작 응답: $START_VOTING_RESPONSE"
        exit 1
    fi
    
    # 투표 시작 후 상태 재확인
    log_info "투표 시작 후 제안 상태 재확인 중..."
    PROPOSAL_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/governance/proposals/${PROPOSAL_ID}")
    PROPOSAL_STATUS=$(echo "$PROPOSAL_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
    log_info "투표 시작 후 제안 상태: $PROPOSAL_STATUS"
fi

if [ "$PROPOSAL_STATUS" = "VOTING" ]; then
    log_success "제안이 투표 가능한 상태입니다: $PROPOSAL_STATUS"
else
    log_error "제안이 투표 가능한 상태가 아닙니다: $PROPOSAL_STATUS"
    exit 1
fi

# ===== 4단계: 투표 가능 여부 확인 =====
log_info "4️⃣ 투표 가능 여부 확인 중..."

CAN_VOTE_CURL="curl -s -X GET \"${BASE_URL}/api/governance/proposals/${PROPOSAL_ID}/can-vote/${ADMIN_USER_ID}?voterWalletAddress=${ADMIN_WALLET_ADDRESS}&networkType=${NETWORK_TYPE}\""
log_debug "실행되는 curl 명령어:"
echo "$CAN_VOTE_CURL"

CAN_VOTE_RESPONSE=$(eval $CAN_VOTE_CURL)
log_info "투표 가능 여부 응답: $CAN_VOTE_RESPONSE"

CAN_VOTE=$(echo "$CAN_VOTE_RESPONSE" | grep -o '"canVote":[^,}]*' | cut -d':' -f2 | tr -d ' ')

if [ "$CAN_VOTE" = "true" ]; then
    log_success "투표할 수 있습니다!"
else
    log_warning "투표할 수 없습니다!"
    log_warning "투표 가능 여부 응답:"
    echo "$CAN_VOTE_RESPONSE"
    exit 1
fi

# ===== 5단계: 투표 파워 확인 =====
log_info "5️⃣ 투표 파워 확인 중..."

VOTING_POWER_CURL="curl -s -X GET \"${BASE_URL}/api/governance/voting-power/${ADMIN_USER_ID}?voterWalletAddress=${ADMIN_WALLET_ADDRESS}&networkType=${NETWORK_TYPE}\""
log_debug "실행되는 curl 명령어:"
echo "$VOTING_POWER_CURL"

VOTING_POWER_RESPONSE=$(eval $VOTING_POWER_CURL)
log_info "투표 파워 응답: $VOTING_POWER_RESPONSE"

VOTING_POWER=$(echo "$VOTING_POWER_RESPONSE" | grep -o '"votingPower":[^,}]*' | cut -d':' -f2 | tr -d ' ')

log_info "투표 파워: $VOTING_POWER"

if [ "$VOTING_POWER" != "0" ] && [ "$VOTING_POWER" != "0.000000000000000000" ]; then
    log_success "충분한 투표 파워가 있습니다!"
else
    log_error "투표 파워가 부족합니다!"
    exit 1
fi

# ===== 6단계: 찬성 투표 실행 =====
log_info "6️⃣ 찬성 투표 실행 중..."

VOTE_PAYLOAD=$(cat <<EOF
{
    "voterId": "$ADMIN_USER_ID",
    "voteType": "FOR",
    "reason": "Admin 계정으로 찬성 투표합니다. 이 제안이 플랫폼 발전에 도움이 될 것으로 판단됩니다.",
    "voterWalletAddress": "$ADMIN_WALLET_ADDRESS",
    "networkType": "ETHEREUM"
}
EOF
)

VOTE_CURL="curl -s -X POST \"${BASE_URL}/api/governance/proposals/${PROPOSAL_ID}/vote-blockchain\" -H \"Content-Type: application/json\" -H \"Accept: application/json\" -d '$VOTE_PAYLOAD'"
log_debug "실행되는 curl 명령어:"
echo "$VOTE_CURL"

log_info "요청 페이로드:"
echo "$VOTE_PAYLOAD"

VOTE_RESPONSE=$(eval $VOTE_CURL)
log_info "투표 응답: $VOTE_RESPONSE"

if echo "$VOTE_RESPONSE" | grep -q '"success":true'; then
    log_success "투표가 성공적으로 완료되었습니다!"
    
    # 투표 결과 추출
    TRANSACTION_HASH=$(echo "$VOTE_RESPONSE" | grep -o '"transactionHash":"[^"]*"' | cut -d'"' -f4)
    if [ -n "$TRANSACTION_HASH" ]; then
        log_info "투표 트랜잭션 해시: $TRANSACTION_HASH"
    fi
else
    log_error "투표가 실패했습니다!"
    log_error "투표 응답:"
    echo "$VOTE_RESPONSE"
    exit 1
fi

log_success "🗳️ 어드민 찬성 투표가 완료되었습니다!"