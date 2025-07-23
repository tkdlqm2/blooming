-- 기존 데이터 삭제 (필요한 경우)
-- DELETE FROM token_transactions;
-- DELETE FROM token_accounts;
-- DELETE FROM wallets;
-- DELETE FROM point_transactions;
-- DELETE FROM point_accounts;
-- DELETE FROM users;

-- 1. 사용자 계정 먼저 생성 (외래 키 참조를 위해)
INSERT INTO users (id, username, email, password, role) VALUES 
('f2aec616-1dcb-4e56-923d-16e07a58ae3c', 'admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'ADMIN');

-- 2. 새로운 이더리움 지갑 정보 (프라이빗 키: 0xa604ff9a1806e3ab62089e9427a91d69972e08b4768e97533cbe7353ef1aab77)
INSERT INTO wallets (id, user_id, wallet_address, encrypted_private_key, network_type, active) VALUES 
('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'f2aec616-1dcb-4e56-923d-16e07a58ae3c', '0x55D5c49e36f8A89111687C9DC8355121068f0cD8', 'jgTiIrbjpucJ2NUHooYdbdNj7UPGgh6wXEd1CyyPjilgjUyIPfLDc14Uc4JtKcbERMapTSfGGwIZLSDKo6MYAq3484rzkrmLSYUAIUm6hX4=', 'ETHEREUM', true);

-- 3. 포인트 계정 (어드민 계정용) - 자동 생성된 UUID 사용
INSERT INTO point_accounts (id, user_id, balance, frozen_balance, version) VALUES 
(RANDOM_UUID(), 'f2aec616-1dcb-4e56-923d-16e07a58ae3c', 1000000, 0, 0);

-- 4. 포인트 트랜잭션 기록 - PointTransactionType.EARN 사용 (EARNED가 아님)
INSERT INTO point_transactions (id, user_id, type, amount, reason, created_at) VALUES 
(RANDOM_UUID(), 'f2aec616-1dcb-4e56-923d-16e07a58ae3c', 'EARN', 1000000, 'Admin 계정 초기 포인트 지급', CURRENT_TIMESTAMP);

-- 5. 어드민 계정의 토큰 계정 생성 (1,000,000 토큰) - 실제 컨트랙트 주소 사용
-- TokenAccount 엔티티의 모든 필드를 정확히 매핑
INSERT INTO token_accounts (
    id, 
    wallet_id, 
    user_id, 
    network, 
    contract, 
    symbol, 
    total_balance, 
    available_balance, 
    wallet_address, 
    is_active, 
    created_at, 
    updated_at
) VALUES (
    RANDOM_UUID(), 
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 
    'f2aec616-1dcb-4e56-923d-16e07a58ae3c', 
    'ETHEREUM', 
    '0xeafF00556BC06464511319dAb26D6CAC148b89d0', 
    'TOKEN', 
    1000000.00, 
    1000000.00, 
    '0x55D5c49e36f8A89111687C9DC8355121068f0cD8', 
    true, 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
);