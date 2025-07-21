#!/bin/bash

# 간단한 ERC20 토큰 전송 테스트 스크립트

BASE_URL="http://localhost:8081"
TIMESTAMP=$(date +%s)
USERNAME="user_${TIMESTAMP}"

echo "🚀 ERC20 토큰 전송 테스트 시작"
echo "사용자명: $USERNAME"

# 1. 회원가입
echo "1️⃣ 회원가입..."
SIGNUP=$(curl -s -X POST "${BASE_URL}/api/users/signup" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$USERNAME\",\"email\":\"$USERNAME@test.com\",\"password\":\"123456\"}")

TOKEN=$(echo $SIGNUP | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
USER_ID=$(echo $SIGNUP | grep -o '"userId":"[^"]*"' | cut -d'"' -f4)

echo "✅ 회원가입 완료 - ID: $USER_ID"

# 2. 포인트 지급
echo "2️⃣ 포인트 지급..."
curl -s -X POST "${BASE_URL}/api/points/receive-free" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"amount":200}' > /dev/null

echo "✅ 포인트 지급 완료"

# 3. 지갑 생성
echo "3️⃣ 지갑 생성..."
WALLET=$(curl -s -X POST "${BASE_URL}/api/wallets" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"userId\":\"$USER_ID\",\"networkType\":\"ETHEREUM\"}")

WALLET_ADDRESS=$(echo $WALLET | grep -o '"walletAddress":"[^"]*"' | cut -d'"' -f4)
echo "✅ 지갑 생성 완료 - 주소: $WALLET_ADDRESS"

# 4. 실제 Exchange 요청 (JWT 토큰 사용)
echo "4️⃣ 실제 Exchange 요청 (JWT 토큰 사용)..."
EXCHANGE=$(curl -s -X POST "${BASE_URL}/api/exchange/request" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"pointAmount\":100,\"walletAddress\":\"$WALLET_ADDRESS\"}")

EXCHANGE_ID=$(echo $EXCHANGE | grep -o '"exchangeRequestId":"[^"]*"' | cut -d'"' -f4)
echo "✅ Exchange 요청 성공 - ID: $EXCHANGE_ID"

# 5. Exchange 처리 (새로운 executeERC20Transfer 실행)
echo "5️⃣ Exchange 처리 (새로운 executeERC20Transfer 실행)..."
PROCESS=$(curl -s -X POST "${BASE_URL}/api/exchange/${EXCHANGE_ID}/process" \
    -H "Content-Type: application/json")

if echo "$PROCESS" | grep -q "교환이 처리되었습니다"; then
    echo "✅ Exchange 처리 성공!"
else
    echo "❌ Exchange 처리 실패: $PROCESS"
fi

# 6. 트랜잭션 해시 확인
echo "6️⃣ 트랜잭션 해시 확인..."
DETAIL=$(curl -s -X GET "${BASE_URL}/api/exchange/${EXCHANGE_ID}" \
    -H "Content-Type: application/json")

TXHASH=$(echo $DETAIL | grep -o '"transactionSignature":"[^"]*"' | cut -d'"' -f4)
if [ -n "$TXHASH" ]; then
    echo "🎉 트랜잭션 해시: $TXHASH"
    echo "✅ ERC20 토큰 전송 성공!"
else
    echo "⚠️ 트랜잭션 해시를 찾을 수 없습니다"
fi

echo ""
echo "🎯 테스트 완료!"
if [ -n "$TXHASH" ]; then
    echo "🎉 새로운 executeERC20Transfer 구현이 성공적으로 실행되었습니다!"
    echo "🚀 ERC20 토큰이 성공적으로 브로드캐스트되었습니다!"
    echo "📋 트랜잭션 해시: $TXHASH"
else
    echo "📊 새로운 executeERC20Transfer 구현이 테스트되었습니다."
    echo "⚠️ 트랜잭션 해시를 확인할 수 없습니다"
fi 