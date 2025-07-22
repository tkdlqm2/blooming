#!/bin/bash

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
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

log_step() {
    echo -e "${PURPLE}[STEP]${NC} $1"
}

# 설정
BASE_URL="http://localhost:8081"
API_BASE="$BASE_URL/api"

# 전역 변수
USER_ID=""
USER_TOKEN=""
WALLET_ADDRESS=""
PROPOSAL_ID=""

# 에러 처리
handle_error() {
    log_error "스크립트 실행 중 오류가 발생했습니다."
    log_error "마지막 명령어: $BASH_COMMAND"
    exit 1
}

# 트랩 설정
trap handle_error ERR

# 헬퍼 함수들
check_response() {
    local response="$1"
    local expected_status="$2"
    
    if [[ $response == *"$expected_status"* ]]; then
        return 0
    else
        return 1
    fi
}

extract_json_value() {
    local json="$1"
    local key="$2"
    if command -v jq &> /dev/null; then
        echo "$json" | jq -r ".$key" 2>/dev/null
    else
        echo "$json" | grep -o "\"$key\":\"[^\"]*\"" | cut -d'"' -f4 | head -1
    fi
}

extract_json_value_number() {
    local json="$1"
    local key="$2"
    if command -v jq &> /dev/null; then
        echo "$json" | jq -r ".$key" 2>/dev/null
    else
        echo "$json" | grep -o "\"$key\":[0-9]*" | cut -d':' -f2
    fi
}

# 메인 함수들
create_user() {
    log_step "1️⃣ 새로운 사용자 회원가입"
    
    local email="governance_user_$(date +%s)@test.com"
    local username="governance_user_$(date +%s)"
    local password="test1234!"
    
    log_info "회원가입 정보:"
    log_info "  이메일: $email"
    log_info "  사용자명: $username"
    log_info "  비밀번호: $password"
    
    local signup_response=$(curl -s -X POST "$API_BASE/users/signup" \
        -H "Content-Type: application/json" \
        -d "{
            \"email\": \"$email\",
            \"username\": \"$username\",
            \"password\": \"$password\",
            \"role\": \"USER\"
        }")
    
    log_info "회원가입 응답: $signup_response"
    
    if check_response "$signup_response" "token"; then
        # 간단한 문자열 파싱으로 수정
        USER_TOKEN=$(echo "$signup_response" | sed 's/.*"token":"\([^"]*\)".*/\1/')
        USER_ID=$(echo "$signup_response" | sed 's/.*"userId":"\([^"]*\)".*/\1/')
        
        log_success "회원가입 성공!"
        log_info "  사용자 ID: $USER_ID"
        log_info "  토큰: ${USER_TOKEN:0:20}..."
    else
        log_error "회원가입 실패"
        exit 1
    fi
}

create_wallet() {
    log_step "2️⃣ 이더리움 지갑 생성"
    
    local wallet_response=$(curl -s -X POST "$API_BASE/wallets" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $USER_TOKEN" \
        -d "{
            \"userId\": \"$USER_ID\",
            \"networkType\": \"ETHEREUM\"
        }")
    
    log_info "지갑 생성 응답: $wallet_response"
    
    if check_response "$wallet_response" "walletAddress"; then
        WALLET_ADDRESS=$(extract_json_value "$wallet_response" "walletAddress")
        
        log_success "지갑 생성 성공!"
        log_info "  지갑 주소: $WALLET_ADDRESS"
    else
        log_error "지갑 생성 실패"
        exit 1
    fi
}

receive_free_points() {
    log_step "3️⃣ 무료 포인트 수령"
    
    local points_amount="1000.00"
    
    log_info "포인트 수령 요청: $points_amount 포인트"
    
    local points_response=$(curl -s -X POST "$API_BASE/points/receive-free" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $USER_TOKEN" \
        -d "{
            \"amount\": $points_amount
        }")
    
    log_info "포인트 수령 응답: $points_response"
    
    if check_response "$points_response" "newBalance"; then
        local total_balance=$(echo "$points_response" | sed 's/.*"newBalance":\([0-9.]*\).*/\1/')
        
        log_success "포인트 수령 성공!"
        log_info "  수령한 포인트: $points_amount"
        log_info "  총 잔액: $total_balance"
    else
        log_error "포인트 수령 실패"
        exit 1
    fi
}

check_point_balance() {
    log_step "4️⃣ 포인트 잔액 확인"
    
    local balance_response=$(curl -s -X GET "$API_BASE/points/balance" \
        -H "Authorization: Bearer $USER_TOKEN")
    
    log_info "잔액 조회 응답: $balance_response"
    
    if check_response "$balance_response" "totalBalance"; then
        local available_balance=$(extract_json_value "$balance_response" "availableBalance")
        local frozen_balance=$(extract_json_value "$balance_response" "frozenBalance")
        local total_balance=$(extract_json_value "$balance_response" "totalBalance")
        
        log_success "잔액 조회 성공!"
        log_info "  사용 가능한 포인트: $available_balance"
        log_info "  동결된 포인트: $frozen_balance"
        log_info "  총 포인트: $total_balance"
    else
        log_error "잔액 조회 실패"
        exit 1
    fi
}

create_governance_proposal_for_user() {
    local USER_ID="$1"
    local WALLET_ADDRESS="$2"
    local USER_TOKEN="$3"

    # macOS 호환 날짜 계산
    local voting_start_date=$(date -v+1H -u +%Y-%m-%dT%H:%M:%S.000Z)
    local voting_end_date=$(date -v+7d -u +%Y-%m-%dT%H:%M:%S.000Z)
    local required_quorum="100.00"
    local proposal_fee="50.00"
    local title="ERC20 수신자 자동 제안 $(date +%Y%m%d_%H%M%S)"
    local description="ERC20 토큰을 수신한 사용자가 자동으로 생성한 거버넌스 제안입니다."

    log_step "[ERC20] 거버넌스 제안 생성 (userId=$USER_ID, wallet=$WALLET_ADDRESS)"
    log_info "  제목: $title"
    log_info "  설명: $description"
    log_info "  투표 시작: $voting_start_date"
    log_info "  투표 종료: $voting_end_date"
    log_info "  필요 정족수: $required_quorum"
    log_info "  제안 수수료: $proposal_fee"
    log_info "  지갑 주소: $WALLET_ADDRESS"

    local proposal_response=$(curl -s -X POST "$API_BASE/governance/proposals" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $USER_TOKEN" \
        -d "{\n            \"creatorId\": \"$USER_ID\",\n            \"title\": \"$title\",\n            \"description\": \"$description\",\n            \"votingStartDate\": \"$voting_start_date\",\n            \"votingEndDate\": \"$voting_end_date\",\n            \"requiredQuorum\": {\n                \"amount\": $required_quorum\n            },\n            \"creatorWalletAddress\": \"$WALLET_ADDRESS\",\n            \"proposalFee\": {\n                \"amount\": $proposal_fee\n            },\n            \"networkType\": \"ETHEREUM\"\n        }")

    log_info "제안 생성 응답: $proposal_response"
    if check_response "$proposal_response" "id"; then
        local PROPOSAL_ID=$(extract_json_value "$proposal_response" "id")
        log_success "[ERC20] 제안 생성 성공! 제안 ID: $PROPOSAL_ID"
    else
        log_error "[ERC20] 제안 생성 실패"
        return 1
    fi
}

get_proposal_details() {
    log_step "6️⃣ 제안 상세 정보 조회"
    
    if [[ -z "$PROPOSAL_ID" ]]; then
        log_error "제안 ID가 없습니다."
        return 1
    fi
    
    local proposal_response=$(curl -s -X GET "$API_BASE/governance/proposals/$PROPOSAL_ID" \
        -H "Authorization: Bearer $USER_TOKEN")
    
    log_info "제안 상세 조회 응답: $proposal_response"
    
    if check_response "$proposal_response" "id"; then
        local title=$(extract_json_value "$proposal_response" "title")
        local status=$(extract_json_value "$proposal_response" "status")
        local creator_id=$(extract_json_value "$proposal_response" "creatorId")
        
        log_success "제안 상세 조회 성공!"
        log_info "  제안 ID: $PROPOSAL_ID"
        log_info "  제목: $title"
        log_info "  상태: $status"
        log_info "  제안자 ID: $creator_id"
    else
        log_error "제안 상세 조회 실패"
        exit 1
    fi
}

get_proposals_by_status() {
    log_step "7️⃣ 활성 제안 목록 조회"
    
    local proposals_response=$(curl -s -X GET "$API_BASE/governance/proposals/status/ACTIVE" \
        -H "Authorization: Bearer $USER_TOKEN")
    
    log_info "활성 제안 목록 응답: $proposals_response"
    
    if check_response "$proposals_response" "id"; then
        log_success "활성 제안 목록 조회 성공!"
        log_info "활성 제안이 존재합니다."
    else
        log_warning "활성 제안이 없습니다."
    fi
}

get_proposals_by_creator() {
    log_step "8️⃣ 내가 생성한 제안 목록 조회"
    
    local proposals_response=$(curl -s -X GET "$API_BASE/governance/proposals/creator/$USER_ID" \
        -H "Authorization: Bearer $USER_TOKEN")
    
    log_info "내 제안 목록 응답: $proposals_response"
    
    if check_response "$proposals_response" "id"; then
        log_success "내 제안 목록 조회 성공!"
        log_info "내가 생성한 제안이 존재합니다."
    else
        log_warning "내가 생성한 제안이 없습니다."
    fi
}

vote_on_proposal() {
    log_step "9️⃣ 제안에 투표"
    
    if [[ -z "$PROPOSAL_ID" ]]; then
        log_error "제안 ID가 없습니다."
        return 1
    fi
    
    local vote_type="YES"
    local reason="이 제안은 블록체인 거버넌스 시스템의 발전에 도움이 될 것 같습니다."
    local voting_power="100.00"
    
    log_info "투표 정보:"
    log_info "  제안 ID: $PROPOSAL_ID"
    log_info "  투표 타입: $vote_type"
    log_info "  투표 권한: $voting_power"
    log_info "  사유: $reason"
    log_info "  투표자 지갑: $WALLET_ADDRESS"
    
    local vote_response=$(curl -s -X POST "$API_BASE/governance/proposals/$PROPOSAL_ID/vote" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $USER_TOKEN" \
        -d "{
            \"voterId\": \"$USER_ID\",
            \"voteType\": \"$vote_type\",
            \"reason\": \"$reason\",
            \"voterWalletAddress\": \"$WALLET_ADDRESS\",
            \"networkType\": \"ETHEREUM\"
        }")
    
    log_info "투표 응답: $vote_response"
    
    if check_response "$vote_response" "voteId"; then
        local vote_id=$(extract_json_value "$vote_response" "voteId")
        local vote_type_response=$(extract_json_value "$vote_response" "voteType")
        
        log_success "투표 성공!"
        log_info "  투표 ID: $vote_id"
        log_info "  투표 타입: $vote_type_response"
    else
        log_error "투표 실패"
        exit 1
    fi
}

get_votes_by_proposal() {
    log_step "🔟 제안에 대한 투표 목록 조회"
    
    if [[ -z "$PROPOSAL_ID" ]]; then
        log_error "제안 ID가 없습니다."
        return 1
    fi
    
    local votes_response=$(curl -s -X GET "$API_BASE/governance/votes/proposal/$PROPOSAL_ID" \
        -H "Authorization: Bearer $USER_TOKEN")
    
    log_info "투표 목록 응답: $votes_response"
    
    if check_response "$votes_response" "voteId"; then
        log_success "투표 목록 조회 성공!"
        log_info "이 제안에 대한 투표가 존재합니다."
    else
        log_warning "이 제안에 대한 투표가 없습니다."
    fi
}

get_votes_by_voter() {
    log_step "1️⃣1️⃣ 내 투표 목록 조회"
    
    local votes_response=$(curl -s -X GET "$API_BASE/governance/votes/voter/$USER_ID" \
        -H "Authorization: Bearer $USER_TOKEN")
    
    log_info "내 투표 목록 응답: $votes_response"
    
    if check_response "$votes_response" "voteId"; then
        log_success "내 투표 목록 조회 성공!"
        log_info "내가 참여한 투표가 존재합니다."
    else
        log_warning "내가 참여한 투표가 없습니다."
    fi
}

# 메인 실행 함수
main() {
    log_info "🚀 거버넌스 제안 테스트 시작"
    log_info "새로운 사용자가 거버넌스 안건을 제안하는 전체 과정을 테스트합니다."
    echo
    
    # 1. 사용자 생성
    create_user
    echo
    
    # 2. 지갑 생성
    create_wallet
    echo
    
    # 3. 포인트 수령
    receive_free_points
    echo
    
    # 4. 잔액 확인
    check_point_balance
    echo
    
    # 5. 제안 생성
    create_governance_proposal_for_user "$USER_ID" "$WALLET_ADDRESS" "$USER_TOKEN"
    echo
    
    # 6. 제안 상세 조회
    get_proposal_details
    echo
    
    # 7. 활성 제안 목록 조회
    get_proposals_by_status
    echo
    
    # 8. 내 제안 목록 조회
    get_proposals_by_creator
    echo
    
    # 9. 투표
    vote_on_proposal
    echo
    
    # 10. 제안에 대한 투표 목록 조회
    get_votes_by_proposal
    echo
    
    # 11. 내 투표 목록 조회
    get_votes_by_voter
    echo
    
    log_success "🎉 거버넌스 제안 테스트 완료!"
    log_info "테스트 요약:"
    log_info "  사용자 ID: $USER_ID"
    log_info "  지갑 주소: $WALLET_ADDRESS"
    log_info "  제안 ID: $PROPOSAL_ID"
    log_info "  모든 단계가 성공적으로 완료되었습니다."
}

# 스크립트 실행
main "$@" 