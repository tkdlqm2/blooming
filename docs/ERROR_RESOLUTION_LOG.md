# 에러 해결 과정 문서

## 개요
이 문서는 Solana 기반 거버넌스 토큰 플랫폼 개발 과정에서 발생한 모든 에러들과 그 해결 과정을 기록합니다.

## 1. JPA/Hibernate 매핑 관련 에러

### 1.1 TokenAccountJpaRepository existsByWalletAddress 에러
**에러 메시지:**
```
Could not create query for public abstract boolean com.bloominggrace.governance.token.infrastructure.repository.TokenAccountJpaRepository.existsByWalletAddress(java.lang.String); No property 'walletAddress' found for type 'TokenAccount'
```

**원인:**
- TokenAccount 엔티티에서 `walletAddress` 필드가 `walletId`로 변경되었는데, TokenAccountJpaRepository에 `existsByWalletAddress` 메서드가 남아있어서 Spring Data JPA가 쿼리 생성을 시도하다가 실패

**해결 과정:**
1. TokenAccountJpaRepository 파일이 존재하지 않음을 확인
2. 빌드 캐시/클래스 파일 삭제 후 재빌드 필요
3. `./gradlew clean build` 실행으로 해결

### 1.2 User/Wallet 순환 참조 무한 재귀 에러
**에러 메시지:**
```
Could not write JSON: Document nesting depth (1001) exceeds the maximum allowed (1000, from `StreamWriteConstraints.getMaxNestingDepth()`)
```

**원인:**
- User → Wallet → User → Wallet 구조로 DTO 변환 시 순환 참조 발생
- WalletDto의 from 메서드에서 User 엔티티 전체를 포함하여 무한 재귀 발생

**해결 과정:**
1. **UserDto 수정:**
   ```java
   public static UserDto from(User user) {
       return new UserDto(
           user.getId(),
           user.getEmail(),
           user.getUsername(),
           user.getRole(),
           user.getWallets() == null ? List.of() : user.getWallets().stream()
               .map(WalletDto::from)
               .collect(java.util.stream.Collectors.toList())
       );
   }
   ```

2. **WalletDto 수정:**
   ```java
   public static WalletDto from(Wallet wallet) {
       return new WalletDto(
           wallet.getId(),
           wallet.getUser() != null ? wallet.getUser().getId() : null,
           wallet.getWalletAddress(),
           wallet.getNetworkType(),
           wallet.isActive()
       );
   }
   ```

3. **UserController 수정:**
   ```java
   @GetMapping("/{id}")
   public ResponseEntity<UserDto> getUser(@PathVariable UUID id) {
       User user = userService.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
       return ResponseEntity.ok(UserDto.from(user));
   }
   ```

## 2. 컴파일 에러

### 2.1 타입 불일치 에러
**에러 메시지:**
```
error: incompatible types: NetworkType cannot be converted to UUID
error: incompatible types: List<Object> cannot be converted to List<WalletDto>
```

**원인:**
- UserController에서 WalletDto 변환 시 NetworkType을 UUID로 넘기는 실수
- WalletDto.from 메서드의 파라미터 순서와 타입 불일치

**해결 과정:**
1. WalletDto 생성자 정의 추가:
   ```java
   public WalletDto(UUID id, UUID userId, String walletAddress, NetworkType networkType, boolean isActive) {
       this.id = id;
       this.userId = userId;
       this.walletAddress = walletAddress;
       this.networkType = networkType;
       this.isActive = isActive;
   }
   ```

2. UserController에서 WalletDto.from 사용하도록 수정

### 2.2 Optional 처리 에러
**에러 메시지:**
```
error: cannot find symbol: method orElseThrow
```

**원인:**
- UserService.findById()가 Optional<User>를 반환하는데 UserController에서 Optional 처리를 하지 않음

**해결 과정:**
```java
User user = userService.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
```

## 3. 암호화 서비스 에러

### 3.1 AESEncryptionService 키 길이 에러
**에러 메시지:**
```
java.lang.IllegalArgumentException: Encryption key must be exactly 32 bytes (256 bits)
```

**원인:**
- application.yml의 암호화 키가 32바이트가 아님

**해결 과정:**
1. application.yml 수정:
   ```yaml
   encryption:
     key: "12345678901234567890123456789012"  # 정확히 32바이트
   ```

2. AESEncryptionService에서 키 길이 검증 로직 확인

## 4. 포트 충돌 에러

### 4.1 포트 8081 사용 중 에러
**에러 메시지:**
```
Web server failed to start. Port 8081 was already in use.
```

**원인:**
- 이전 실행된 애플리케이션 프로세스가 포트 8081을 점유

**해결 과정:**
1. 포트 사용 프로세스 확인:
   ```bash
   lsof -ti:8081
   ```

2. 프로세스 종료:
   ```bash
   lsof -ti:8081 | xargs kill -9
   ```

3. 애플리케이션 재시작:
   ```bash
   ./gradlew bootRun
   ```

## 5. Spring Security 설정 에러

### 5.1 403 Forbidden 에러
**에러 메시지:**
```
HTTP 403 Forbidden
```

**원인:**
- Spring Security 설정에서 특정 API 엔드포인트가 차단됨

**해결 과정:**
1. SecurityConfig에서 wallet API 엔드포인트 허용:
   ```java
   .requestMatchers("/api/wallets/**").permitAll()
   ```

## 6. Lombok 관련 에러

### 6.1 Lombok 어노테이션 충돌
**에러 메시지:**
```
error: cannot find symbol: method builder()
```

**원인:**
- Lombok @Builder 어노테이션을 적용했지만 일부 클래스에서 제거되어 builder() 메서드가 없음

**해결 과정:**
1. Lombok 어노테이션 일관성 확인
2. 필요한 경우 수동으로 생성자와 getter/setter 추가
3. WalletDto에서 Lombok 어노테이션 제거하고 수동 구현

## 7. 빌드 및 실행 성공 확인

### 7.1 최종 빌드 성공
```bash
./gradlew clean build
BUILD SUCCESSFUL
```

### 7.2 애플리케이션 실행 성공
```bash
./gradlew bootRun
Started GovernanceApplication in 2.094 seconds
```

### 7.3 API 테스트 성공
- Health endpoint: `curl http://localhost:8081/actuator/health`
- User signup: POST `/api/auth/signup`
- User login: POST `/api/auth/login`
- Wallet creation: POST `/api/wallets`
- User info: GET `/api/users/{id}`

## 8. 주요 해결 원칙

### 8.1 DTO 패턴 적용
- 엔티티 간 순환 참조 방지를 위해 DTO 패턴 사용
- API 응답에서 엔티티 직접 반환 금지
- UserDto와 WalletDto에서 서로 참조하지 않고 ID만 포함

### 8.2 계층 분리
- Domain, Application, Infrastructure 계층 명확히 분리
- Repository Adapter 패턴으로 JPA와 도메인 계층 분리
- Service 계층에서 비즈니스 로직 처리

### 8.3 타입 안전성
- Optional 타입 적절히 처리
- 컴파일 타임에 타입 검증
- 명시적 타입 변환 사용

### 8.4 설정 관리
- application.yml에서 환경별 설정 분리
- 암호화 키 등 민감한 정보는 환경변수 사용 권장
- 개발/운영 환경 설정 분리

## 9. 향후 개선 사항

### 9.1 에러 처리 개선
- Global Exception Handler 구현
- 표준화된 에러 응답 형식 정의
- 로깅 레벨 적절히 설정

### 9.2 보안 강화
- JWT 토큰 만료 시간 설정
- 비밀번호 정책 강화
- API 인증/인가 체계 완성

### 9.3 테스트 코드 작성
- Unit Test 작성
- Integration Test 작성
- API Test 작성

## 10. 결론

이 문서에 기록된 에러 해결 과정을 통해 다음과 같은 교훈을 얻을 수 있습니다:

1. **순환 참조 문제**: DTO 패턴을 일관되게 적용하여 해결
2. **타입 안전성**: 컴파일 타임에 타입 검증을 통해 런타임 에러 방지
3. **설정 관리**: 환경별 설정 분리와 적절한 값 검증
4. **계층 분리**: 명확한 아키텍처 패턴 적용으로 유지보수성 향상

이러한 해결 과정을 통해 안정적이고 확장 가능한 거버넌스 토큰 플랫폼을 구축할 수 있었습니다. 