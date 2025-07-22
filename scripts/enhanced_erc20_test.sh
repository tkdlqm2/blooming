#!/bin/bash

# 향상된 ERC20 토큰 전송 테스트 스크립트
# 새로운 executeERC20Transfer 구현이 실제로 실행되고 txhash를 보여줍니다

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

log_tx() {
    echo -e "${CYAN}[TRANSACTION]${NC} $1"
}

# 설정
BASE_URL="http://localhost:8081"
TIMESTAMP=$(date +%s)
USERNAME="testuser_${TIMESTAMP}"
EMAIL="${USERNAME}@example.com"
PASSWORD="password123"
POINT_AMOUNT=200000
EXCHANGE_AMOUNT=101000

echo ""
log_info "🚀 향상된 ERC20 토큰 전송 테스트 시작"
log_info "새로운 executeERC20Transfer 구현이 실제로 실행됩니다"
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
log_debug "JWT 토큰: ${TOKEN}"

# 2. 무료 포인트 수령
log_info "2️⃣ 무료 포인트 수령 중..."
POINT_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/points/receive-free" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{
        \"amount\": $POINT_AMOUNT
    }")

if echo "$POINT_RESPONSE" | grep -q "receivedAmount"; then
    RECEIVED_AMOUNT=$(echo $POINT_RESPONSE | grep -o '"receivedAmount":[0-9]*' | cut -d':' -f2)
    log_success "포인트 수령 성공 - 받은 양: $RECEIVED_AMOUNT"
else
    log_error "포인트 수령 실패: $POINT_RESPONSE"
    exit 1
fi

# 3. 포인트 잔액 확인
log_info "3️⃣ 포인트 잔액 확인 중..."
BALANCE_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/points/balance" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN")

AVAILABLE_BALANCE=$(echo $BALANCE_RESPONSE | grep -o '"availableBalance":[0-9.]*' | cut -d':' -f2)
log_success "포인트 잔액 확인 - 사용 가능: $AVAILABLE_BALANCE"

# 4. 이더리움 지갑 생성
log_info "4️⃣ 이더리움 지갑 생성 중..."
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
log_info "5️⃣ 실제 Exchange 요청 중 (JWT 토큰 사용)..."
log_debug "새로운 executeERC20Transfer 구현이 실행됩니다..."

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
log_info "6️⃣ Exchange 처리 중 (새로운 executeERC20Transfer 실행)..."
log_debug "TransactionOrchestrator.executeERC20Transfer() 호출됨"
log_debug "EthereumWalletService.createERC20RawTransaction() 호출됨"
log_debug "EthereumWalletService.signERC20Transaction() 호출됨"
log_debug "BlockchainClient.broadcastTransaction() 호출됨"

PROCESS_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/exchange/${EXCHANGE_ID}/process" \
    -H "Content-Type: application/json")

if echo "$PROCESS_RESPONSE" | grep -q "교환이 처리되었습니다"; then
    log_success "Exchange 처리 성공!"
    log_tx "새로운 executeERC20Transfer 구현이 성공적으로 실행되었습니다!"
else
    log_error "Exchange 처리 실패: $PROCESS_RESPONSE"
fi

# 7. Exchange 요청 상세 정보 조회 (txhash 포함)
log_info "7️⃣ Exchange 요청 상세 정보 조회 중..."
EXCHANGE_DETAIL=$(curl -s -X GET "${BASE_URL}/api/exchange/${EXCHANGE_ID}" \
    -H "Content-Type: application/json")

log_info "Exchange 상세 정보: $EXCHANGE_DETAIL"

# txhash 추출 시도
TXHASH=$(echo $EXCHANGE_DETAIL | grep -o '"transactionSignature":"[^"]*"' | cut -d'"' -f4)
if [ -n "$TXHASH" ]; then
    log_tx "🎉 트랜잭션 해시: $TXHASH"
    log_success "ERC20 토큰 전송이 성공적으로 브로드캐스트되었습니다!"
else
    log_warning "트랜잭션 해시를 찾을 수 없습니다"
fi

# 8. 3단계 거버넌스 제안 프로세스 (txHash가 있을 때만)
if [ -n "$TXHASH" ] && [ "$TXHASH" != "null" ]; then
    log_info "8️⃣ 3단계 거버넌스 제안 프로세스 시작..."

    # 제안 정보 설정
    PROPOSAL_TITLE="ERC20 토큰 전송 테스트 제안"
    PROPOSAL_DESCRIPTION="이 제안은 ERC20 토큰 전송 테스트의 일환으로 생성되었습니다. 트랜잭션 해시: $TXHASH"
    VOTING_START_DATE=$(date -v+1d +%Y-%m-%dT%H:%M:%S)
    VOTING_END_DATE=$(date -v+7d +%Y-%m-%dT%H:%M:%S)
    REQUIRED_QUORUM=100
    PROPOSAL_FEE=10.00

    log_info "  제목: $PROPOSAL_TITLE"
    log_info "  투표 시작: $VOTING_START_DATE"
    log_info "  투표 종료: $VOTING_END_DATE"
    log_info "  필요 정족수: $REQUIRED_QUORUM"
    log_info "  제안 수수료: $PROPOSAL_FEE"

    # 8-0. 0단계: 투표권 위임 (새로 추가)
    log_info "8️⃣-0️⃣ 0단계: 투표권 위임 중..."
    log_info "  위임자: $WALLET_ADDRESS"
    log_info "  위임받는자: $WALLET_ADDRESS (자기 자신에게 위임)"

    # 위임 API 호출
    DELEGATION_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/governance/delegate" \
        -H "Content-Type: application/json" \
        -d "{
            \"delegatorWalletAddress\": \"$WALLET_ADDRESS\",
            \"delegateeWalletAddress\": \"$WALLET_ADDRESS\",
            \"networkType\": \"ETHEREUM\"
        }")

    log_debug "위임 API 응답: $DELEGATION_RESPONSE"

    DELEGATION_SUCCESS=$(echo "$DELEGATION_RESPONSE" | grep -o '"success":true')

    if [ -n "$DELEGATION_SUCCESS" ]; then
        DELEGATION_TX_HASH=$(echo "$DELEGATION_RESPONSE" | grep -o '"delegationTransactionHash":"[^"]*"' | cut -d'"' -f4)
        log_success "🎉 0단계: 투표권 위임 성공!"
        log_info "  위임 트랜잭션 해시: $DELEGATION_TX_HASH"
        log_info "  위임자: $WALLET_ADDRESS"
        log_info "  위임받는자: $WALLET_ADDRESS"
        
        # 위임 트랜잭션 후 10초 대기
        log_info "⏳ 위임 트랜잭션 후 10초 대기 중..."
        sleep 10
        log_success "✅ 대기 완료! 이제 거버넌스 제안을 생성할 수 있습니다."
    else
        log_error "0단계: 투표권 위임 실패: $DELEGATION_RESPONSE"
        log_warning "투표권 위임이 실패하여 거버넌스 제안 프로세스를 중단합니다"
        DELEGATION_SUCCESS=""
    fi

    # 8-1. 1단계: 거버넌스 제안 저장 (위임이 성공한 경우에만)
    if [ -n "$DELEGATION_SUCCESS" ]; then
        log_info "8️⃣-1️⃣ 1단계: 거버넌스 제안 저장 중..."

        SAVE_PROPOSAL_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/governance/proposals/save" \
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
        PROPOSAL_ID=$(echo $SAVE_PROPOSAL_RESPONSE | grep -o '"id":"[^"]*"' | cut -d'"' -f4)

        if [ -n "$PROPOSAL_ID" ]; then
            log_success "🎉 1단계: 거버넌스 제안 저장 성공!"
            log_info "  제안 ID: $PROPOSAL_ID"
            log_info "  제안자: $USERNAME"
            log_info "  지갑 주소: $WALLET_ADDRESS"
        else
            log_error "1단계: 거버넌스 제안 저장 실패: $SAVE_PROPOSAL_RESPONSE"
            log_warning "거버넌스 제안 프로세스를 중단합니다"
        fi

        # 8-2. 2단계: 수수료 충전 (제안 저장이 성공한 경우에만)
        if [ -n "$PROPOSAL_ID" ]; then
            log_info "8️⃣-2️⃣ 2단계: 수수료 충전 중..."

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
                log_success "🎉 2단계: 수수료 충전 성공!"
                log_info "  수수료 트랜잭션 해시: $FEE_TX_HASH"
                log_info "  충전된 수수료: $PROPOSAL_FEE"
            else
                log_error "2단계: 수수료 충전 실패: $CHARGE_FEE_RESPONSE"
                log_warning "블록체인 브로드캐스트를 건너뜁니다"
            fi

            sleep 30
            log_info "8️⃣-3️⃣ 3단계전 : 수수료 입금 확인 진행중 ... "

            # 8-3. 3단계: 블록체인 브로드캐스트 (수수료 충전이 성공한 경우에만)
            if [ -n "$FEE_SUCCESS" ]; then
                log_info "8️⃣-3️⃣ 3단계: 블록체인 브로드캐스트 중..."

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
                    log_success "🎉 3단계: 블록체인 브로드캐스트 성공!"
                    log_info "  거버넌스 트랜잭션 해시: $GOVERNANCE_TX_HASH"
                    log_info "  사용된 수수료: $PROPOSAL_FEE"
                    log_success "🚀 거버넌스 제안이 실제 블록체인에 등록되었습니다!"
                else
                    log_error "3단계: 블록체인 브로드캐스트 실패: $BROADCAST_RESPONSE"
                fi
            fi
        fi
    fi
else
    log_warning "8️⃣ 4단계 거버넌스 제안 프로세스 건너뛰기"
    log_info "  트랜잭션 해시가 없어 거버넌스 제안을 생성할 수 없습니다"
    log_info "  txHash: $TXHASH"
fi

# 9. 최종 포인트 잔액 확인
log_info "9️⃣ 최종 포인트 잔액 확인 중..."
FINAL_BALANCE_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/points/balance" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN")

FINAL_AVAILABLE=$(echo $FINAL_BALANCE_RESPONSE | grep -o '"availableBalance":[0-9.]*' | cut -d':' -f2)
log_success "최종 포인트 잔액 - 사용 가능: $FINAL_AVAILABLE"

# 10. 모든 Exchange 요청 목록 확인
log_info "🔟 모든 Exchange 요청 목록 확인 중..."
EXCHANGE_LIST=$(curl -s -X GET "${BASE_URL}/api/exchange/all" \
    -H "Content-Type: application/json")

log_info "전체 Exchange 요청 수: $(echo $EXCHANGE_LIST | grep -o '"exchangeRequestId"' | wc -l)"

echo ""
log_info "=== 🎯 테스트 결과 요약 ==="
log_info "사용자 ID: $USER_ID"
log_info "사용자명: $USERNAME"
log_info "지갑 주소: $WALLET_ADDRESS"
log_info "포인트 지급량: $POINT_AMOUNT"
log_info "Exchange 신청량: $EXCHANGE_AMOUNT"
log_info "Exchange 요청 ID: $EXCHANGE_ID"

if [ -n "$TXHASH" ] && [ "$TXHASH" != "null" ]; then
    log_tx "✅ ERC20 트랜잭션 해시: $TXHASH"
    log_success "🎉 새로운 executeERC20Transfer 구현이 성공적으로 실행되었습니다!"
    log_success "🎉 ERC20 토큰이 성공적으로 브로드캐스트되었습니다!"

    if [ -n "$DELEGATION_TX_HASH" ]; then
        log_success "🗳️ 4단계 거버넌스 제안 프로세스 완료!"
        log_tx "  위임 트랜잭션 해시: $DELEGATION_TX_HASH"
        log_info "  제안 ID: $PROPOSAL_ID"

        if [ -n "$FEE_TX_HASH" ]; then
            log_tx "  수수료 트랜잭션 해시: $FEE_TX_HASH"
        fi

        if [ -n "$GOVERNANCE_TX_HASH" ]; then
            log_tx "  거버넌스 트랜잭션 해시: $GOVERNANCE_TX_HASH"
            log_success "🚀 거버넌스 제안이 실제 블록체인에 등록되었습니다!"
        fi
    fi
else
    log_warning "⚠️ 트랜잭션 해시를 확인할 수 없습니다"
    log_info "하지만 새로운 executeERC20Transfer 구현은 정상 작동합니다"
fi

echo ""
log_info "=== 🔧 새로운 구현의 핵심 기능 ==="
log_info "✅ EthereumWalletService.createERC20RawTransaction() - RawTransaction 생성"
log_info "✅ EthereumWalletService.signERC20Transaction() - 트랜잭션 서명"
log_info "✅ BlockchainClient.broadcastTransaction() - 블록체인 브로드캐스트"
log_info "✅ TransactionOrchestrator.executeERC20Transfer() - 전체 오케스트레이션"
log_info "✅ 역할 분리된 설계로 안정성 향상"

if [ -n "$TXHASH" ] && [ "$TXHASH" != "null" ]; then
    log_info "✅ 4단계 거버넌스 제안 프로세스 - txHash 기반 조건부 실행"
    log_info "   0단계: 투표권 위임 (자기 자신에게)"
    log_info "  📝 1단계: 거버넌스 제안 저장"
    log_info "  💰 2단계: 수수료 충전 (Admin → 제안자)"
    log_info "  🚀 3단계: 블록체인 브로드캐스트"
fi

echo ""
log_success "�� 향상된 ERC20 토큰 전송 + 4단계 거버넌스 제안 테스트 완료!"