#!/bin/bash

echo "🚀 Solana Devnet 거버넌스 컨트랙트 배포 스크립트"
echo "================================================"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 함수 정의
print_step() {
    echo -e "${BLUE}📋 $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# 1. 환경 확인
print_step "환경 확인 중..."

# Solana CLI 확인
if ! command -v solana &> /dev/null; then
    print_error "Solana CLI가 설치되지 않았습니다. 먼저 setup_solana_environment.sh를 실행하세요."
    exit 1
fi

# Anchor 확인
if ! command -v anchor &> /dev/null; then
    print_error "Anchor Framework가 설치되지 않았습니다. 먼저 setup_solana_environment.sh를 실행하세요."
    exit 1
fi

# Node.js 확인
if ! command -v node &> /dev/null; then
    print_error "Node.js가 설치되지 않았습니다. 먼저 setup_solana_environment.sh를 실행하세요."
    exit 1
fi

print_success "환경 확인 완료"

# 2. Devnet 설정 확인
print_step "Devnet 설정 확인 중..."
CURRENT_CLUSTER=$(solana config get | grep "RPC URL" | awk '{print $3}')

if [[ "$CURRENT_CLUSTER" != *"devnet"* ]]; then
    print_warning "Devnet으로 설정을 변경합니다..."
    solana config set --url devnet
    print_success "Devnet으로 설정됨"
else
    print_success "이미 Devnet으로 설정됨"
fi

# 3. 잔액 확인
print_step "잔액 확인 중..."
BALANCE=$(solana balance)
echo "현재 잔액: $BALANCE SOL"

if [ "$(echo $BALANCE | cut -d' ' -f1)" -lt 1 ]; then
    print_warning "잔액이 부족합니다. 에어드롭을 요청합니다..."
    solana airdrop 2
    sleep 5
    NEW_BALANCE=$(solana balance)
    print_success "새 잔액: $NEW_BALANCE SOL"
else
    print_success "충분한 잔액이 있습니다"
fi

# 4. 프로젝트 디렉토리로 이동
print_step "프로젝트 디렉토리 설정 중..."
if [ ! -d "governance-contracts" ]; then
    print_error "governance-contracts 디렉토리가 없습니다. 먼저 setup_solana_environment.sh를 실행하세요."
    exit 1
fi

cd governance-contracts
print_success "프로젝트 디렉토리로 이동됨"

# 5. 의존성 설치
print_step "의존성 설치 중..."
if [ ! -d "node_modules" ]; then
    npm install
    print_success "npm 의존성 설치 완료"
else
    print_success "의존성이 이미 설치됨"
fi

# 6. 프로그램 빌드
print_step "프로그램 빌드 중..."
anchor build

if [ $? -eq 0 ]; then
    print_success "프로그램 빌드 완료"
else
    print_error "프로그램 빌드 실패"
    exit 1
fi

# 7. 프로그램 ID 확인
print_step "프로그램 ID 확인 중..."
if [ -f "target/deploy/governance-keypair.json" ]; then
    PROGRAM_ID=$(solana address -k target/deploy/governance-keypair.json)
    print_success "프로그램 ID: $PROGRAM_ID"
    
    # lib.rs의 declare_id 업데이트
    sed -i.bak "s/declare_id!(\"11111111111111111111111111111111\")/declare_id!(\"$PROGRAM_ID\")/" programs/governance/src/lib.rs
    print_success "프로그램 ID 업데이트 완료"
else
    print_error "프로그램 키페어를 찾을 수 없습니다"
    exit 1
fi

# 8. 다시 빌드 (프로그램 ID 업데이트 후)
print_step "프로그램 ID 업데이트 후 재빌드 중..."
anchor build

if [ $? -eq 0 ]; then
    print_success "재빌드 완료"
else
    print_error "재빌드 실패"
    exit 1
fi

# 9. 프로그램 배포
print_step "프로그램 배포 중..."
anchor deploy

if [ $? -eq 0 ]; then
    print_success "프로그램 배포 완료"
else
    print_error "프로그램 배포 실패"
    exit 1
fi

# 10. 테스트 실행
print_step "테스트 실행 중..."
anchor test

if [ $? -eq 0 ]; then
    print_success "테스트 통과"
else
    print_warning "테스트 실패 (계속 진행)"
fi

# 11. 초기 설정 스크립트 실행
print_step "초기 설정 스크립트 실행 중..."
if [ -f "scripts/deploy.ts" ]; then
    npx ts-node scripts/deploy.ts
    
    if [ $? -eq 0 ]; then
        print_success "초기 설정 완료"
    else
        print_warning "초기 설정 실패 (수동으로 진행 필요)"
    fi
else
    print_warning "배포 스크립트를 찾을 수 없습니다"
fi

# 12. 배포 완료 정보
echo ""
echo "🎉 Solana Devnet 거버넌스 컨트랙트 배포 완료!"
echo "=============================================="
echo ""
echo "📋 배포 정보:"
echo "- 프로그램 ID: $PROGRAM_ID"
echo "- 클러스터: Devnet"
echo "- 배포자: $(solana address)"
echo ""
echo "🔗 유용한 링크:"
echo "- Devnet Explorer: https://explorer.solana.com/?cluster=devnet"
echo "- 프로그램: https://explorer.solana.com/address/$PROGRAM_ID?cluster=devnet"
echo ""
echo "📋 다음 단계:"
echo "1. 프론트엔드 연동"
echo "2. 거버넌스 토큰 분배"
echo "3. 커뮤니티 투표 시작"
echo ""
echo "💡 관리 명령어:"
echo "- 로그 확인: anchor logs"
echo "- 프로그램 업그레이드: anchor upgrade target/deploy/governance.so"
echo "- 계정 정보: solana account $PROGRAM_ID"
echo ""

print_success "배포가 성공적으로 완료되었습니다!" 