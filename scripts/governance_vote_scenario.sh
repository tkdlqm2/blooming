#!/bin/bash

# 거버넌스 투표 시나리오 스크립트
# 콘솔 입력을 받아서 사용자 수와 투표 비율을 설정하고 실행

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

log_delegate() {
    echo -e "${PURPLE}[DELEGATE]${NC} $1"
}

# 설정
BASE_URL="http://localhost:8081"
TIMESTAMP=$(date +%s)
POINT_AMOUNT=200000
EXCHANGE_AMOUNT=101000
PROPOSAL_FEE=10.00

# 콘솔 입력 받기
echo ""
log_info "🚀 거버넌스 투표 시나리오 시작"
echo ""

# 사용자 수 입력
read -p "생성할 사용자 수를 입력하세요 (10명 이하): " USER_COUNT

# 입력 검증
if ! [[ "$USER_COUNT" =~ ^[0-9]+$ ]] || [ "$USER_COUNT" -lt 1 ] || [ "$USER_COUNT" -gt 10 ]; then
    log_error "올바른 사용자 수를 입력해주세요 (1-10명)"
    exit 1
fi

echo ""
log_info "총 사용자 수: $USER_COUNT명"
echo ""

# 투표 비율 입력
read -p "찬성 투표 수를 입력하세요: " YES_VOTES
read -p "반대 투표 수를 입력하세요: " NO_VOTES
read -p "기권 투표 수를 입력하세요: " ABSTAIN_VOTES

# 투표 수 검증
TOTAL_VOTES=$((YES_VOTES + NO_VOTES + ABSTAIN_VOTES))
if [ "$TOTAL_VOTES" -gt "$USER_COUNT" ]; then
    log_error "투표 수의 합($TOTAL_VOTES)이 사용자 수($USER_COUNT)를 초과합니다"
    exit 1
fi

echo ""
log_info "투표 비율 - 찬성: $YES_VOTES, 반대: $NO_VOTES, 기권: $ABSTAIN_VOTES"
echo ""

# 거버넌스 안건 제안 입력
read -p "거버넌스 안건 제안을 입력하세요: " PROPOSAL_TITLE
read -p "거버넌스 안건 설명을 입력하세요: " PROPOSAL_DESCRIPTION

echo ""
log_info "거버넌스 안건: $PROPOSAL_TITLE"
log_info "설명: $PROPOSAL_DESCRIPTION"
echo ""

# 사용자 정보를 저장할 배열
declare -a USER_IDS
declare -a USER_TOKENS
declare -a USER_WALLETS

# 1. 새로운 user n명 생성
log_info "1️⃣ 새로운 사용자 $USER_COUNT명 생성 중..."

for i in $(seq 1 $USER_COUNT); do
    USERNAME="testuser_${TIMESTAMP}_${i}"
    EMAIL="${USERNAME}@example.com"
    PASSWORD="password123"
    
    log_info "사용자 $i 생성 중: $USERNAME"
    
    SIGNUP_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/users/signup" \
        -H "Content-Type: application/json" \
        -d "{
            \"username\": \"$USERNAME\",
            \"email\": \"$EMAIL\",
            \"password\": \"$PASSWORD\"
        }")
    
    TOKEN=$(echo $SIGNUP_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    USER_ID=$(echo $SIGNUP_RESPONSE | grep -o '"userId":"[^"]*"' | cut -d'"' -f4)
    
    if [ -z "$TOKEN" ]; then
        log_error "사용자 $i 회원가입 실패: $SIGNUP_RESPONSE"
        exit 1
    fi
    
    USER_IDS[$i]=$USER_ID
    USER_TOKENS[$i]=$TOKEN
    
    log_success "사용자 $i 생성 성공 - ID: $USER_ID"
done

echo ""

# 2. user1은 free Token 신청하고 exchange 요청
log_info "2️⃣ user1의 free Token 신청 및 exchange 요청 중..."

USER1_ID=${USER_IDS[1]}
USER1_TOKEN=${USER_TOKENS[1]}

# 무료 포인트 수령
log_info "user1 무료 포인트 수령 중..."
POINT_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/points/receive-free" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $USER1_TOKEN" \
    -d "{
        \"amount\": $POINT_AMOUNT
    }")

if echo "$POINT_RESPONSE" | grep -q "receivedAmount"; then
    RECEIVED_AMOUNT=$(echo $POINT_RESPONSE | grep -o '"receivedAmount":[0-9]*' | cut -d':' -f2)
    log_success "user1 포인트 수령 성공 - 받은 양: $RECEIVED_AMOUNT"
else
    log_error "user1 포인트 수령 실패: $POINT_RESPONSE"
    exit 1
fi

# 이더리움 지갑 생성
log_info "user1 이더리움 지갑 생성 중..."
WALLET_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/wallets" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $USER1_TOKEN" \
    -d "{
        \"userId\": \"$USER1_ID\",
        \"networkType\": \"ETHEREUM\"
    }")

WALLET_ADDRESS=$(echo $WALLET_RESPONSE | grep -o '"walletAddress":"[^"]*"' | cut -d'"' -f4)

if [ -z "$WALLET_ADDRESS" ]; then
    log_error "user1 지갑 생성 실패: $WALLET_RESPONSE"
    exit 1
fi

USER_WALLETS[1]=$WALLET_ADDRESS
log_success "user1 지갑 생성 성공 - 주소: $WALLET_ADDRESS"

# Exchange 요청
log_info "user1 Exchange 요청 중..."
EXCHANGE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/exchange/request" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $USER1_TOKEN" \
    -d "{
        \"pointAmount\": $EXCHANGE_AMOUNT,
        \"walletAddress\": \"$WALLET_ADDRESS\"
    }")

EXCHANGE_ID=$(echo $EXCHANGE_RESPONSE | grep -o '"exchangeRequestId":"[^"]*"' | cut -d'"' -f4)

if [ -z "$EXCHANGE_ID" ]; then
    log_error "user1 Exchange 요청 실패: $EXCHANGE_RESPONSE"
    exit 1
fi

log_success "user1 Exchange 요청 성공 - 요청 ID: $EXCHANGE_ID"

# Exchange 처리
log_info "user1 Exchange 처리 중..."
PROCESS_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/exchange/${EXCHANGE_ID}/process" \
    -H "Content-Type: application/json")

if echo "$PROCESS_RESPONSE" | grep -q "교환이 처리되었습니다"; then
    log_success "user1 Exchange 처리 성공!"
else
    log_error "user1 Exchange 처리 실패: $PROCESS_RESPONSE"
    exit 1
fi

# Exchange 상세 정보 조회
EXCHANGE_DETAIL=$(curl -s -X GET "${BASE_URL}/api/exchange/${EXCHANGE_ID}" \
    -H "Content-Type: application/json")

TXHASH=$(echo $EXCHANGE_DETAIL | grep -o '"transactionSignature":"[^"]*"' | cut -d'"' -f4)
if [ -n "$TXHASH" ] && [ "$TXHASH" != "null" ]; then
    log_tx "user1 ERC20 트랜잭션 해시: $TXHASH"
fi

# delegate 진행중 메시지와 20초 대기
log_delegate "delegate 진행중 ..."
sleep 20

echo ""

# 3. user1은 거버넌스 제안
log_info "3️⃣ user1의 거버넌스 제안 생성 중..."

# 제안 정보 설정
export TZ=America/New_York
VOTING_START_DATE=$(date +%Y-%m-%dT%H:%M:%S)
VOTING_END_DATE=$(date -v+7d +%Y-%m-%dT%H:%M:%S)
REQUIRED_QUORUM=100

log_info "제안 정보:"
log_info "  제목: $PROPOSAL_TITLE"
log_info "  설명: $PROPOSAL_DESCRIPTION"
log_info "  투표 시작: $VOTING_START_DATE"
log_info "  투표 종료: $VOTING_END_DATE"
log_info "  필요 정족수: $REQUIRED_QUORUM"

# 거버넌스 제안 저장 (수정된 페이로드)
SAVE_PROPOSAL_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/governance/proposals/save" \
    -H "Content-Type: application/json" \
    -d "{
        \"creatorId\": \"$USER1_ID\",
        \"title\": \"$PROPOSAL_TITLE\",
        \"description\": \"$PROPOSAL_DESCRIPTION\",
        \"votingStartDate\": \"$VOTING_START_DATE\",
        \"votingEndDate\": \"$VOTING_END_DATE\",
        \"requiredQuorum\": $REQUIRED_QUORUM,
        \"creatorWalletAddress\": \"$WALLET_ADDRESS\",
        \"proposalFee\": $PROPOSAL_FEE,
        \"networkType\": \"ETHEREUM\"
    }")

PROPOSAL_ID=$(echo $SAVE_PROPOSAL_RESPONSE | grep -o '"id":"[^"]*"' | cut -d'"' -f4)

if [ -n "$PROPOSAL_ID" ]; then
    log_success "거버넌스 제안 저장 성공!"
    log_info "제안 ID: $PROPOSAL_ID"
else
    log_error "거버넌스 제안 저장 실패: $SAVE_PROPOSAL_RESPONSE"
    exit 1
fi

echo ""

# 4. 제안 완료되면 수수료를 충전
log_info "4️⃣ user1에게 이더리움 수수료 충전 중..."

CHARGE_FEE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/governance/proposals/$PROPOSAL_ID/charge-fee" \
    -H "Content-Type: application/json" \
    -d "{
        \"creatorWalletAddress\": \"$WALLET_ADDRESS\",
        \"proposalFee\": $PROPOSAL_FEE,
        \"networkType\": \"ETHEREUM\"
    }")

FEE_SUCCESS=$(echo "$CHARGE_FEE_RESPONSE" | grep -o '"success":true')

if [ -n "$FEE_SUCCESS" ]; then
    FEE_TX_HASH=$(echo "$CHARGE_FEE_RESPONSE" | grep -o '"feeTransactionHash":"[^"]*"' | cut -d'"' -f4)
    log_success "이더리움 수수료 충전 성공!"
    log_tx "수수료 트랜잭션 해시: $FEE_TX_HASH"
else
    log_error "이더리움 수수료 충전 실패: $CHARGE_FEE_RESPONSE"
    exit 1
fi

# 5. 이더리움 수수료 송금 로그와 30초 대기
log_info "이더리움 수수료 송금 진행중 ..."
sleep 30

echo ""

# 6. 거버넌스 안건 제안 broadcast
log_info "6️⃣ 거버넌스 안건 제안 broadcast 중..."

BROADCAST_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/governance/proposals/$PROPOSAL_ID/broadcast" \
    -H "Content-Type: application/json" \
    -d "{
        \"creatorWalletAddress\": \"$WALLET_ADDRESS\",
        \"proposalFee\": $PROPOSAL_FEE,
        \"networkType\": \"ETHEREUM\"
    }")

BROADCAST_SUCCESS=$(echo "$BROADCAST_RESPONSE" | grep -o '"success":true')

if [ -n "$BROADCAST_SUCCESS" ]; then
    GOVERNANCE_TX_HASH=$(echo "$BROADCAST_RESPONSE" | grep -o '"governanceTransactionHash":"[^"]*"' | cut -d'"' -f4)
    log_success "거버넌스 안건 제안 broadcast 성공!"
    log_tx "거버넌스 트랜잭션 해시: $GOVERNANCE_TX_HASH"
else
    log_error "거버넌스 안건 제안 broadcast 실패: $BROADCAST_RESPONSE"
    exit 1
fi

echo ""
log_info "ProposalId를 변수에 저장: $PROPOSAL_ID"
log_info "거버넌스 시작 시간이 약간의 블록 갯수 오차가 있기에 안전적인 테스트를 위해서는 2분 딜레이!"
log_info "2분 후 투표를 시작합니다..."

sleep 120

echo ""

# 7. 나머지 user는 free token 신청하고 exchange 요청
log_info "7️⃣ 나머지 사용자들의 free token 신청 및 exchange 요청 중..."

for i in $(seq 2 $USER_COUNT); do
    log_info "사용자 $i 처리 중..."
    
    USER_ID=${USER_IDS[$i]}
    USER_TOKEN=${USER_TOKENS[$i]}
    
    # 무료 포인트 수령
    log_info "사용자 $i 무료 포인트 수령 중..."
    POINT_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/points/receive-free" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $USER_TOKEN" \
        -d "{
            \"amount\": $POINT_AMOUNT
        }")
    
    if echo "$POINT_RESPONSE" | grep -q "receivedAmount"; then
        RECEIVED_AMOUNT=$(echo $POINT_RESPONSE | grep -o '"receivedAmount":[0-9]*' | cut -d':' -f2)
        log_success "사용자 $i 포인트 수령 성공 - 받은 양: $RECEIVED_AMOUNT"
    else
        log_error "사용자 $i 포인트 수령 실패: $POINT_RESPONSE"
        continue
    fi
    
    # 이더리움 지갑 생성
    log_info "사용자 $i 이더리움 지갑 생성 중..."
    WALLET_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/wallets" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $USER_TOKEN" \
        -d "{
            \"userId\": \"$USER_ID\",
            \"networkType\": \"ETHEREUM\"
        }")
    
    WALLET_ADDRESS=$(echo $WALLET_RESPONSE | grep -o '"walletAddress":"[^"]*"' | cut -d'"' -f4)
    
    if [ -z "$WALLET_ADDRESS" ]; then
        log_error "사용자 $i 지갑 생성 실패: $WALLET_RESPONSE"
        continue
    fi
    
    USER_WALLETS[$i]=$WALLET_ADDRESS
    log_success "사용자 $i 지갑 생성 성공 - 주소: $WALLET_ADDRESS"
    
    # Exchange 요청
    log_info "사용자 $i Exchange 요청 중..."
    EXCHANGE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/exchange/request" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $USER_TOKEN" \
        -d "{
            \"pointAmount\": $EXCHANGE_AMOUNT,
            \"walletAddress\": \"$WALLET_ADDRESS\"
        }")
    
    EXCHANGE_ID=$(echo $EXCHANGE_RESPONSE | grep -o '"exchangeRequestId":"[^"]*"' | cut -d'"' -f4)
    
    if [ -z "$EXCHANGE_ID" ]; then
        log_error "사용자 $i Exchange 요청 실패: $EXCHANGE_RESPONSE"
        continue
    fi
    
    log_success "사용자 $i Exchange 요청 성공 - 요청 ID: $EXCHANGE_ID"
    
    # Exchange 처리
    log_info "사용자 $i Exchange 처리 중..."
    PROCESS_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/exchange/${EXCHANGE_ID}/process" \
        -H "Content-Type: application/json")
    
    if echo "$PROCESS_RESPONSE" | grep -q "교환이 처리되었습니다"; then
        log_success "사용자 $i Exchange 처리 성공!"
    else
        log_error "사용자 $i Exchange 처리 실패: $PROCESS_RESPONSE"
        continue
    fi
    
    # Exchange 상세 정보 조회
    EXCHANGE_DETAIL=$(curl -s -X GET "${BASE_URL}/api/exchange/${EXCHANGE_ID}" \
        -H "Content-Type: application/json")
    
    TXHASH=$(echo $EXCHANGE_DETAIL | grep -o '"transactionSignature":"[^"]*"' | cut -d'"' -f4)
    if [ -n "$TXHASH" ] && [ "$TXHASH" != "null" ]; then
        log_tx "사용자 $i ERC20 트랜잭션 해시: $TXHASH"
    fi
done

echo ""

# 8. 투표를 위해 필요한 수수료를 충전
log_info "8️⃣ 투표를 위해 필요한 수수료 충전 중..."

for i in $(seq 2 $USER_COUNT); do
    log_info "사용자 $i 투표 수수료 충전 중..."
    
    USER_ID=${USER_IDS[$i]}
    WALLET_ADDRESS=${USER_WALLETS[$i]}
    
    # 투표 수수료 충전 (간단한 이더 전송)
    FEE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/governance/proposals/$PROPOSAL_ID/charge-fee" \
        -H "Content-Type: application/json" \
        -d "{
            \"creatorWalletAddress\": \"$WALLET_ADDRESS\",
            \"proposalFee\": $PROPOSAL_FEE,
            \"networkType\": \"ETHEREUM\"
        }")
    
    FEE_SUCCESS=$(echo "$FEE_RESPONSE" | grep -o '"success":true')
    
    if [ -n "$FEE_SUCCESS" ]; then
        FEE_TX_HASH=$(echo "$FEE_RESPONSE" | grep -o '"feeTransactionHash":"[^"]*"' | cut -d'"' -f4)
        log_success "사용자 $i 투표 수수료 충전 성공!"
        log_tx "사용자 $i 수수료 트랜잭션 해시: $FEE_TX_HASH"
    else
        log_error "사용자 $i 투표 수수료 충전 실패: $FEE_RESPONSE"
    fi
done

echo ""

# 9. 투표 진행
log_info "9️⃣ 투표 진행 중..."

# 제안 상태 확인 및 활성화
log_info "제안 상태 확인 중..."
PROPOSAL_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/governance/proposals/${PROPOSAL_ID}")
PROPOSAL_STATUS=$(echo "$PROPOSAL_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
log_info "현재 제안 상태: $PROPOSAL_STATUS"

# DRAFT -> ACTIVE
if [ "$PROPOSAL_STATUS" = "DRAFT" ]; then
    log_info "제안 활성화 중..."
    ACTIVATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/governance/proposals/${PROPOSAL_ID}/activate")
    
    if echo "$ACTIVATE_RESPONSE" | grep -q '"success":true'; then
        log_success "제안이 활성화되었습니다!"
        PROPOSAL_STATUS="ACTIVE"
    else
        log_error "제안 활성화 실패!"
        exit 1
    fi
fi

# 투표 기간 수정
if [ "$PROPOSAL_STATUS" = "ACTIVE" ]; then
    log_info "투표 기간 수정 중..."
    UPDATE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/governance/proposals/${PROPOSAL_ID}/update-voting-period")
    
    if echo "$UPDATE_RESPONSE" | grep -q '"success":true'; then
        log_success "투표 기간이 수정되었습니다!"
    else
        log_error "투표 기간 수정 실패!"
        exit 1
    fi
fi

# 투표 시작
if [ "$PROPOSAL_STATUS" = "ACTIVE" ]; then
    log_info "투표 시작 중..."
    START_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/governance/proposals/${PROPOSAL_ID}/start-voting")
    
    if echo "$START_RESPONSE" | grep -q '"success":true'; then
        log_success "투표가 시작되었습니다!"
        PROPOSAL_STATUS="VOTING"
    else
        log_error "투표 시작 실패!"
        exit 1
    fi
fi

# 투표 실행
if [ "$PROPOSAL_STATUS" = "VOTING" ]; then
    log_info "투표 실행 중..."
    
    # 찬성 투표
    for i in $(seq 1 $YES_VOTES); do
        if [ $i -le $USER_COUNT ]; then
            USER_ID=${USER_IDS[$i]}
            WALLET_ADDRESS=${USER_WALLETS[$i]}
            
            log_info "사용자 $i 찬성 투표 중..."
            
            VOTE_PAYLOAD=$(cat <<EOF
{
    "voterId": "$USER_ID",
    "voteType": "YES",
    "reason": "사용자 $i의 찬성 투표입니다.",
    "voterWalletAddress": "$WALLET_ADDRESS",
    "networkType": "ETHEREUM"
}
EOF
)
            
            VOTE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/governance/proposals/${PROPOSAL_ID}/vote-blockchain" \
                -H "Content-Type: application/json" \
                -d "$VOTE_PAYLOAD")
            
            if echo "$VOTE_RESPONSE" | grep -q '"success":true'; then
                log_success "사용자 $i 찬성 투표 성공!"
            else
                log_error "사용자 $i 찬성 투표 실패: $VOTE_RESPONSE"
            fi
        fi
    done
    
    # 반대 투표
    for i in $(seq $((YES_VOTES + 1)) $((YES_VOTES + NO_VOTES))); do
        if [ $i -le $USER_COUNT ]; then
            USER_ID=${USER_IDS[$i]}
            WALLET_ADDRESS=${USER_WALLETS[$i]}
            
            log_info "사용자 $i 반대 투표 중..."
            
            VOTE_PAYLOAD=$(cat <<EOF
{
    "voterId": "$USER_ID",
    "voteType": "NO",
    "reason": "사용자 $i의 반대 투표입니다.",
    "voterWalletAddress": "$WALLET_ADDRESS",
    "networkType": "ETHEREUM"
}
EOF
)
            
            VOTE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/governance/proposals/${PROPOSAL_ID}/vote-blockchain" \
                -H "Content-Type: application/json" \
                -d "$VOTE_PAYLOAD")
            
            if echo "$VOTE_RESPONSE" | grep -q '"success":true'; then
                log_success "사용자 $i 반대 투표 성공!"
            else
                log_error "사용자 $i 반대 투표 실패: $VOTE_RESPONSE"
            fi
        fi
    done
    
    # 기권 투표
    for i in $(seq $((YES_VOTES + NO_VOTES + 1)) $((YES_VOTES + NO_VOTES + ABSTAIN_VOTES))); do
        if [ $i -le $USER_COUNT ]; then
            USER_ID=${USER_IDS[$i]}
            WALLET_ADDRESS=${USER_WALLETS[$i]}
            
            log_info "사용자 $i 기권 투표 중..."
            
            VOTE_PAYLOAD=$(cat <<EOF
{
    "voterId": "$USER_ID",
    "voteType": "ABSTAIN",
    "reason": "사용자 $i의 기권 투표입니다.",
    "voterWalletAddress": "$WALLET_ADDRESS",
    "networkType": "ETHEREUM"
}
EOF
)
            
            VOTE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/governance/proposals/${PROPOSAL_ID}/vote-blockchain" \
                -H "Content-Type: application/json" \
                -d "$VOTE_PAYLOAD")
            
            if echo "$VOTE_RESPONSE" | grep -q '"success":true'; then
                log_success "사용자 $i 기권 투표 성공!"
            else
                log_error "사용자 $i 기권 투표 실패: $VOTE_RESPONSE"
            fi
        fi
    done
else
    log_error "제안이 투표 가능한 상태가 아닙니다: $PROPOSAL_STATUS"
    exit 1
fi

echo ""
log_info "=== 🎯 시나리오 결과 요약 ==="
log_info "총 사용자 수: $USER_COUNT명"
log_info "찬성 투표: $YES_VOTES명"
log_info "반대 투표: $NO_VOTES명"
log_info "기권 투표: $ABSTAIN_VOTES명"
log_info "제안 ID: $PROPOSAL_ID"
log_info "제안 제목: $PROPOSAL_TITLE"

if [ -n "$GOVERNANCE_TX_HASH" ]; then
    log_tx "거버넌스 트랜잭션 해시: $GOVERNANCE_TX_HASH"
fi

echo ""
log_success " 거버넌스 투표 시나리오 완료!" 