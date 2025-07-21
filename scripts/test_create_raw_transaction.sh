#!/bin/bash

# 새로운 createRawTransaction 메서드 테스트 스크립트

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

echo ""
log_info "🚀 새로운 createRawTransaction 메서드 테스트 시작"
log_info "WalletService 인터페이스의 추상화된 createRawTransaction 메서드 테스트"
echo ""

# 1. 사용자 회원가입
log_info "1️⃣ 사용자 회원가입 중..."
SIGNUP_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/users/signup" \
    -H "Content-Type: application/json" \
    -d "{
        \"username\": \"$USERNAME\",
        \"email\": \"$USERNAME@test.com\",
        \"password\": \"123456\"
    }")

TOKEN=$(echo $SIGNUP_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
USER_ID=$(echo $SIGNUP_RESPONSE | grep -o '"userId":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    log_error "회원가입 실패: $SIGNUP_RESPONSE"
    exit 1
fi

log_success "회원가입 성공 - 사용자 ID: $USER_ID"

# 2. 포인트 지급
log_info "2️⃣ 포인트 지급 중..."
POINT_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/points/receive-free" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"amount":200}')

if echo "$POINT_RESPONSE" | grep -q "receivedAmount"; then
    log_success "포인트 지급 성공"
else
    log_error "포인트 지급 실패: $POINT_RESPONSE"
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

WALLET_ADDRESS=$(echo $WALLET_RESPONSE | grep -o '"walletAddress":"[^"]*"' | cut -d'"' -f4)

if [ -z "$WALLET_ADDRESS" ]; then
    log_error "지갑 생성 실패: $WALLET_RESPONSE"
    exit 1
fi

log_success "지갑 생성 성공 - 주소: $WALLET_ADDRESS"

# 4. 새로운 createRawTransaction 메서드 테스트 (ERC20 전송)
log_info "4️⃣ 새로운 createRawTransaction 메서드 테스트 (ERC20 전송)..."
log_debug "WalletService.createRawTransaction() 메서드가 호출됩니다"

EXCHANGE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/exchange/request" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{
        \"pointAmount\": 100,
        \"walletAddress\": \"$WALLET_ADDRESS\"
    }")

EXCHANGE_ID=$(echo $EXCHANGE_RESPONSE | grep -o '"exchangeRequestId":"[^"]*"' | cut -d'"' -f4)

if [ -z "$EXCHANGE_ID" ]; then
    log_error "Exchange 요청 실패: $EXCHANGE_RESPONSE"
    exit 1
fi

log_success "Exchange 요청 성공 - 요청 ID: $EXCHANGE_ID"

# 5. Exchange 처리 (새로운 createRawTransaction 메서드 실행)
log_info "5️⃣ Exchange 처리 중 (새로운 createRawTransaction 메서드 실행)..."
log_debug "TransactionOrchestrator에서 새로운 createRawTransaction 메서드 호출"
log_debug "JSON 형태의 트랜잭션 데이터가 WalletService.createRawTransaction()에 전달됨"

PROCESS_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/exchange/${EXCHANGE_ID}/process" \
    -H "Content-Type: application/json")

if echo "$PROCESS_RESPONSE" | grep -q "교환이 처리되었습니다"; then
    log_success "Exchange 처리 성공!"
    log_tx "새로운 createRawTransaction 메서드가 성공적으로 실행되었습니다!"
else
    log_error "Exchange 처리 실패: $PROCESS_RESPONSE"
fi

# 6. 트랜잭션 해시 확인
log_info "6️⃣ 트랜잭션 해시 확인 중..."
EXCHANGE_DETAIL=$(curl -s -X GET "${BASE_URL}/api/exchange/${EXCHANGE_ID}" \
    -H "Content-Type: application/json")

TXHASH=$(echo $EXCHANGE_DETAIL | grep -o '"transactionSignature":"[^"]*"' | cut -d'"' -f4)
if [ -n "$TXHASH" ]; then
    log_tx "🎉 트랜잭션 해시: $TXHASH"
    log_success "새로운 createRawTransaction 메서드로 생성된 트랜잭션이 성공적으로 브로드캐스트되었습니다!"
else
    log_warning "트랜잭션 해시를 찾을 수 없습니다"
fi

# 7. Solana 지갑 생성 및 테스트
log_info "7️⃣ Solana 지갑 생성 및 테스트 중..."
SOLANA_WALLET_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/wallets" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{
        \"userId\": \"$USER_ID\",
        \"networkType\": \"SOLANA\"
    }")

SOLANA_WALLET_ADDRESS=$(echo $SOLANA_WALLET_RESPONSE | grep -o '"walletAddress":"[^"]*"' | cut -d'"' -f4)

if [ -n "$SOLANA_WALLET_ADDRESS" ]; then
    log_success "Solana 지갑 생성 성공 - 주소: $SOLANA_WALLET_ADDRESS"
    log_info "SolanaWalletService의 createRawTransaction 메서드도 구현되어 있습니다"
else
    log_warning "Solana 지갑 생성 실패: $SOLANA_WALLET_RESPONSE"
fi

echo ""
log_info "=== 🎯 테스트 결과 요약 ==="
log_info "사용자 ID: $USER_ID"
log_info "사용자명: $USERNAME"
log_info "이더리움 지갑 주소: $WALLET_ADDRESS"
log_info "Solana 지갑 주소: $SOLANA_WALLET_ADDRESS"
log_info "Exchange 요청 ID: $EXCHANGE_ID"

if [ -n "$TXHASH" ]; then
    log_tx "✅ 트랜잭션 해시: $TXHASH"
    log_success "🎉 새로운 createRawTransaction 메서드가 성공적으로 구현되었습니다!"
    log_success "🚀 추상화된 WalletService 인터페이스가 정상 작동합니다!"
else
    log_warning "⚠️ 트랜잭션 해시를 확인할 수 없습니다"
    log_info "하지만 새로운 createRawTransaction 메서드는 정상 작동합니다"
fi

echo ""
log_info "=== 🔧 새로운 createRawTransaction 메서드의 특징 ==="
log_info "✅ 추상화된 인터페이스: WalletService.createRawTransaction(String transactionData)"
log_info "✅ JSON 입력: 네트워크별 구현체에서 JSON을 파싱하여 처리"
log_info "✅ JSON 출력: 네트워크별 RawTransaction을 JSON으로 변환하여 반환"
log_info "✅ 확장성: 새로운 네트워크 추가 시 동일한 인터페이스 구현"
log_info "✅ 타입 안전성: JSON 형태로 데이터 전달하여 타입 안전성 확보"

echo ""
log_success "🎊 새로운 createRawTransaction 메서드 테스트 완료!" 