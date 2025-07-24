# 배포 가이드

## 개요

이 문서는 Solana 기반 거버넌스 토큰 플랫폼의 배포 방법을 설명합니다.

## 환경 요구사항

### 필수 요구사항

- **Java**: 17 이상
- **Gradle**: 7.0 이상
- **PostgreSQL**: 12 이상 (운영 환경)
- **Redis**: 6.0 이상 (선택사항)
- **Docker**: 20.0 이상 (컨테이너 배포 시)

### 권장 사양

- **CPU**: 4코어 이상
- **메모리**: 8GB 이상
- **디스크**: 100GB 이상 (SSD 권장)
- **네트워크**: 100Mbps 이상

## 개발 환경 설정

### 1. 로컬 개발 환경

```bash
# 프로젝트 클론
git clone <repository-url>
cd governance

# 의존성 설치
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

### 2. 개발 환경 설정 파일

`src/main/resources/application-dev.yml`:

```yaml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:h2:mem:governance
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  h2:
    console:
      enabled: true
      path: /h2-console

logging:
  level:
    com.bloominggrace.governance: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

governance:
  exchange:
    rate: 1.0
  voting:
    minimum-participation: 0.1
```

## 운영 환경 설정

### 1. 운영 환경 설정 파일

`src/main/resources/application-prod.yml`:

```yaml
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:governance}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

logging:
  level:
    com.bloominggrace.governance: INFO
    org.springframework.security: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/governance.log
    max-size: 100MB
    max-history: 30

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized

governance:
  exchange:
    rate: ${EXCHANGE_RATE:1.0}
  voting:
    minimum-participation: ${MIN_VOTING_PARTICIPATION:0.1}
  solana:
    network: ${SOLANA_NETWORK:devnet}
    rpc-url: ${SOLANA_RPC_URL:https://api.devnet.solana.com}
    program-id: ${SOLANA_PROGRAM_ID:}
```

### 2. 환경 변수 설정

```bash
# 데이터베이스 설정
export DB_HOST=your-db-host
export DB_PORT=5432
export DB_NAME=governance
export DB_USERNAME=governance_user
export DB_PASSWORD=secure_password

# Redis 설정
export REDIS_HOST=your-redis-host
export REDIS_PORT=6379
export REDIS_PASSWORD=redis_password

# Solana 설정
export SOLANA_NETWORK=mainnet-beta
export SOLANA_RPC_URL=https://api.mainnet-beta.solana.com
export SOLANA_PROGRAM_ID=your_program_id

# 애플리케이션 설정
export EXCHANGE_RATE=1.0
export MIN_VOTING_PARTICIPATION=0.1
export SERVER_PORT=8080
```

## Docker 배포

### 1. Dockerfile

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# Gradle 빌드 결과물 복사
COPY build/libs/*.jar app.jar

# 애플리케이션 사용자 생성
RUN addgroup --system app && adduser --system --ingroup app app

# 파일 권한 설정
RUN chown -R app:app /app
USER app

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Docker Compose

`docker-compose.yml`:

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=governance
      - DB_USERNAME=governance_user
      - DB_PASSWORD=secure_password
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    depends_on:
      - postgres
      - redis
    restart: unless-stopped
    volumes:
      - ./logs:/app/logs

  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=governance
      - POSTGRES_USER=governance_user
      - POSTGRES_PASSWORD=secure_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    command: redis-server --requirepass redis_password
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - app
    restart: unless-stopped

volumes:
  postgres_data:
  redis_data:
```

### 3. Nginx 설정

`nginx.conf`:

```nginx
events {
    worker_connections 1024;
}

http {
    upstream governance_backend {
        server app:8080;
    }

    server {
        listen 80;
        server_name your-domain.com;
        return 301 https://$server_name$request_uri;
    }

    server {
        listen 443 ssl http2;
        server_name your-domain.com;

        ssl_certificate /etc/nginx/ssl/cert.pem;
        ssl_certificate_key /etc/nginx/ssl/key.pem;
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
        ssl_prefer_server_ciphers off;

        location / {
            proxy_pass http://governance_backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        location /actuator/health {
            proxy_pass http://governance_backend;
            access_log off;
        }
    }
}
```

## Kubernetes 배포

### 1. ConfigMap

`configmap.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: governance-config
data:
  application-prod.yml: |
    spring:
      datasource:
        url: jdbc:postgresql://postgres:5432/governance
        username: governance_user
        password: secure_password
      redis:
        host: redis
        port: 6379
    governance:
      exchange:
        rate: 1.0
      voting:
        minimum-participation: 0.1
```

### 2. Secret

`secret.yaml`:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: governance-secret
type: Opaque
data:
  db-password: c2VjdXJlX3Bhc3N3b3Jk  # base64 encoded
  redis-password: cmVkaXNfcGFzc3dvcmQ=  # base64 encoded
  solana-private-key: <base64-encoded-private-key>
```

### 3. Deployment

`deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: governance-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: governance
  template:
    metadata:
      labels:
        app: governance
    spec:
      containers:
      - name: governance
        image: governance:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: governance-secret
              key: db-password
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: governance-secret
              key: redis-password
        volumeMounts:
        - name: config
          mountPath: /app/config
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
      volumes:
      - name: config
        configMap:
          name: governance-config
```

### 4. Service

`service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: governance-service
spec:
  selector:
    app: governance
  ports:
  - port: 80
    targetPort: 8080
  type: ClusterIP
```

### 5. Ingress

`ingress.yaml`:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: governance-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
  - hosts:
    - your-domain.com
    secretName: governance-tls
  rules:
  - host: your-domain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: governance-service
            port:
              number: 80
```

## 데이터베이스 마이그레이션

### 1. Flyway 설정

`build.gradle`에 추가:

```gradle
dependencies {
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-database-postgresql'
}
```

### 2. 마이그레이션 스크립트

`src/main/resources/db/migration/V1__Create_initial_schema.sql`:

```sql
-- Point Management Context
CREATE TABLE point_accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    available_amount DECIMAL(20,8) NOT NULL DEFAULT 0,
    frozen_amount DECIMAL(20,8) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_point_accounts_user_id ON point_accounts(user_id);

-- Token Management Context
CREATE TABLE token_accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    wallet_address VARCHAR(44) NOT NULL,
    available_amount DECIMAL(20,8) NOT NULL DEFAULT 0,
    staked_amount DECIMAL(20,8) NOT NULL DEFAULT 0,
    locked_amount DECIMAL(20,8) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_token_accounts_user_id ON token_accounts(user_id);
CREATE INDEX idx_token_accounts_wallet_address ON token_accounts(wallet_address);
CREATE UNIQUE INDEX uk_token_accounts_user_id ON token_accounts(user_id);
```

## 모니터링 및 로깅

### 1. Prometheus 메트릭

`build.gradle`에 추가:

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'io.micrometer:micrometer-registry-prometheus'
}
```

### 2. 로그 수집

ELK Stack 또는 Grafana Loki를 사용하여 로그 수집:

```yaml
# logback-spring.xml
<configuration>
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <loggerName/>
                <message/>
                <mdc/>
                <stackTrace/>
            </providers>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="JSON"/>
    </root>
</configuration>
```

## 보안 설정

### 1. SSL/TLS 인증서

Let's Encrypt를 사용한 무료 SSL 인증서:

```bash
# Certbot 설치
sudo apt-get install certbot python3-certbot-nginx

# 인증서 발급
sudo certbot --nginx -d your-domain.com
```

### 2. 방화벽 설정

```bash
# UFW 설정
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

### 3. 데이터베이스 보안

```sql
-- PostgreSQL 사용자 생성
CREATE USER governance_user WITH PASSWORD 'secure_password';
GRANT CONNECT ON DATABASE governance TO governance_user;
GRANT USAGE ON SCHEMA public TO governance_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO governance_user;
```

## 백업 및 복구

### 1. 데이터베이스 백업

```bash
#!/bin/bash
# backup.sh
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/postgres"
DB_NAME="governance"

mkdir -p $BACKUP_DIR
pg_dump -h localhost -U governance_user -d $DB_NAME > $BACKUP_DIR/backup_$DATE.sql

# 30일 이상 된 백업 삭제
find $BACKUP_DIR -name "backup_*.sql" -mtime +30 -delete
```

### 2. 자동 백업 스케줄

```bash
# crontab 설정
0 2 * * * /path/to/backup.sh
```

## 성능 튜닝

### 1. JVM 튜닝

```bash
# JVM 옵션
JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication"
```

### 2. 데이터베이스 튜닝

```sql
-- PostgreSQL 설정
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;
```

## 트러블슈팅

### 1. 일반적인 문제

- **메모리 부족**: JVM 힙 크기 증가
- **데이터베이스 연결 실패**: 연결 풀 설정 확인
- **Redis 연결 실패**: Redis 서버 상태 확인

### 2. 로그 분석

```bash
# 애플리케이션 로그 확인
tail -f logs/governance.log

# 에러 로그 필터링
grep "ERROR" logs/governance.log

# 성능 로그 확인
grep "slow query" logs/governance.log
```

### 3. 헬스체크

```bash
# 애플리케이션 상태 확인
curl http://localhost:8080/actuator/health

# 데이터베이스 연결 확인
curl http://localhost:8080/actuator/health/db

# Redis 연결 확인
curl http://localhost:8080/actuator/health/redis
``` 