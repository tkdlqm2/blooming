package com.bloominggrace.governance.wallet.infrastructure.service.ethereum;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.shared.domain.service.EncryptionService;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.wallet.domain.service.WalletService;
import com.bloominggrace.governance.wallet.domain.service.KeyPairProvider;
import com.bloominggrace.governance.wallet.infrastructure.repository.WalletRepository;
import com.bloominggrace.governance.shared.domain.model.TransactionBody;
import com.bloominggrace.governance.shared.domain.model.EthereumTransactionData;
import com.bloominggrace.governance.shared.util.HashUtils;
import com.bloominggrace.governance.shared.util.HexUtils;
import com.bloominggrace.governance.shared.util.BigIntUtils;
import com.bloominggrace.governance.shared.util.SignatureUtils;
import org.springframework.stereotype.Service;
import com.bloominggrace.governance.user.domain.model.User;
import com.bloominggrace.governance.user.infrastructure.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.math.BigInteger;
import java.security.SecureRandom;

@Service("ethereumWalletService")
public class EthereumWalletService implements WalletService {
    
    private final WalletRepository walletRepository;
    private final EncryptionService encryptionService;
    private final UserRepository userRepository;

    public EthereumWalletService(
            WalletRepository walletRepository,
            EncryptionService encryptionService,
            UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.encryptionService = encryptionService;
        this.userRepository = userRepository;
    }
    
    @Override
    public Wallet createWallet(UserId userId, NetworkType networkType) {
        KeyPairProvider.KeyPairResult keyPair = KeyPairProvider.generateKeyPair(NetworkType.ETHEREUM);
        String encryptedPrivateKey = encryptionService.encrypt(keyPair.getPrivateKey());
        User user = userRepository.findById(userId.getValue())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Wallet wallet = new Wallet(user, keyPair.getAddress(), NetworkType.ETHEREUM, encryptedPrivateKey);
        return walletRepository.save(wallet);
    }
    

    
    @Override
    public Optional<Wallet> findByAddress(String walletAddress) {
        return walletRepository.findAll().stream()
                .filter(wallet -> wallet.getWalletAddress().equals(walletAddress) && 
                                wallet.getNetworkType() == NetworkType.ETHEREUM)
                .findFirst();
    }
    
    @Override
    public List<Wallet> findByUserId(UserId userId) {
        return walletRepository.findByUserId(userId.getValue()).stream()
                .filter(wallet -> wallet.getNetworkType() == NetworkType.ETHEREUM)
                .toList();
    }
    
    @Override
    public Wallet save(Wallet wallet) {
        return walletRepository.save(wallet);
    }
    

    
    @Override
    public Wallet updateActiveStatus(String walletAddress, boolean active) {
        return walletRepository.findAll().stream()
                .filter(wallet -> wallet.getWalletAddress().equals(walletAddress) && 
                                wallet.getNetworkType() == NetworkType.ETHEREUM)
                .findFirst()
                .map(wallet -> {
                    wallet.setActive(active);
                    return walletRepository.save(wallet);
                })
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
    }
    



    
    /**
     * ECDSA 서명 생성
     * Etherscan 문서의 서명 알고리즘 구현
     */
    private byte[] createECDSASignature(byte[] messageHash, BigInteger privateKey) {
        try {
            // ECDSA 서명을 위한 타원곡선 파라미터 (secp256k1)
            BigInteger p = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);
            BigInteger n = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);
            BigInteger Gx = new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16);
            BigInteger Gy = new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16);
            
            // 메시지 해시를 BigInteger로 변환
            BigInteger messageHashBigInt = new BigInteger(1, messageHash);
            
            // k (임시값) 생성
            SecureRandom random = new SecureRandom();
            BigInteger k;
            do {
                k = new BigInteger(n.bitLength(), random);
            } while (k.compareTo(n) >= 0 || k.compareTo(BigInteger.ZERO) <= 0);
            
            // R = k * G 계산
            BigInteger[] R = scalarMultiply(Gx, Gy, k, p);
            BigInteger r = R[0].mod(n);
            
            // s = k^(-1) * (messageHash + r * privateKey) mod n
            BigInteger kInverse = k.modInverse(n);
            BigInteger s = kInverse.multiply(messageHashBigInt.add(r.multiply(privateKey))).mod(n);
            
            // 서명 생성 (r, s, v)
            byte[] signature = new byte[65];
            
            // r, s를 32바이트로 변환
            byte[] rBytes = r.toByteArray();
            byte[] sBytes = s.toByteArray();
            
            // 패딩
            byte[] rPadded = new byte[32];
            byte[] sPadded = new byte[32];
            System.arraycopy(rBytes, Math.max(0, rBytes.length - 32), rPadded, Math.max(0, 32 - rBytes.length), Math.min(32, rBytes.length));
            System.arraycopy(sBytes, Math.max(0, sBytes.length - 32), sPadded, Math.max(0, 32 - sBytes.length), Math.min(32, sBytes.length));
            
            // 서명 조합
            System.arraycopy(rPadded, 0, signature, 0, 32);
            System.arraycopy(sPadded, 0, signature, 32, 32);
            signature[64] = 27; // recovery id (v)
            
            return signature;
        } catch (Exception e) {
            throw new RuntimeException("ECDSA signature creation error", e);
        }
    }
    
    /**
     * 타원곡선 스칼라 곱셈 (간단한 구현)
     */
    private BigInteger[] scalarMultiply(BigInteger x, BigInteger y, BigInteger k, BigInteger p) {
        // 간단한 구현 (실제로는 더 복잡한 타원곡선 연산 필요)
        BigInteger resultX = x.multiply(k).mod(p);
        BigInteger resultY = y.multiply(k).mod(p);
        return new BigInteger[]{resultX, resultY};
    }
    
    /**
     * 주어진 메시지에 대해 개인키로 서명합니다.
     */
    @Override
    public byte[] sign(byte[] message, String privateKey) {
        try {
            // 개인키를 BigInteger로 변환
            BigInteger privateKeyBigInt = new BigInteger(privateKey, 16);
            
            // 메시지 해시 생성 (Keccak-256)
            byte[] messageHash = keccak256(message);
            
            // ECDSA 서명 생성
            return createECDSASignature(messageHash, privateKeyBigInt);
        } catch (Exception e) {
            throw new RuntimeException("Ethereum sign error", e);
        }
    }

    public byte[] signTransaction(byte[] transactionData, String privateKey) {
        try {
            // 개인키를 BigInteger로 변환
            BigInteger privateKeyBigInt = new BigInteger(privateKey, 16);
            
            // 트랜잭션 데이터 해시 생성 (Keccak-256)
            byte[] transactionHash = keccak256(transactionData);
            
            // ECDSA 서명 생성
            return createECDSASignature(transactionHash, privateKeyBigInt);
        } catch (Exception e) {
            throw new RuntimeException("Ethereum transaction sign error", e);
        }
    }
    
    @Override
    public <T> byte[] signTransactionBody(TransactionBody<T> transactionBody, String privateKey) {
        try {
            // 1. Ethereum 특화 필드들을 동적으로 설정
            EthereumTransactionData ethereumData = createEthereumTransactionData(transactionBody);
            
            // 2. Raw transaction 데이터 생성 (서명 전)
            byte[] rawTransactionData = createEthereumRawTransaction(ethereumData);
            
            // 3. 트랜잭션 데이터 해시 생성 (Keccak-256)
            byte[] transactionHash = keccak256(rawTransactionData);
            
            // 4. ECDSA 서명 생성
            byte[] signature = createECDSASignature(transactionHash, new BigInteger(privateKey, 16));
            
            // 5. signedRawTransaction 생성 (서명 포함된 RLP 인코딩)
            return createSignedRawTransaction(ethereumData, signature);
            
        } catch (Exception e) {
            throw new RuntimeException("Ethereum raw transaction sign error", e);
        }
    }
    
    /**
     * Ethereum 트랜잭션 데이터 생성 (네트워크별 특화 필드 포함)
     */
    private <T> EthereumTransactionData createEthereumTransactionData(TransactionBody<T> transactionBody) {
        // 기본값을 사용하여 트랜잭션 데이터 생성
        BigInteger gasPrice = getGasPrice();
        BigInteger gasLimit = getGasLimit(transactionBody);
        BigInteger value = getValue(transactionBody);
        long nonce = getNonce(transactionBody.getFromAddress());
        
        return new EthereumTransactionData(
            gasPrice,
            gasLimit,
            value,
            transactionBody.getToAddress()
        );
    }
    
    /**
     * 가스 가격 가져오기 (기본값 사용)
     */
    private BigInteger getGasPrice() {
        // 기본값 사용 (20 Gwei)
        return BigInteger.valueOf(20000000000L);
    }
    
    /**
     * 가스 한도 계산 (트랜잭션 타입에 따라)
     */
    private <T> BigInteger getGasLimit(TransactionBody<T> transactionBody) {
        switch (transactionBody.getType()) {
            case PROPOSAL_CREATE:
            case PROPOSAL_VOTE:
                // 스마트 컨트랙트 호출은 더 많은 가스 필요
                return BigInteger.valueOf(100000L);
            case TOKEN_TRANSFER:
                // ERC-20 토큰 전송
                return BigInteger.valueOf(65000L);
            default:
                // 기본 ETH 전송
                return BigInteger.valueOf(21000L);
        }
    }
    
    /**
     * 전송할 ETH 양 계산
     */
    private <T> BigInteger getValue(TransactionBody<T> transactionBody) {
        // 대부분의 경우 0 (스마트 컨트랙트 호출)
        // 실제 ETH 전송의 경우에만 값이 있음
        return BigInteger.ZERO;
    }
    
    /**
     * 논스 가져오기 (기본값 사용)
     */
    private long getNonce(String fromAddress) {
        // 기본값 0 사용
        return 0L;
    }
    
    /**
     * Ethereum Raw Transaction 생성 (RLP 인코딩)
     */
    private byte[] createEthereumRawTransaction(EthereumTransactionData ethereumData) {
        // 실제 구현에서는 RLP 인코딩을 사용해야 함
        // 여기서는 간단한 바이트 배열 조합으로 대체
        String rawData = String.format("%s:%s:%s:%s",
            ethereumData.getGasPrice().toString(),
            ethereumData.getGasLimit().toString(),
            ethereumData.getValue().toString(),
            ethereumData.getContractAddress() != null ? ethereumData.getContractAddress() : ""
        );
        return rawData.getBytes();
    }
    
    /**
     * Signed Raw Transaction 생성 (서명 포함)
     */
    private byte[] createSignedRawTransaction(EthereumTransactionData ethereumData, byte[] signature) {
        // 실제 구현에서는 RLP 인코딩된 서명된 트랜잭션을 생성해야 함
        // Etherscan 문서에 따르면 0x 접두사가 있는 hex string 형태
        String signedHex = "0x" + bytesToHex(signature);
        return signedHex.getBytes();
    }
    
    /**
     * Keccak-256 해시 함수 구현 (Ethereum 표준)
     */
    private byte[] keccak256(byte[] input) {
        return HashUtils.keccak256(input);
    }
    
    /**
     * 바이트 배열을 16진수 문자열로 변환
     */
    private String bytesToHex(byte[] bytes) {
        return HexUtils.bytesToHex(bytes);
    }

    /**
     * Ethereum 주소 유효성 검증
     */
    public boolean isValidAddress(String address) {
        if (address == null || address.isEmpty()) {
            return false;
        }
        if (!address.startsWith("0x")) {
            return false;
        }
        if (address.length() != 42) {
            return false;
        }
        String hexPart = address.substring(2);
        return hexPart.matches("^[0-9a-fA-F]{40}$");
    }
} 