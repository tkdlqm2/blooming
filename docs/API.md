# API 문서

## 개요

이 문서는 Solana 기반 거버넌스 토큰 플랫폼의 REST API 명세를 제공합니다.

## 기본 정보

- **Base URL**: `http://localhost:8080/api`
- **Content-Type**: `application/json`
- **인증**: JWT 토큰 (향후 구현 예정)

## 공통 응답 형식

### 성공 응답
```json
{
  "success": true,
  "data": {
    // 응답 데이터
  },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### 에러 응답
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "유효하지 않은 입력입니다",
    "details": {
      "field": "userId",
      "reason": "필수 필드입니다"
    }
  },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Point Management API

### 포인트 적립

사용자에게 포인트를 적립합니다.

```http
POST /points/earn
```

#### 요청 파라미터
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| userId | UUID | Y | 사용자 ID |
| amount | BigDecimal | Y | 적립할 포인트 금액 |
| reason | String | Y | 적립 사유 |

#### 요청 예시
```bash
curl -X POST "http://localhost:8080/api/points/earn" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "amount": 100.0,
    "reason": "서비스 이용 적립"
  }'
```

#### 응답 예시
```json
{
  "success": true,
  "data": {
    "accountId": "123e4567-e89b-12d3-a456-426614174001",
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "balance": {
      "available": 100.0,
      "frozen": 0.0
    },
    "transactionId": "123e4567-e89b-12d3-a456-426614174002"
  }
}
```

### 포인트 잔액 조회

사용자의 포인트 잔액을 조회합니다.

```http
GET /points/balance/{userId}
```

#### 경로 파라미터
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| userId | UUID | 사용자 ID |

#### 응답 예시
```json
{
  "success": true,
  "data": {
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "balance": {
      "available": 100.0,
      "frozen": 50.0
    },
    "total": 150.0
  }
}
```

### 포인트 동결

포인트를 동결하여 교환 대기 상태로 만듭니다.

```http
POST /points/freeze
```

#### 요청 파라미터
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| userId | UUID | Y | 사용자 ID |
| amount | BigDecimal | Y | 동결할 포인트 금액 |
| reason | String | Y | 동결 사유 |

## Exchange API

### 교환 요청

포인트를 토큰으로 교환하는 요청을 생성합니다.

```http
POST /exchange/request
```

#### 요청 파라미터
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| userId | UUID | Y | 사용자 ID |
| pointAmount | BigDecimal | Y | 교환할 포인트 금액 |

#### 응답 예시
```json
{
  "success": true,
  "data": {
    "requestId": "123e4567-e89b-12d3-a456-426614174003",
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "pointAmount": 100.0,
    "tokenAmount": 100.0,
    "exchangeRate": 1.0,
    "status": "PENDING",
    "createdAt": "2024-01-01T00:00:00Z"
  }
}
```

### 현재 교환 비율 조회

현재 포인트-토큰 교환 비율을 조회합니다.

```http
GET /exchange/rate
```

#### 응답 예시
```json
{
  "success": true,
  "data": {
    "rate": 1.0,
    "lastUpdated": "2024-01-01T00:00:00Z"
  }
}
```

### 교환 요청 상세 조회

특정 교환 요청의 상세 정보를 조회합니다.

```http
GET /exchange/request/{requestId}
```

#### 경로 파라미터
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| requestId | UUID | 교환 요청 ID |

## Token Management API

### 토큰 민팅

사용자에게 토큰을 민팅합니다.

```http
POST /tokens/mint
```

#### 요청 파라미터
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| userId | UUID | Y | 사용자 ID |
| walletAddress | String | Y | Solana 지갑 주소 |
| amount | BigDecimal | Y | 민팅할 토큰 금액 |
| reason | String | Y | 민팅 사유 |

#### 응답 예시
```json
{
  "success": true,
  "data": {
    "transactionId": "123e4567-e89b-12d3-a456-426614174004",
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "walletAddress": "ABC123...",
    "amount": 100.0,
    "transactionSignature": "5J7X...",
    "status": "CONFIRMED"
  }
}
```

### 토큰 스테이킹

토큰을 스테이킹합니다.

```http
POST /tokens/stake
```

#### 요청 파라미터
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| userId | UUID | Y | 사용자 ID |
| amount | BigDecimal | Y | 스테이킹할 토큰 금액 |
| reason | String | Y | 스테이킹 사유 |

### 토큰 언스테이킹

스테이킹된 토큰을 언스테이킹합니다.

```http
POST /tokens/unstake
```

#### 요청 파라미터
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| userId | UUID | Y | 사용자 ID |
| amount | BigDecimal | Y | 언스테이킹할 토큰 금액 |
| reason | String | Y | 언스테이킹 사유 |

### 토큰 잔액 조회

사용자의 토큰 잔액을 조회합니다.

```http
GET /tokens/balance/{userId}
```

#### 응답 예시
```json
{
  "success": true,
  "data": {
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "walletAddress": "ABC123...",
    "balance": {
      "available": 50.0,
      "staked": 30.0,
      "locked": 20.0
    },
    "total": 100.0,
    "votingPower": 80.0
  }
}
```

### Solana 토큰 잔액 조회

Solana 블록체인에서 실제 토큰 잔액을 조회합니다.

```http
GET /tokens/solana-balance/{walletAddress}
```

#### 경로 파라미터
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| walletAddress | String | Solana 지갑 주소 |

#### 응답 예시
```json
{
  "success": true,
  "data": {
    "walletAddress": "ABC123...",
    "balance": 100.0,
    "lastUpdated": "2024-01-01T00:00:00Z"
  }
}
```

### 트랜잭션 상태 확인

Solana 트랜잭션의 상태를 확인합니다.

```http
GET /tokens/transaction/{transactionSignature}/status
```

#### 경로 파라미터
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| transactionSignature | String | Solana 트랜잭션 서명 |

## Governance API

### 프로포잘 생성

새로운 거버넌스 프로포잘을 생성합니다.

```http
POST /governance/proposals
```

#### 요청 본문
```json
{
  "proposerId": "123e4567-e89b-12d3-a456-426614174000",
  "title": "새로운 기능 제안",
  "description": "플랫폼에 새로운 기능을 추가하는 제안입니다.",
  "proposerAddress": "ABC123...",
  "votingPeriodDays": 7,
  "votingPeriodHours": 0,
  "minimumVotingPower": 100.0
}
```

#### 응답 예시
```json
{
  "success": true,
  "data": {
    "proposalId": "123e4567-e89b-12d3-a456-426614174005",
    "proposerId": "123e4567-e89b-12d3-a456-426614174000",
    "title": "새로운 기능 제안",
    "description": "플랫폼에 새로운 기능을 추가하는 제안입니다.",
    "status": "DRAFT",
    "votingPeriod": {
      "startAt": null,
      "endAt": null
    },
    "minimumVotingPower": 100.0,
    "createdAt": "2024-01-01T00:00:00Z"
  }
}
```

### 투표 시작

프로포잘의 투표를 시작합니다.

```http
POST /governance/proposals/{proposalId}/start
```

#### 경로 파라미터
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| proposalId | UUID | 프로포잘 ID |

#### 쿼리 파라미터
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| proposerTokenBalance | BigDecimal | Y | 제안자의 토큰 잔액 |

### 투표

프로포잘에 투표합니다.

```http
POST /governance/proposals/{proposalId}/vote
```

#### 경로 파라미터
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| proposalId | UUID | 프로포잘 ID |

#### 쿼리 파라미터
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| voterId | UUID | Y | 투표자 ID |
| voteOption | String | Y | 투표 옵션 (FOR, AGAINST, ABSTAIN) |
| votingPower | BigDecimal | Y | 투표 권한 |

#### 응답 예시
```json
{
  "success": true,
  "data": {
    "proposalId": "123e4567-e89b-12d3-a456-426614174005",
    "voterId": "123e4567-e89b-12d3-a456-426614174000",
    "voteOption": "FOR",
    "votingPower": 100.0,
    "votedAt": "2024-01-01T00:00:00Z"
  }
}
```

### 활성 프로포잘 조회

현재 활성 상태인 프로포잘들을 조회합니다.

```http
GET /governance/proposals/active
```

#### 응답 예시
```json
{
  "success": true,
  "data": [
    {
      "proposalId": "123e4567-e89b-12d3-a456-426614174005",
      "title": "새로운 기능 제안",
      "description": "플랫폼에 새로운 기능을 추가하는 제안입니다.",
      "status": "ACTIVE",
      "votingPeriod": {
        "startAt": "2024-01-01T00:00:00Z",
        "endAt": "2024-01-08T00:00:00Z"
      },
      "results": {
        "forVotes": 150.0,
        "againstVotes": 50.0,
        "abstainVotes": 10.0,
        "totalVotingPower": 210.0
      },
      "minimumVotingPower": 100.0
    }
  ]
}
```

### 프로포잘 상세 조회

특정 프로포잘의 상세 정보를 조회합니다.

```http
GET /governance/proposals/{proposalId}
```

#### 경로 파라미터
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| proposalId | UUID | 프로포잘 ID |

## 에러 코드

| 코드 | 설명 |
|------|------|
| VALIDATION_ERROR | 입력값 검증 실패 |
| NOT_FOUND | 리소스를 찾을 수 없음 |
| INSUFFICIENT_BALANCE | 잔액 부족 |
| INVALID_STATUS | 잘못된 상태 |
| DUPLICATE_VOTE | 중복 투표 |
| VOTING_NOT_ACTIVE | 투표 기간이 아님 |
| INSUFFICIENT_VOTING_POWER | 투표권 부족 |

## 인증 및 권한

현재 버전에서는 인증이 구현되지 않았습니다. 향후 JWT 기반 인증이 추가될 예정입니다.

## Rate Limiting

API 호출 제한은 현재 구현되지 않았습니다. 향후 구현 예정입니다.

## 웹훅 (Webhook)

도메인 이벤트 발생 시 웹훅을 통해 외부 시스템에 알림을 보낼 수 있습니다.

### 웹훅 이벤트

- `points.earned`: 포인트 적립
- `points.frozen`: 포인트 동결
- `exchange.completed`: 교환 완료
- `tokens.minted`: 토큰 민팅
- `tokens.staked`: 토큰 스테이킹
- `proposal.activated`: 프로포잘 활성화
- `vote.cast`: 투표

### 웹훅 설정

```http
POST /webhooks
```

#### 요청 본문
```json
{
  "url": "https://your-webhook-url.com/events",
  "events": ["points.earned", "exchange.completed"],
  "secret": "your-webhook-secret"
}
```

## API 버전 관리

API 버전은 URL 경로에 포함됩니다: `/api/v1/points/earn`

현재 버전: v1 