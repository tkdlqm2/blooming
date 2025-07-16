package com.bloominggrace.governance.shared.domain.model;

import org.springframework.stereotype.Component;

import java.math.BigInteger;

/**
 * 이더리움 네트워크용 트랜잭션 본문 팩토리
 */
@Component("ethereumTransactionBodyFactory")
public class EthereumTransactionBodyFactory implements TransactionBodyFactory {
    
    @Override
    public <T> TransactionBody<T> createTransactionBody(TransactionRequest request) {
        // 이더리움 네트워크별 특정 데이터 생성
        EthereumTransactionData ethereumData = createEthereumTransactionData(request);
        
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
            ethereumData
        );
        
        return transactionBody;
    }
    
    @Override
    public String getSupportedNetworkType() {
        return "ETHEREUM";
    }
    
    /**
     * 이더리움 네트워크별 특정 데이터 생성
     */
    private EthereumTransactionData createEthereumTransactionData(TransactionRequest request) {
        // 실제로는 Ethereum RPC를 통해 동적으로 가져와야 함
        BigInteger gasPrice = getGasPrice(request);
        BigInteger gasLimit = getGasLimit(request);
        BigInteger value = getValue(request);
        
        return new EthereumTransactionData(
            gasPrice,
            gasLimit,
            value,
            request.getToAddress()
        );
    }
    
    /**
     * 트랜잭션 타입에 따른 가스 가격 계산
     */
    private BigInteger getGasPrice(TransactionRequest request) {
        // 실제 구현에서는 Ethereum RPC를 통해 현재 가스 가격을 가져와야 함
        switch (request.getType()) {
            case PROPOSAL_CREATE:
            case PROPOSAL_VOTE:
                return BigInteger.valueOf(25000000000L); // 25 Gwei (스마트 컨트랙트 호출)
            case TOKEN_MINT:
            case TOKEN_BURN:
            case TOKEN_STAKE:
            case TOKEN_UNSTAKE:
                return BigInteger.valueOf(22000000000L); // 22 Gwei (토큰 작업)
            case TOKEN_TRANSFER:
                return BigInteger.valueOf(20000000000L); // 20 Gwei (기본)
            default:
                return BigInteger.valueOf(20000000000L);
        }
    }
    
    /**
     * 트랜잭션 타입에 따른 가스 한도 계산
     */
    private BigInteger getGasLimit(TransactionRequest request) {
        switch (request.getType()) {
            case PROPOSAL_CREATE:
                return BigInteger.valueOf(300000L); // 정책 제안 생성
            case PROPOSAL_VOTE:
                return BigInteger.valueOf(100000L); // 투표
            case TOKEN_MINT:
                return BigInteger.valueOf(200000L); // 토큰 민팅
            case TOKEN_BURN:
                return BigInteger.valueOf(150000L); // 토큰 소각
            case TOKEN_STAKE:
            case TOKEN_UNSTAKE:
                return BigInteger.valueOf(180000L); // 토큰 스테이킹/언스테이킹
            case TOKEN_TRANSFER:
                return BigInteger.valueOf(65000L); // ERC-20 토큰 전송
            default:
                return BigInteger.valueOf(21000L); // 기본 ETH 전송
        }
    }
    
    /**
     * 트랜잭션 타입에 따른 전송할 ETH 값 계산
     */
    private BigInteger getValue(TransactionRequest request) {
        // 대부분의 경우 0 (스마트 컨트랙트 호출)
        // 실제 ETH 전송의 경우에만 값이 있음
        return BigInteger.ZERO;
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
            case TOKEN_STAKE:
                return String.format("{\"amount\":\"%s\",\"type\":\"TOKEN_STAKE\"}", request.getAmount().toString());
            case TOKEN_UNSTAKE:
                return String.format("{\"amount\":\"%s\",\"type\":\"TOKEN_UNSTAKE\"}", request.getAmount().toString());
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
            case TOKEN_STAKE:
                return TransactionBody.TransactionType.TOKEN_STAKE;
            case TOKEN_UNSTAKE:
                return TransactionBody.TransactionType.TOKEN_UNSTAKE;
            case TOKEN_TRANSFER:
                return TransactionBody.TransactionType.TOKEN_TRANSFER;
            default:
                throw new IllegalArgumentException("Unsupported transaction type: " + requestType);
        }
    }
} 