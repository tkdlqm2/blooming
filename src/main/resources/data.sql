-- Admin 사용자 생성 (비밀번호: admin123)
INSERT INTO users (id, email, username, password, role) VALUES 
('f2aec616-1dcb-4e56-923d-16e07a58ae3c', 'admin@governance.com', 'admin', '$2a$10$E2N3/289hWwfWiPrWHuutetMFIkOdbZ03OK5jBYA4WGlzybRCzmuq', 'ADMIN');

-- 솔라나 지갑 정보
INSERT INTO wallets (id, user_id, wallet_address, encrypted_private_key, network_type, active) VALUES 
('df50e3a5-0265-44a8-bede-07d134841307', 'f2aec616-1dcb-4e56-923d-16e07a58ae3c', '1111111111111111111GKcUfEEzaVazsaWLw3LNAzcnR76Dw6b6xGgSvvX7uVdJ', 'e5VDLJh2hMbScH1aH3PrPXuVQyyYdoTG0nB9Wh9z6z0n+LmM7dqMul+Wrz+OXvxQC91GI065xCqyrJZFUFe4eKMDlW0wSjm4wUxEZFpRtNc/nivR495rn0y6a/5GJa/+nzysv9kqqO8QAXA4o5lOBI9D4GolW5GbLe4vnJ1klg0=', 'SOLANA', true);

-- 새로운 이더리움 지갑 정보 (프라이빗 키: 0xa604ff9a1806e3ab62089e9427a91d69972e08b4768e97533cbe7353ef1aab77)
INSERT INTO wallets (id, user_id, wallet_address, encrypted_private_key, network_type, active) VALUES 
('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'f2aec616-1dcb-4e56-923d-16e07a58ae3c', '0x55D5c49e36f8A89111687C9DC8355121068f0cD8', 'jgTiIrbjpucJ2NUHooYdbdNj7UPGgh6wXEd1CyyPjilgjUyIPfLDc14Uc4JtKcbERMapTSfGGwIZLSDKo6MYAq3484rzkrmLSYUAIUm6hX4=', 'ETHEREUM', true);

-- 새로운 테스트 사용자 5명 생성
-- User 1: Alice
INSERT INTO users (id, email, username, password, role) VALUES 
('11111111-1111-1111-1111-111111111111', 'alice@example.com', 'alice', '$2a$10$E2N3/289hWwfWiPrWHuutetMFIkOdbZ03OK5jBYA4WGlzybRCzmuq', 'USER');

-- User 2: Bob
INSERT INTO users (id, email, username, password, role) VALUES 
('22222222-2222-2222-2222-222222222222', 'bob@example.com', 'bob', '$2a$10$E2N3/289hWwfWiPrWHuutetMFIkOdbZ03OK5jBYA4WGlzybRCzmuq', 'USER');

-- User 3: Charlie
INSERT INTO users (id, email, username, password, role) VALUES 
('33333333-3333-3333-3333-333333333333', 'charlie@example.com', 'charlie', '$2a$10$E2N3/289hWwfWiPrWHuutetMFIkOdbZ03OK5jBYA4WGlzybRCzmuq', 'USER');

-- User 4: Diana
INSERT INTO users (id, email, username, password, role) VALUES 
('44444444-4444-4444-4444-444444444444', 'diana@example.com', 'diana', '$2a$10$E2N3/289hWwfWiPrWHuutetMFIkOdbZ03OK5jBYA4WGlzybRCzmuq', 'USER');

-- User 5: Eve
INSERT INTO users (id, email, username, password, role) VALUES 
('55555555-5555-5555-5555-555555555555', 'eve@example.com', 'eve', '$2a$10$E2N3/289hWwfWiPrWHuutetMFIkOdbZ03OK5jBYA4WGlzybRCzmuq', 'USER');

-- 각 사용자의 이더리움 지갑 생성
-- Alice의 지갑
INSERT INTO wallets (id, user_id, wallet_address, encrypted_private_key, network_type, active) VALUES 
('aaaa1111-aaaa-1111-aaaa-111111111111', '11111111-1111-1111-1111-111111111111', '0x28db38d398d9ea5623c850f699a41cd32d74ed68', 'e5VDLJh2hMbScH1aH3PrPXuVQyyYdoTG0nB9Wh9z6z0n+LmM7dqMul+Wrz+OXvxQC91GI065xCqyrJZFUFe4eKMDlW0wSjm4wUxEZFpRtNc/nivR495rn0y6a/5GJa/+nzysv9kqqO8QAXA4o5lOBI9D4GolW5GbLe4vnJ1klg0=', 'ETHEREUM', true);

-- Bob의 지갑
INSERT INTO wallets (id, user_id, wallet_address, encrypted_private_key, network_type, active) VALUES 
('bbbb2222-bbbb-2222-bbbb-222222222222', '22222222-2222-2222-2222-222222222222', '0x39ec38d398d9ea5623c850f699a41cd32d74ed69', 'e5VDLJh2hMbScH1aH3PrPXuVQyyYdoTG0nB9Wh9z6z0n+LmM7dqMul+Wrz+OXvxQC91GI065xCqyrJZFUFe4eKMDlW0wSjm4wUxEZFpRtNc/nivR495rn0y6a/5GJa/+nzysv9kqqO8QAXA4o5lOBI9D4GolW5GbLe4vnJ1klg0=', 'ETHEREUM', true);

-- Charlie의 지갑
INSERT INTO wallets (id, user_id, wallet_address, encrypted_private_key, network_type, active) VALUES 
('cccc3333-cccc-3333-cccc-333333333333', '33333333-3333-3333-3333-333333333333', '0x4a0d38d398d9ea5623c850f699a41cd32d74ed6a', 'e5VDLJh2hMbScH1aH3PrPXuVQyyYdoTG0nB9Wh9z6z0n+LmM7dqMul+Wrz+OXvxQC91GI065xCqyrJZFUFe4eKMDlW0wSjm4wUxEZFpRtNc/nivR495rn0y6a/5GJa/+nzysv9kqqO8QAXA4o5lOBI9D4GolW5GbLe4vnJ1klg0=', 'ETHEREUM', true);

-- Diana의 지갑
INSERT INTO wallets (id, user_id, wallet_address, encrypted_private_key, network_type, active) VALUES 
('dddd4444-dddd-4444-dddd-444444444444', '44444444-4444-4444-4444-444444444444', '0x5b1e38d398d9ea5623c850f699a41cd32d74ed6b', 'e5VDLJh2hMbScH1aH3PrPXuVQyyYdoTG0nB9Wh9z6z0n+LmM7dqMul+Wrz+OXvxQC91GI065xCqyrJZFUFe4eKMDlW0wSjm4wUxEZFpRtNc/nivR495rn0y6a/5GJa/+nzysv9kqqO8QAXA4o5lOBI9D4GolW5GbLe4vnJ1klg0=', 'ETHEREUM', true);

-- Eve의 지갑
INSERT INTO wallets (id, user_id, wallet_address, encrypted_private_key, network_type, active) VALUES 
('eeee5555-eeee-5555-eeee-555555555555', '55555555-5555-5555-5555-555555555555', '0x6c2f38d398d9ea5623c850f699a41cd32d74ed6c', 'e5VDLJh2hMbScH1aH3PrPXuVQyyYdoTG0nB9Wh9z6z0n+LmM7dqMul+Wrz+OXvxQC91GI065xCqyrJZFUFe4eKMDlW0wSjm4wUxEZFpRtNc/nivR495rn0y6a/5GJa/+nzysv9kqqO8QAXA4o5lOBI9D4GolW5GbLe4vnJ1klg0=', 'ETHEREUM', true);

-- 각 사용자의 포인트 계정 생성 (각각 1000 포인트)
-- Alice의 포인트 계정
INSERT INTO point_accounts (id, user_id, balance, frozen_balance) VALUES 
('11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 1000, 0);

-- Bob의 포인트 계정
INSERT INTO point_accounts (id, user_id, balance, frozen_balance) VALUES 
('22222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222', 1000, 0);

-- Charlie의 포인트 계정
INSERT INTO point_accounts (id, user_id, balance, frozen_balance) VALUES 
('33333333-3333-3333-3333-333333333333', '33333333-3333-3333-3333-333333333333', 1000, 0);

-- Diana의 포인트 계정
INSERT INTO point_accounts (id, user_id, balance, frozen_balance) VALUES 
('44444444-4444-4444-4444-444444444444', '44444444-4444-4444-4444-444444444444', 1000, 0);

-- Eve의 포인트 계정
INSERT INTO point_accounts (id, user_id, balance, frozen_balance) VALUES 
('55555555-5555-5555-5555-555555555555', '55555555-5555-5555-5555-555555555555', 1000, 0);

-- 각 사용자의 포인트 트랜잭션 기록 (무료 포인트 지급)
-- Alice의 포인트 트랜잭션
INSERT INTO point_transactions (id, user_id, amount, type, reason, created_at) VALUES 
('11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 1000, 'FREE_RECEIVE', '무료 포인트 지급', CURRENT_TIMESTAMP);

-- Bob의 포인트 트랜잭션
INSERT INTO point_transactions (id, user_id, amount, type, reason, created_at) VALUES 
('22222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222', 1000, 'FREE_RECEIVE', '무료 포인트 지급', CURRENT_TIMESTAMP);

-- Charlie의 포인트 트랜잭션
INSERT INTO point_transactions (id, user_id, amount, type, reason, created_at) VALUES 
('33333333-3333-3333-3333-333333333333', '33333333-3333-3333-3333-333333333333', 1000, 'FREE_RECEIVE', '무료 포인트 지급', CURRENT_TIMESTAMP);

-- Diana의 포인트 트랜잭션
INSERT INTO point_transactions (id, user_id, amount, type, reason, created_at) VALUES 
('44444444-4444-4444-4444-444444444444', '44444444-4444-4444-4444-444444444444', 1000, 'FREE_RECEIVE', '무료 포인트 지급', CURRENT_TIMESTAMP);

-- Eve의 포인트 트랜잭션
INSERT INTO point_transactions (id, user_id, amount, type, reason, created_at) VALUES 
('55555555-5555-5555-5555-555555555555', '55555555-5555-5555-5555-555555555555', 1000, 'FREE_RECEIVE', '무료 포인트 지급', CURRENT_TIMESTAMP); 