#!/bin/bash

# 간단한 투표 스크립트 - 기존 API들을 순차적으로 호출
# 사용법: ./simple_vote.sh <제안_ID>

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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
ADMIN_USER_ID="f2aec616-1dcb-4e56-923d-16e07a58ae3c"
ADMIN_WALLET_ADDRESS="0x55D5c49e36f8A89111687C9DC8355121068f0cD8"

# 제안 ID 확인
if [ $# -eq 0 ]; then
    log_error "제안 ID를 입력해주세요."
    log_info "사용법: ./simple_vote.sh <제안_ID>"
    exit 1
fi

PROPOSAL_ID="$1"

log_info "🚀 간단한 투표 시작"
log_info "제안 ID: $PROPOSAL_ID"

# 1단계: 제안 상태 확인
log_info "1️⃣ 제안 상태 확인 중..."
PROPOSAL_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/governance/proposals/${PROPOSAL_ID}")
PROPOSAL_STATUS=$(echo "$PROPOSAL_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
log_info "현재 제안 상태: $PROPOSAL_STATUS"

# 2단계: DRAFT -> ACTIVE
if [ "$PROPOSAL_STATUS" = "DRAFT" ]; then
    log_info "2️⃣ 제안 활성화 중..."
    ACTIVATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/governance/proposals/${PROPOSAL_ID}/activate")
    log_info "활성화 응답: $ACTIVATE_RESPONSE"
    
    if echo "$ACTIVATE_RESPONSE" | grep -q '"success":true'; then
        log_success "제안이 활성화되었습니다!"
        PROPOSAL_STATUS="ACTIVE"
    else
        log_error "제안 활성화 실패!"
        exit 1
    fi
fi

# 3단계: 투표 기간 수정 (현재 시간으로)
if [ "$PROPOSAL_STATUS" = "ACTIVE" ]; then
    log_info "3️⃣ 투표 기간 수정 중..."
    UPDATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/governance/proposals/${PROPOSAL_ID}/update-voting-period")
    log_info "투표 기간 수정 응답: $UPDATE_RESPONSE"
    
    if echo "$UPDATE_RESPONSE" | grep -q '"success":true'; then
        log_success "투표 기간이 수정되었습니다!"
    else
        log_error "투표 기간 수정 실패!"
        exit 1
    fi
fi

# 4단계: 투표 시작
if [ "$PROPOSAL_STATUS" = "ACTIVE" ]; then
    log_info "4️⃣ 투표 시작 중..."
    START_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/governance/proposals/${PROPOSAL_ID}/start-voting")
    log_info "투표 시작 응답: $START_RESPONSE"
    
    if echo "$START_RESPONSE" | grep -q '"success":true'; then
        log_success "투표가 시작되었습니다!"
        PROPOSAL_STATUS="VOTING"
    else
        log_error "투표 시작 실패!"
        exit 1
    fi
fi

# 5단계: 투표 실행
if [ "$PROPOSAL_STATUS" = "VOTING" ]; then
    log_info "5️⃣ 투표 실행 중..."
    
    # VoteType.YES 사용 (올바른 enum 값)
    VOTE_PAYLOAD=$(cat <<EOF
{
    "voterId": "$ADMIN_USER_ID",
    "voteType": "YES",
    "reason": "Admin 계정으로 찬성 투표합니다.",
    "voterWalletAddress": "$ADMIN_WALLET_ADDRESS",
    "networkType": "ETHEREUM"
}
EOF
)
    
    log_info "투표 페이로드: $VOTE_PAYLOAD"
    
    VOTE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/governance/proposals/${PROPOSAL_ID}/vote-blockchain" \
        -H "Content-Type: application/json" \
        -d "$VOTE_PAYLOAD")
    
    log_info "투표 응답: $VOTE_RESPONSE"
    
    if echo "$VOTE_RESPONSE" | grep -q '"success":true'; then
        log_success "투표가 성공적으로 완료되었습니다!"
    else
        log_error "투표 실패!"
        exit 1
    fi
else
    log_error "제안이 투표 가능한 상태가 아닙니다: $PROPOSAL_STATUS"
    exit 1
fi

log_success "✅ 모든 과정이 완료되었습니다!" 