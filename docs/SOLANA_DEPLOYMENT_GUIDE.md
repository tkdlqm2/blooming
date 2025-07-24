# Solana Devnet 거버넌스 컨트랙트 배포 가이드

## 목차
1. [개요](#개요)
2. [사전 준비](#사전-준비)
3. [Solana 개발 환경 설정](#solana-개발-환경-설정)
4. [거버넌스 컨트랙트 구조](#거버넌스-컨트랙트-구조)
5. [컨트랙트 개발](#컨트랙트-개발)
6. [배포 스크립트](#배포-스크립트)
7. [테스트 및 검증](#테스트-및-검증)
8. [모니터링 및 관리](#모니터링-및-관리)

## 개요

이 가이드는 Solana Devnet에 거버넌스 컨트랙트를 배포하는 방법을 설명합니다. 
거버넌스 컨트랙트는 다음과 같은 기능을 제공합니다:

- 제안(Proposal) 생성 및 관리
- 투표(Voting) 시스템
- 토큰 스테이킹 및 보상
- 거버넌스 토큰 관리

## 사전 준비

### 1. 필수 도구 설치

```bash
# Node.js 설치 (v18 이상)
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install 18
nvm use 18

# Solana CLI 설치
sh -c "$(curl -sSfL https://release.solana.com/v1.17.0/install)"
export PATH="$HOME/.local/share/solana/install/active_release/bin:$PATH"

# Anchor Framework 설치
cargo install --git https://github.com/coral-xyz/anchor avm --locked --force
avm install latest
avm use latest

# Rust 설치 (필요한 경우)
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
source ~/.cargo/env
```

### 2. 개발 환경 확인

```bash
# Solana CLI 버전 확인
solana --version

# Anchor 버전 확인
anchor --version

# Rust 버전 확인
rustc --version
```

## Solana 개발 환경 설정

### 1. Solana Devnet 설정

```bash
# Devnet으로 설정
solana config set --url devnet

# 현재 설정 확인
solana config get

# Devnet SOL 받기 (테스트용)
solana airdrop 2
```

### 2. 키페어 생성 및 관리

```bash
# 새 키페어 생성
solana-keygen new --outfile ~/.config/solana/devnet-wallet.json

# 키페어 경로 설정
solana config set --keypair ~/.config/solana/devnet-wallet.json

# 잔액 확인
solana balance
```

### 3. 프로그램 ID 생성

```bash
# 프로그램 ID 생성
solana address -k ~/.config/solana/devnet-wallet.json

# 또는 새 키페어로 프로그램 ID 생성
solana-keygen new -o target/deploy/governance-keypair.json
```

## 거버넌스 컨트랙트 구조

### 1. 프로젝트 구조

```
governance-contracts/
├── programs/
│   └── governance/
│       ├── src/
│       │   ├── lib.rs
│       │   ├── state.rs
│       │   ├── instructions/
│       │   │   ├── mod.rs
│       │   │   ├── create_proposal.rs
│       │   │   ├── vote.rs
│       │   │   ├── execute_proposal.rs
│       │   │   └── stake_tokens.rs
│       │   └── errors.rs
│       ├── Cargo.toml
│       └── Anchor.toml
├── tests/
│   └── governance.ts
├── scripts/
│   ├── deploy.ts
│   └── setup.ts
└── package.json
```

### 2. Anchor.toml 설정

```toml
[features]
seeds = false
skip-lint = false

[programs.devnet]
governance = "YOUR_PROGRAM_ID"

[registry]
url = "https://api.apr.dev"

[provider]
cluster = "devnet"
wallet = "~/.config/solana/devnet-wallet.json"

[scripts]
test = "yarn run ts-mocha -p ./tsconfig.json -t 1000000 tests/**/*.ts"
```

## 컨트랙트 개발

### 1. 상태 구조 정의 (state.rs)

```rust
use anchor_lang::prelude::*;

#[account]
pub struct Proposal {
    pub id: u64,
    pub title: String,
    pub description: String,
    pub creator: Pubkey,
    pub yes_votes: u64,
    pub no_votes: u64,
    pub total_votes: u64,
    pub start_time: i64,
    pub end_time: i64,
    pub executed: bool,
    pub quorum: u64,
}

#[account]
pub struct Vote {
    pub proposal_id: u64,
    pub voter: Pubkey,
    pub vote_type: VoteType,
    pub voting_power: u64,
    pub timestamp: i64,
}

#[account]
pub struct Staker {
    pub owner: Pubkey,
    pub staked_amount: u64,
    pub voting_power: u64,
    pub last_stake_time: i64,
}

#[derive(AnchorSerialize, AnchorDeserialize, Clone, PartialEq, Eq)]
pub enum VoteType {
    Yes,
    No,
    Abstain,
}
```

### 2. 명령어 구현 (instructions/)

#### create_proposal.rs
```rust
use anchor_lang::prelude::*;
use crate::state::*;

#[derive(Accounts)]
pub struct CreateProposal<'info> {
    #[account(mut)]
    pub creator: Signer<'info>,
    
    #[account(
        init,
        payer = creator,
        space = 8 + Proposal::INIT_SPACE,
        seeds = [b"proposal", &[proposal_id]],
        bump
    )]
    pub proposal: Account<'info, Proposal>,
    
    pub system_program: Program<'info, System>,
}

pub fn create_proposal(
    ctx: Context<CreateProposal>,
    title: String,
    description: String,
    duration: i64,
    quorum: u64,
) -> Result<()> {
    let proposal = &mut ctx.accounts.proposal;
    let clock = Clock::get()?;
    
    proposal.id = proposal_id;
    proposal.title = title;
    proposal.description = description;
    proposal.creator = ctx.accounts.creator.key();
    proposal.start_time = clock.unix_timestamp;
    proposal.end_time = clock.unix_timestamp + duration;
    proposal.quorum = quorum;
    proposal.executed = false;
    
    Ok(())
}
```

### 3. 메인 라이브러리 (lib.rs)

```rust
use anchor_lang::prelude::*;

declare_id!("YOUR_PROGRAM_ID");

pub mod state;
pub mod instructions;
pub mod errors;

use instructions::*;

#[program]
pub mod governance {
    use super::*;

    pub fn create_proposal(
        ctx: Context<CreateProposal>,
        title: String,
        description: String,
        duration: i64,
        quorum: u64,
    ) -> Result<()> {
        instructions::create_proposal::create_proposal(ctx, title, description, duration, quorum)
    }

    pub fn vote(
        ctx: Context<Vote>,
        proposal_id: u64,
        vote_type: VoteType,
    ) -> Result<()> {
        instructions::vote::vote(ctx, proposal_id, vote_type)
    }

    pub fn execute_proposal(
        ctx: Context<ExecuteProposal>,
        proposal_id: u64,
    ) -> Result<()> {
        instructions::execute_proposal::execute_proposal(ctx, proposal_id)
    }

    pub fn stake_tokens(
        ctx: Context<StakeTokens>,
        amount: u64,
    ) -> Result<()> {
        instructions::stake_tokens::stake_tokens(ctx, amount)
    }
}
```

## 배포 스크립트

### 1. 배포 스크립트 (scripts/deploy.ts)

```typescript
import * as anchor from "@coral-xyz/anchor";
import { Program } from "@coral-xyz/anchor";
import { Governance } from "../target/types/governance";
import { Keypair, LAMPORTS_PER_SOL, PublicKey } from "@solana/web3.js";

async function main() {
    // 연결 설정
    const connection = new anchor.web3.Connection(
        anchor.web3.clusterApiUrl("devnet"),
        "confirmed"
    );

    // 키페어 로드
    const wallet = Keypair.fromSecretKey(
        Buffer.from(JSON.parse(require("fs").readFileSync(
            process.env.ANCHOR_WALLET || "~/.config/solana/devnet-wallet.json",
            "utf-8"
        )))
    );

    // Provider 설정
    const provider = new anchor.AnchorProvider(
        connection,
        new anchor.Wallet(wallet),
        { commitment: "confirmed" }
    );
    anchor.setProvider(provider);

    // 프로그램 로드
    const program = anchor.workspace.Governance as Program<Governance>;

    console.log("🚀 거버넌스 컨트랙트 배포 시작...");

    try {
        // 프로그램 배포
        const programId = await program.programId;
        console.log("📦 프로그램 ID:", programId.toString());

        // 초기 설정
        await setupGovernance(program, wallet);

        console.log("✅ 거버넌스 컨트랙트 배포 완료!");
        console.log("🔗 Devnet Explorer:", `https://explorer.solana.com/address/${programId.toString()}?cluster=devnet`);

    } catch (error) {
        console.error("❌ 배포 실패:", error);
        throw error;
    }
}

async function setupGovernance(program: Program<Governance>, wallet: Keypair) {
    console.log("⚙️ 거버넌스 초기 설정 중...");

    // 초기 제안 생성
    const proposalTitle = "초기 거버넌스 설정";
    const proposalDescription = "거버넌스 시스템 초기 설정을 위한 제안";
    
    const proposalKeypair = Keypair.generate();
    
    await program.methods
        .createProposal(
            proposalTitle,
            proposalDescription,
            86400, // 24시간
            1000   // 최소 쿼럼
        )
        .accounts({
            creator: wallet.publicKey,
            proposal: proposalKeypair.publicKey,
            systemProgram: anchor.web3.SystemProgram.programId,
        })
        .signers([proposalKeypair])
        .rpc();

    console.log("📋 초기 제안 생성 완료");
}

main().catch(console.error);
```

### 2. 설정 스크립트 (scripts/setup.ts)

```typescript
import * as anchor from "@coral-xyz/anchor";
import { Program } from "@coral-xyz/anchor";
import { Governance } from "../target/types/governance";
import { Keypair, PublicKey } from "@solana/web3.js";

export async function setupGovernanceSystem(
    program: Program<Governance>,
    wallet: Keypair
) {
    console.log("🔧 거버넌스 시스템 설정 중...");

    // 1. 거버넌스 토큰 설정
    const tokenMint = await createGovernanceToken(program, wallet);
    
    // 2. 스테이킹 풀 설정
    const stakingPool = await createStakingPool(program, wallet, tokenMint);
    
    // 3. 거버넌스 파라미터 설정
    await setGovernanceParameters(program, wallet);

    console.log("✅ 거버넌스 시스템 설정 완료");
    
    return {
        tokenMint,
        stakingPool
    };
}

async function createGovernanceToken(
    program: Program<Governance>,
    wallet: Keypair
): Promise<PublicKey> {
    // SPL 토큰 생성 로직
    // ... 구현 필요
    return new PublicKey("token_mint_address");
}

async function createStakingPool(
    program: Program<Governance>,
    wallet: Keypair,
    tokenMint: PublicKey
): Promise<PublicKey> {
    // 스테이킹 풀 생성 로직
    // ... 구현 필요
    return new PublicKey("staking_pool_address");
}

async function setGovernanceParameters(
    program: Program<Governance>,
    wallet: Keypair
) {
    // 거버넌스 파라미터 설정
    // - 최소 제안 기간
    // - 최소 투표 기간
    // - 최소 쿼럼
    // - 최소 스테이킹 금액
}
```

## 테스트 및 검증

### 1. 테스트 스크립트 (tests/governance.ts)

```typescript
import * as anchor from "@coral-xyz/anchor";
import { Program } from "@coral-xyz/anchor";
import { Governance } from "../target/types/governance";
import { Keypair, LAMPORTS_PER_SOL, PublicKey } from "@solana/web3.js";
import { expect } from "chai";

describe("Governance Contract", () => {
    const provider = anchor.AnchorProvider.env();
    anchor.setProvider(provider);

    const program = anchor.workspace.Governance as Program<Governance>;
    const wallet = provider.wallet as anchor.Wallet;

    it("제안 생성 테스트", async () => {
        const proposalKeypair = Keypair.generate();
        
        const title = "테스트 제안";
        const description = "테스트 제안 설명";
        const duration = 3600; // 1시간
        const quorum = 100;

        await program.methods
            .createProposal(title, description, duration, quorum)
            .accounts({
                creator: wallet.publicKey,
                proposal: proposalKeypair.publicKey,
                systemProgram: anchor.web3.SystemProgram.programId,
            })
            .signers([proposalKeypair])
            .rpc();

        const proposal = await program.account.proposal.fetch(proposalKeypair.publicKey);
        
        expect(proposal.title).to.equal(title);
        expect(proposal.description).to.equal(description);
        expect(proposal.creator.toString()).to.equal(wallet.publicKey.toString());
    });

    it("투표 테스트", async () => {
        // 투표 로직 테스트
    });

    it("제안 실행 테스트", async () => {
        // 제안 실행 로직 테스트
    });
});
```

### 2. 배포 후 검증

```bash
# 프로그램 배포
anchor build
anchor deploy

# 테스트 실행
anchor test

# 프로그램 정보 확인
solana program show --programs

# 계정 정보 확인
solana account <PROGRAM_ID>
```

## 모니터링 및 관리

### 1. 로그 모니터링

```typescript
// 이벤트 구독
connection.onProgramAccountChange(
    program.programId,
    (accountInfo, context) => {
        console.log("계정 변경:", accountInfo.account.data);
    }
);
```

### 2. 관리 스크립트

```typescript
// 거버넌스 상태 조회
async function getGovernanceState(program: Program<Governance>) {
    const proposals = await program.account.proposal.all();
    const stakers = await program.account.staker.all();
    
    return {
        totalProposals: proposals.length,
        activeProposals: proposals.filter(p => !p.account.executed).length,
        totalStakers: stakers.length,
        totalStaked: stakers.reduce((sum, s) => sum + s.account.stakedAmount.toNumber(), 0)
    };
}
```

## 문제 해결

### 1. 일반적인 오류

```bash
# 잔액 부족
solana airdrop 2

# 프로그램 업그레이드
anchor upgrade target/deploy/governance.so

# 계정 초기화
anchor migrate
```

### 2. 디버깅 팁

- `anchor logs` 명령어로 로그 확인
- Solana Explorer에서 트랜잭션 추적
- `solana confirm` 명령어로 트랜잭션 상태 확인

## 다음 단계

1. **메인넷 배포**: Devnet 테스트 완료 후 메인넷 배포
2. **프론트엔드 연동**: 웹 인터페이스 개발
3. **보안 감사**: 전문 보안 감사 수행
4. **커뮤니티 투표**: 거버넌스 토큰 홀더 투표

---

## 참고 자료

- [Solana 개발자 문서](https://docs.solana.com/)
- [Anchor Framework 문서](https://www.anchor-lang.com/)
- [Solana Devnet Explorer](https://explorer.solana.com/?cluster=devnet)
- [SPL 토큰 프로그램](https://spl.solana.com/token) 