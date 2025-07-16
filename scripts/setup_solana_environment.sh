#!/bin/bash

echo "🚀 Solana Devnet 거버넌스 컨트랙트 배포 환경 설정"
echo "=================================================="

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

# 1. Node.js 설치 확인
print_step "Node.js 설치 확인 중..."
if ! command -v node &> /dev/null; then
    print_warning "Node.js가 설치되지 않았습니다. 설치를 시작합니다..."
    
    # NVM 설치
    if ! command -v nvm &> /dev/null; then
        curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
        export NVM_DIR="$HOME/.nvm"
        [ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"
    fi
    
    # Node.js 18 설치
    nvm install 18
    nvm use 18
    nvm alias default 18
else
    NODE_VERSION=$(node --version)
    print_success "Node.js $NODE_VERSION 설치됨"
fi

# 2. Solana CLI 설치 확인
print_step "Solana CLI 설치 확인 중..."
if ! command -v solana &> /dev/null; then
    print_warning "Solana CLI가 설치되지 않았습니다. 설치를 시작합니다..."
    
    sh -c "$(curl -sSfL https://release.solana.com/v1.17.0/install)"
    export PATH="$HOME/.local/share/solana/install/active_release/bin:$PATH"
    
    # PATH를 .bashrc에 추가
    echo 'export PATH="$HOME/.local/share/solana/install/active_release/bin:$PATH"' >> ~/.bashrc
    source ~/.bashrc
else
    SOLANA_VERSION=$(solana --version)
    print_success "Solana CLI $SOLANA_VERSION 설치됨"
fi

# 3. Rust 설치 확인
print_step "Rust 설치 확인 중..."
if ! command -v rustc &> /dev/null; then
    print_warning "Rust가 설치되지 않았습니다. 설치를 시작합니다..."
    
    curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y
    source ~/.cargo/env
else
    RUST_VERSION=$(rustc --version)
    print_success "Rust $RUST_VERSION 설치됨"
fi

# 4. Anchor Framework 설치 확인
print_step "Anchor Framework 설치 확인 중..."
if ! command -v anchor &> /dev/null; then
    print_warning "Anchor Framework가 설치되지 않았습니다. 설치를 시작합니다..."
    
    # AVM 설치
    cargo install --git https://github.com/coral-xyz/anchor avm --locked --force
    
    # 최신 Anchor 설치
    avm install latest
    avm use latest
else
    ANCHOR_VERSION=$(anchor --version)
    print_success "Anchor Framework $ANCHOR_VERSION 설치됨"
fi

# 5. Solana Devnet 설정
print_step "Solana Devnet 설정 중..."
solana config set --url devnet
print_success "Devnet으로 설정됨"

# 6. 키페어 생성
print_step "Devnet 키페어 생성 중..."
if [ ! -f ~/.config/solana/devnet-wallet.json ]; then
    mkdir -p ~/.config/solana
    solana-keygen new --outfile ~/.config/solana/devnet-wallet.json --no-bip39-passphrase
    print_success "새 키페어 생성됨: ~/.config/solana/devnet-wallet.json"
else
    print_success "기존 키페어 사용: ~/.config/solana/devnet-wallet.json"
fi

# 키페어 경로 설정
solana config set --keypair ~/.config/solana/devnet-wallet.json

# 7. 잔액 확인 및 에어드롭
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

# 8. 프로그램 ID 생성
print_step "프로그램 ID 생성 중..."
mkdir -p target/deploy
if [ ! -f target/deploy/governance-keypair.json ]; then
    solana-keygen new -o target/deploy/governance-keypair.json --no-bip39-passphrase
    print_success "프로그램 키페어 생성됨: target/deploy/governance-keypair.json"
else
    print_success "기존 프로그램 키페어 사용"
fi

PROGRAM_ID=$(solana address -k target/deploy/governance-keypair.json)
print_success "프로그램 ID: $PROGRAM_ID"

# 9. 환경 변수 설정
print_step "환경 변수 설정 중..."
cat > .env << EOF
# Solana Devnet 설정
SOLANA_CLUSTER=devnet
SOLANA_RPC_URL=https://api.devnet.solana.com
ANCHOR_WALLET=~/.config/solana/devnet-wallet.json
PROGRAM_ID=$PROGRAM_ID

# 거버넌스 설정
GOVERNANCE_TOKEN_NAME="Governance Token"
GOVERNANCE_TOKEN_SYMBOL="GOV"
GOVERNANCE_TOKEN_DECIMALS=9
MIN_STAKE_AMOUNT=1000000000
MIN_PROPOSAL_DURATION=3600
MIN_VOTING_DURATION=86400
MIN_QUORUM=1000
EOF

print_success "환경 변수 파일 생성됨: .env"

# 10. 프로젝트 구조 생성
print_step "프로젝트 구조 생성 중..."
mkdir -p governance-contracts/{programs/governance/src/instructions,tests,scripts,app}

# Anchor.toml 생성
cat > governance-contracts/Anchor.toml << EOF
[features]
seeds = false
skip-lint = false

[programs.devnet]
governance = "$PROGRAM_ID"

[registry]
url = "https://api.apr.dev"

[provider]
cluster = "devnet"
wallet = "~/.config/solana/devnet-wallet.json"

[scripts]
test = "yarn run ts-mocha -p ./tsconfig.json -t 1000000 tests/**/*.ts"
EOF

# package.json 생성
cat > governance-contracts/package.json << EOF
{
  "scripts": {
    "lint:fix": "prettier */*.js \"*/**/*{.js,.ts}\" -w",
    "lint": "prettier */*.js \"*/**/*{.js,.ts}\" --check"
  },
  "dependencies": {
    "@coral-xyz/anchor": "^0.28.0"
  },
  "devDependencies": {
    "@types/bn.js": "^5.1.0",
    "@types/chai": "^4.3.0",
    "@types/mocha": "^9.0.0",
    "chai": "^4.2.0",
    "mocha": "^9.0.3",
    "prettier": "^2.6.2",
    "ts-mocha": "^10.0.0",
    "typescript": "^4.3.5"
  }
}
EOF

# tsconfig.json 생성
cat > governance-contracts/tsconfig.json << EOF
{
  "compilerOptions": {
    "types": ["mocha", "chai"],
    "typeRoots": ["./node_modules/@types"],
    "lib": ["es2015"],
    "module": "commonjs",
    "target": "es6",
    "esModuleInterop": true
  }
}
EOF

print_success "프로젝트 구조 생성 완료"

# 11. 설치 완료 메시지
echo ""
echo "🎉 Solana Devnet 환경 설정 완료!"
echo "=================================="
echo ""
echo "📋 다음 단계:"
echo "1. cd governance-contracts"
echo "2. yarn install"
echo "3. anchor build"
echo "4. anchor deploy"
echo ""
echo "🔗 유용한 링크:"
echo "- Devnet Explorer: https://explorer.solana.com/?cluster=devnet"
echo "- 프로그램 ID: $PROGRAM_ID"
echo "- 키페어 위치: ~/.config/solana/devnet-wallet.json"
echo ""
echo "💡 팁:"
echo "- 잔액이 부족하면: solana airdrop 2"
echo "- 설정 확인: solana config get"
echo "- 로그 확인: anchor logs"
echo ""

print_success "환경 설정이 완료되었습니다!" 