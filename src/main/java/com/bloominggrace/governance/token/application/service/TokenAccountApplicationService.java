package com.bloominggrace.governance.token.application.service;

import com.bloominggrace.governance.shared.domain.UserId;
import com.bloominggrace.governance.token.application.dto.CreateTokenAccountRequest;
import com.bloominggrace.governance.token.application.dto.TokenAccountDto;
import com.bloominggrace.governance.token.domain.model.TokenAccount;
import com.bloominggrace.governance.token.infrastructure.repository.TokenAccountRepository;
import com.bloominggrace.governance.user.application.service.UserService;
import com.bloominggrace.governance.wallet.application.service.WalletApplicationService;
import com.bloominggrace.governance.wallet.domain.model.Wallet;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import com.bloominggrace.governance.shared.domain.model.BlockchainMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Optional;
import java.math.BigDecimal;

/**
 * 토큰 계정 애플리케이션 서비스
 */
@Slf4j
@Service
@Transactional
public class TokenAccountApplicationService {
    
    private final TokenAccountRepository tokenAccountRepository;
    private final WalletApplicationService walletApplicationService;
    private final UserService userService;
    
    // 토큰 설정 주입
    @Value("${token.ethereum.contract-address}")
    private String ethereumContractAddress;
    
    @Value("${token.ethereum.symbol}")
    private String ethereumSymbol;
    
    @Value("${token.solana.contract-address}")
    private String solanaContractAddress;
    
    @Value("${token.solana.symbol}")
    private String solanaSymbol;
    
    public TokenAccountApplicationService(TokenAccountRepository tokenAccountRepository,
                                        WalletApplicationService walletApplicationService,
                                        UserService userService) {
        this.tokenAccountRepository = tokenAccountRepository;
        this.walletApplicationService = walletApplicationService;
        this.userService = userService;
        
        // 설정값 로그 출력
        log.info("TokenAccountApplicationService initialized with:");
        log.info("  ethereumContractAddress: {}", ethereumContractAddress);
        log.info("  ethereumSymbol: {}", ethereumSymbol);
        log.info("  solanaContractAddress: {}", solanaContractAddress);
        log.info("  solanaSymbol: {}", solanaSymbol);
    }
    
    /**
     * 새로운 토큰 계정을 생성합니다.
     */
    public TokenAccountDto createTokenAccount(CreateTokenAccountRequest request) {
        // 사용자 존재 여부 확인
        userService.findById(UUID.fromString(request.getUserId()));
        
        // 지갑 정보 조회 (지갑 주소로)
        Wallet wallet = walletApplicationService.getWalletByAddress(request.getWalletAddress())
            .orElseThrow(() -> new IllegalArgumentException("Wallet not found with address: " + request.getWalletAddress()));
        
        // 이미 같은 컨트랙트의 토큰 계정이 존재하는지 확인
        if (tokenAccountRepository.existsByWalletAndContract(wallet, request.getContract())) {
            throw new IllegalArgumentException("Token account for this contract already exists in the wallet");
        }
        
        // 토큰 계정 생성
        TokenAccount tokenAccount = new TokenAccount(
            wallet,
            new UserId(UUID.fromString(request.getUserId())),
            request.getNetwork(),
            request.getContract(),
            request.getSymbol()
        );
        
        TokenAccount savedTokenAccount = tokenAccountRepository.save(tokenAccount);
        
        return convertToDto(savedTokenAccount);
    }
    
    /**
     * 토큰 계정 ID로 조회합니다.
     */
    @Transactional(readOnly = true)
    public TokenAccountDto findById(UUID id) {
        TokenAccount tokenAccount = tokenAccountRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Token account not found with id: " + id));
        
        return convertToDto(tokenAccount);
    }
    
    /**
     * 사용자 ID로 모든 토큰 계정을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<TokenAccountDto> findByUserId(String userId) {
        List<TokenAccount> tokenAccounts = tokenAccountRepository.findAllByUserId(new UserId(UUID.fromString(userId)));
        
        return tokenAccounts.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 사용자 ID, 네트워크, 컨트랙트로 특정 토큰 계정을 조회합니다.
     */
    @Transactional(readOnly = true)
    public Optional<TokenAccountDto> findByUserIdAndNetworkAndContract(String userId, NetworkType network, String contract) {
        Optional<TokenAccount> tokenAccount = tokenAccountRepository.findByUserIdAndNetworkAndContract(
            new UserId(UUID.fromString(userId)), network, contract);
        
        return tokenAccount.map(this::convertToDto);
    }
    
    /**
     * 사용자 ID와 네트워크로 토큰 계정들을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<TokenAccountDto> findByUserIdAndNetwork(String userId, NetworkType network) {
        List<TokenAccount> tokenAccounts = tokenAccountRepository.findByUserIdAndNetwork(
            new UserId(UUID.fromString(userId)), network);
        
        return tokenAccounts.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 지갑 주소로 모든 토큰 계정을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<TokenAccountDto> findByWalletAddress(String walletAddress) {
        List<TokenAccount> tokenAccounts = tokenAccountRepository.findByWalletAddress(walletAddress);
        
        return tokenAccounts.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 지갑 주소, 네트워크, 컨트랙트로 특정 토큰 계정을 조회합니다.
     */
    @Transactional(readOnly = true)
    public Optional<TokenAccountDto> findByWalletAddressAndNetworkAndContract(String walletAddress, NetworkType network, String contract) {
        Optional<TokenAccount> tokenAccount = tokenAccountRepository.findByWalletAddressAndNetworkAndContract(
            walletAddress, network, contract);
        
        return tokenAccount.map(this::convertToDto);
    }
    
    /**
     * 지갑 ID로 모든 토큰 계정을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<TokenAccountDto> findByWalletId(UUID walletId) {
        Wallet wallet = walletApplicationService.findById(walletId)
            .orElseThrow(() -> new IllegalArgumentException("Wallet not found with id: " + walletId));
        
        List<TokenAccount> tokenAccounts = tokenAccountRepository.findByWallet(wallet);
        
        return tokenAccounts.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 지갑 ID와 네트워크로 토큰 계정을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<TokenAccountDto> findByWalletIdAndNetwork(UUID walletId, NetworkType network) {
        Wallet wallet = walletApplicationService.findById(walletId)
            .orElseThrow(() -> new IllegalArgumentException("Wallet not found with id: " + walletId));
        
        List<TokenAccount> tokenAccounts = tokenAccountRepository.findByWalletAndNetwork(wallet, network);
        
        return tokenAccounts.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 지갑 주소와 네트워크로 토큰 계정을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<TokenAccountDto> findByWalletAddressAndNetwork(String walletAddress, NetworkType network) {
        List<TokenAccount> tokenAccounts = tokenAccountRepository.findByWalletAddressAndNetwork(walletAddress, network);
        
        return tokenAccounts.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 모든 토큰 계정을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<TokenAccountDto> findAll() {
        List<TokenAccount> tokenAccounts = tokenAccountRepository.findAll();
        
        return tokenAccounts.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 토큰 계정을 삭제합니다.
     */
    public void deleteTokenAccount(UUID id) {
        if (!tokenAccountRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("Token account not found with id: " + id);
        }
        
        tokenAccountRepository.delete(id);
    }
    
    /**
     * 토큰 계정을 조회하거나 없으면 생성합니다.
     */
    public TokenAccountDto findOrCreateTokenAccount(String userId, String walletAddress, NetworkType network, String contract, String symbol) {
        try {
            // 먼저 기존 토큰 계정이 있는지 확인
            UUID userIdUuid = UUID.fromString(userId);
            Optional<TokenAccount> existingTokenAccount = tokenAccountRepository.findByUserIdAndNetworkAndContract(
                new UserId(userIdUuid), network, contract);
            
            if (existingTokenAccount.isPresent()) {
                return convertToDto(existingTokenAccount.get());
            }
            
            // 없으면 새로 생성
            CreateTokenAccountRequest request = new CreateTokenAccountRequest();
            request.setUserId(userId);
            request.setWalletAddress(walletAddress);
            request.setNetwork(network);
            request.setContract(contract);
            request.setSymbol(symbol);
            
            return createTokenAccount(request);
        } catch (Exception e) {
            log.error("Error in findOrCreateTokenAccount: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find or create token account: " + e.getMessage(), e);
        }
    }
    
    /**
     * 네트워크별 기본 토큰 계정을 조회하거나 생성합니다.
     */
    public TokenAccountDto findOrCreateDefaultTokenAccount(String userId, String walletAddress, NetworkType network) {
        String contractAddress;
        String symbol;
        
        switch (network) {
            case ETHEREUM:
                contractAddress = BlockchainMetadata.Ethereum.ERC20_CONTRACT_ADDRESS;
                symbol = BlockchainMetadata.Ethereum.ERC20_SYMBOL;
                break;
            case SOLANA:
                contractAddress = BlockchainMetadata.Solana.SPL_TOKEN_MINT_ADDRESS;
                symbol = BlockchainMetadata.Solana.SPL_TOKEN_SYMBOL;
                break;
            default:
                throw new IllegalArgumentException("Unsupported network type: " + network);
        }
        
        return findOrCreateTokenAccount(userId, walletAddress, network, contractAddress, symbol);
    }
    
    /**
     * TokenAccount 엔티티를 직접 반환하는 메서드 (내부 사용)
     */
    public TokenAccount getOrCreateTokenAccount(UserId userId, String walletAddress, NetworkType network, String contract, String symbol) {
        log.info("getOrCreateTokenAccount called with userId: {}, walletAddress: {}, network: {}, contract: {}, symbol: {}", 
            userId, walletAddress, network, contract, symbol);
        
        // 먼저 기존 토큰 계정이 있는지 확인
        Optional<TokenAccount> existingTokenAccount = tokenAccountRepository.findByUserIdAndNetworkAndContract(userId, network, contract);
        
        if (existingTokenAccount.isPresent()) {
            log.info("Existing token account found: {}", existingTokenAccount.get().getId());
            return existingTokenAccount.get();
        }
        
        log.info("No existing token account found, creating new one");
        
        // 없으면 새로 생성
        Optional<Wallet> walletOpt = walletApplicationService.getWalletByAddress(walletAddress);
        if (walletOpt.isEmpty()) {
            log.error("Wallet not found with address: {}", walletAddress);
            throw new IllegalArgumentException("Wallet not found with address: " + walletAddress);
        }
        
        Wallet wallet = walletOpt.get();
        log.info("Found wallet: {}", wallet.getId());
        
        TokenAccount tokenAccount = new TokenAccount(
            wallet,
            userId,
            network,
            contract,
            symbol
        );
        
        log.info("Created token account: {}", tokenAccount.getId());
        return tokenAccountRepository.save(tokenAccount);
    }
    
    /**
     * TokenAccount 엔티티를 DTO로 변환합니다.
     */
    private TokenAccountDto convertToDto(TokenAccount tokenAccount) {
        String userIdString = tokenAccount.getUserId() != null && tokenAccount.getUserId().getValue() != null 
            ? tokenAccount.getUserId().getValue().toString() 
            : null;
            
        return new TokenAccountDto(
            tokenAccount.getId(),
            tokenAccount.getWallet().getId(),
            userIdString,
            tokenAccount.getTotalBalance(),
            BigDecimal.ZERO, // 스테이킹 기능 제거로 인해 0으로 설정
            tokenAccount.getAvailableBalance(),
            tokenAccount.getNetwork(),
            tokenAccount.getContract(),
            tokenAccount.getSymbol(),
            tokenAccount.isActive(),
            tokenAccount.getCreatedAt(),
            tokenAccount.getUpdatedAt()
        );
    }
} 