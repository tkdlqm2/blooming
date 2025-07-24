# 🚀 블록체인 기반 재화 교환 서비스 - 설치 및 실행 가이드

## �� 사전 요구사항

### **시스템 요구사항**
- **OS**: macOS 10.15+ / Ubuntu 20.04+ / Windows 10+
- **Java**: OpenJDK 17+
- **Node.js**: 18+ (스마트 컨트랙트 배포용)
- **메모리**: 최소 8GB RAM (권장 16GB)
- **디스크**: 최소 10GB 여유 공간

### **개발 도구**
- **IDE**: IntelliJ IDEA 2023.1+ (권장)
- **에디터**: VS Code (선택사항)
- **Git**: 2.30+

---

## 🔧 설치 가이드

### **1. Java 설치**

#### **macOS (Homebrew)**
```bash
# Homebrew 설치 (없는 경우)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# OpenJDK 17 설치
brew install openjdk@17

# 환경변수 설정
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
echo 'export JAVA_HOME="/opt/homebrew/opt/openjdk@17"' >> ~/.zshrc
source ~/.zshrc
```

#### **Ubuntu/Debian**
```bash
# 패키지 업데이트
sudo apt update

# OpenJDK 17 설치
sudo apt install openjdk-17-jdk

# 환경변수 설정
echo 'export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"' >> ~/.bashrc
source ~/.bashrc
```

#### **Windows**
```bash
# Chocolatey 사용
choco install openjdk17

# 또는 수동 설치: https://adoptium.net/temurin/releases/
```

### **2. Node.js 설치**

#### **macOS**
```bash
# Node.js 18 설치
brew install node@18

# npm 업데이트
npm install -g npm@latest
```

#### **Ubuntu/Debian**
```bash
# NodeSource 저장소 추가
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -

# Node.js 설치
sudo apt-get install -y nodejs
```

### **3. 프로젝트 클론**

```bash
git clone https://github.com/your-username/governance.git
```
```bash
# 현재 디렉토리에서 시작 (이미 프로젝트가 있는 경우)
cd /Users/okestro/Downloads/governance

# Git 초기화 (새 프로젝트인 경우)
git init
git add .
git commit -m "Initial commit"
```

---

## �� 컴파일 및 빌드

### **1. Gradle Wrapper 권한 설정**

```bash
chmod +x gradlew
```
### **2. 의존성 다운로드**

```bash
./gradlew dependencies
```
### **3. 컴파일**

```bash
./gradlew compileJava
```
### **4. 테스트 컴파일**

```bash
./gradlew compileTestJava
```
테스트 파일의 import 경로를 수정해야 합니다.

이제 다시 테스트 컴파일을 시도해보겠습니다.

```bash
./gradlew compileTestJava
```
### **5. 전체 빌드**

```bash
./gradlew build
```
---

## �� 실행 가이드

### **1. 애플리케이션 실행**

```bash
./gradlew bootRun
```
### **2. JAR 파일로 실행**

```bash
java -jar build/libs/governance-0.0.1-SNAPSHOT.jar
```
포트 8081이 이미 사용 중입니다. 다른 포트로 실행하거나 기존 프로세스를 종료해야 합니다.

```bash
lsof -ti:8081 | xargs kill -9
```
이제 다시 실행해보겠습니다.

```bash
java -jar build/libs/governance-0.0.1-SNAPSHOT.jar
```
---

## 🧪 테스트 가이드

### **1. 단위 테스트 실행**

```bash
./gradlew test
```
### **2. 특정 테스트 실행**

```bash
./gradlew test --tests "*GovernanceApplicationTests*"
```
### **3. 통합 테스트 실행**

```bash
./gradlew integrationTest
```
### **4. 사용 가능한 Gradle 태스크 확인**

```bash
./gradlew tasks
```
---

## 📋 완전한 설치 및 실행 가이드

### **1. 환경 설정**

#### **Java 17 설치 확인**
```bash
java -version
# OpenJDK 64-Bit Server VM (build 17.0.14+9, mixed mode, sharing)
```

#### **Node.js 18+ 설치 확인**
```bash
node --version
# v18.19.0
npm --version
# 10.2.4
```

### **2. 프로젝트 설정**

#### **프로젝트 클론 및 이동**
```bash
cd /Users/okestro/Downloads/governance
```

#### **Gradle Wrapper 권한 설정**
```bash
chmod +x gradlew
```

### **3. 빌드 및 실행**

#### **의존성 다운로드**
```bash
./gradlew dependencies
```

#### **컴파일**
```bash
./gradlew compileJava
./gradlew compileTestJava
```

#### **전체 빌드**
```bash
./gradlew build
```

#### **애플리케이션 실행**
```bash
# 방법 1: Gradle로 실행
./gradlew bootRun

# 방법 2: JAR 파일로 실행
java -jar build/libs/governance-0.0.1-SNAPSHOT.jar

# 방법 3: 특정 포트로 실행
java -jar build/libs/governance-0.0.1-SNAPSHOT.jar --server.port=8082
```

### **4. 테스트 실행**

#### **모든 테스트**
```bash
./gradlew test
```

#### **특정 테스트 클래스**
```bash
./gradlew test --tests "*TestClassName*"
```

#### **테스트 리포트 확인**
```bash
# HTML 리포트 생성
./gradlew test --rerun-tasks
open build/reports/tests/test/index.html
```

### **5. 애플리케이션 접속**

#### **API 문서**
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **API Endpoints**: http://localhost:8081/api/v1

#### **H2 데이터베이스 콘솔**
- **URL**: http://localhost:8081/h2-console
- **JDBC URL**: `jdbc:h2:mem:governance`
- **Username**: `sa`
- **Password**: (비어있음)

### **6. 개발 환경 설정**

#### **IntelliJ IDEA 설정**
1. **프로젝트 열기**: `File > Open > governance 폴더 선택`
2. **Gradle 프로젝트 동기화**: `View > Tool Windows > Gradle`
3. **Run Configuration 설정**:
    - **Main class**: `com.bloominggrace.governance.GovernanceApplication`
    - **VM options**: `-Dspring.profiles.active=dev`
    - **Program arguments**: `--server.port=8081`

#### **VS Code 설정**
1. **확장 프로그램 설치**:
    - Extension Pack for Java
    - Spring Boot Extension Pack
    - Gradle for Java
2. **설정 파일 생성**: `.vscode/settings.json`
```json
{
    "java.configuration.updateBuildConfiguration": "automatic",
    "java.compile.nullAnalysis.mode": "automatic"
}
```

### **7. 문제 해결**

#### **포트 충돌 해결**
```bash
# 포트 사용 중인 프로세스 확인
lsof -i :8081

# 프로세스 종료
lsof -ti:8081 | xargs kill -9
```

#### **메모리 부족 해결**
```bash
# JVM 힙 메모리 증가
java -Xmx2g -jar build/libs/governance-0.0.1-SNAPSHOT.jar
```

#### **로그 레벨 설정**
```bash
# 디버그 모드로 실행
java -jar build/libs/governance-0.0.1-SNAPSHOT.jar --logging.level.com.bloominggrace=DEBUG
```

### **8. 프로덕션 배포**

#### **Docker 이미지 빌드**
```bash
./gradlew bootBuildImage
```

#### **Docker 실행**
```bash
docker run -p 8081:8081 governance:0.0.1-SNAPSHOT
```

#### **환경 변수 설정**
```bash
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:postgresql://localhost:5432/governance
export JWT_SECRET=your-secret-key
java -jar build/libs/governance-0.0.1-SNAPSHOT.jar
```

---

## �� 빠른 시작 체크리스트

http://localhost:8081/swagger-ui/index.html#/
이제 블록체인 기반 재화 교환 서비스를 성공적으로 설치하고 실행할 수 있습니다! 🚀