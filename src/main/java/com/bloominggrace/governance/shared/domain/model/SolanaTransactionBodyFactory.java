package com.bloominggrace.governance.shared.domain.model;

import org.springframework.stereotype.Component;

/**
 * 솔라나 네트워크용 트랜잭션 본문 팩토리
 */
@Component("solanaTransactionBodyFactory")
public class SolanaTransactionBodyFactory implements TransactionBodyFactory {
    
    @Override
    public <T> TransactionBody<T> createTransactionBody(TransactionRequest request) {
        // 솔라나 네트워크별 특정 데이터 생성
        SolanaTransactionData solanaData = createSolanaTransactionData(request);
        
        // 트랜잭션 데이터 생성
        String data = createTransactionData(request);
        
        // TransactionBody 생성 (타입 캐스팅)
        @SuppressWarnings("unchecked")
        TransactionBody<T> transactionBody = (TransactionBody<T>) new TransactionBody<>(
            convertTransactionType(request.getType()),
            request.getFromAddress(),
            request.getToAddress(),
            data,
            request.getNetworkType(),
            solanaData
        );
        
        return transactionBody;
    }
    
    @Override
    public String getSupportedNetworkType() {
        return "SOLANA";
    }
    
    /**
     * 솔라나 네트워크별 특정 데이터 생성
     */
    private SolanaTransactionData createSolanaTransactionData(TransactionRequest request) {
        // 실제로는 Solana RPC를 통해 동적으로 가져와야 함
        String recentBlockhash = getRecentBlockhash();
        long fee = getFee(request);
        String programId = getProgramId(request);
        
        return new SolanaTransactionData(
            recentBlockhash,
            fee,
            programId
        );
    }
    
    /**
     * 최근 블록해시 가져오기 (실제로는 Solana RPC 호출)
     */
    private String getRecentBlockhash() {
        // 실제 구현에서는 Solana RPC를 통해 최근 블록해시를 가져와야 함
        // 예: https://api.mainnet-beta.solana.com
        return "11111111111111111111111111111111";
    }
    
    /**
     * 트랜잭션 타입에 따른 수수료 계산
     */
    private long getFee(TransactionRequest request) {
        switch (request.getType()) {
            case PROPOSAL_CREATE:
                return 10000L; // 0.00001 SOL (정책 제안 생성)
            case PROPOSAL_VOTE:
                return 5000L;  // 0.000005 SOL (투표)
            case TOKEN_MINT:
            case TOKEN_BURN:
                return 8000L;  // 0.000008 SOL (토큰 작업)
            case TOKEN_TRANSFER:
                return 5000L;  // 0.000005 SOL (토큰 전송)
            default:
                return 5000L;  // 기본 수수료
        }
    }
    
    /**
     * 트랜잭션 타입에 따른 프로그램 ID 반환
     */
    private String getProgramId(TransactionRequest request) {
        switch (request.getType()) {
            case PROPOSAL_CREATE:
            case PROPOSAL_VOTE:
                return "GovernanceProgram111111111111111111111111111"; // 가상의 거버넌스 프로그램 ID
            case TOKEN_MINT:
            case TOKEN_BURN:
            case TOKEN_TRANSFER:
                return "TokenProgram111111111111111111111111111111"; // 가상의 토큰 프로그램 ID
            default:
                return "SystemProgram111111111111111111111111111111"; // Solana 시스템 프로그램
        }
    }
    
    /**
     * 트랜잭션 데이터 생성
     */
    private String createTransactionData(TransactionRequest request) {
        switch (request.getType()) {
            case PROPOSAL_CREATE: {
                TransactionRequest.ProposalData proposalData = (TransactionRequest.ProposalData) request.getTransactionData();
                return String.format("{\"proposalId\":\"%s\",\"title\":\"%s\",\"description\":\"%s\",\"fee\":\"%s\"}",
                    proposalData.getProposalId(), proposalData.getTitle(), proposalData.getDescription(), 
                    proposalData.getProposalFee().toString());
            }
            case PROPOSAL_VOTE: {
                TransactionRequest.VoteData voteData = (TransactionRequest.VoteData) request.getTransactionData();
                return String.format("{\"proposalId\":\"%s\",\"voteType\":\"%s\",\"votingPower\":\"%s\",\"reason\":\"%s\"}",
                    voteData.getProposalId(), voteData.getVoteType(), voteData.getVotingPower().toString(), 
                    voteData.getReason() != null ? voteData.getReason() : "");
            }
            case TOKEN_TRANSFER:
                return String.format("{\"tokenAddress\":\"%s\",\"amount\":\"%s\",\"type\":\"TOKEN_TRANSFER\"}", 
                    request.getTokenAddress(), request.getAmount().toString());
            case TOKEN_MINT: {
                TransactionRequest.TokenMintData mintData = (TransactionRequest.TokenMintData) request.getTransactionData();
                return String.format("{\"amount\":\"%s\",\"description\":\"%s\",\"type\":\"TOKEN_MINT\"}", 
                    request.getAmount().toString(), mintData.getDescription() != null ? mintData.getDescription() : "");
            }
            case TOKEN_BURN: {
                TransactionRequest.TokenBurnData burnData = (TransactionRequest.TokenBurnData) request.getTransactionData();
                return String.format("{\"amount\":\"%s\",\"description\":\"%s\",\"type\":\"TOKEN_BURN\"}", 
                    request.getAmount().toString(), burnData.getDescription() != null ? burnData.getDescription() : "");
            }
            default:
                return "{}";
        }
    }
    
    /**
     * TransactionRequest 타입을 TransactionBody 타입으로 변환
     */
    private TransactionBody.TransactionType convertTransactionType(TransactionRequest.TransactionType requestType) {
        switch (requestType) {
            case PROPOSAL_CREATE:
                return TransactionBody.TransactionType.PROPOSAL_CREATE;
            case PROPOSAL_VOTE:
                return TransactionBody.TransactionType.PROPOSAL_VOTE;
            case TOKEN_MINT:
                return TransactionBody.TransactionType.TOKEN_MINT;
            case TOKEN_BURN:
                return TransactionBody.TransactionType.TOKEN_BURN;
            case TOKEN_TRANSFER:
                return TransactionBody.TransactionType.TOKEN_TRANSFER;
            default:
                throw new IllegalArgumentException("Unsupported transaction type: " + requestType);
        }
    }
} 