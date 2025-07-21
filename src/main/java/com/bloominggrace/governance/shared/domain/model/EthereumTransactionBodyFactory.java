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
        
        // nonce 값 결정 (TransactionRequest에서 가져오거나 기본값 사용)
        long nonce = request.getNonce() != null ? request.getNonce().longValue() : System.currentTimeMillis();
        
        // TransactionBody 생성 (타입 캐스팅)
        @SuppressWarnings("unchecked")
        TransactionBody<T> transactionBody = (TransactionBody<T>) new TransactionBody<>(
            convertTransactionType(request.getType()),
            request.getFromAddress(),
            request.getToAddress(),
            data,
            request.getNetworkType(),
            ethereumData,
            nonce // nonce 전달
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
        BigInteger nonce = request.getNonce() != null ? request.getNonce() : BigInteger.ZERO;
        
        // TOKEN_TRANSFER의 경우 토큰 컨트랙트 주소를 toAddress로 설정
        String toAddress = request.getToAddress();
        String contractAddress = null;
        String data = null;
        
        if (request.getType() == TransactionRequest.TransactionType.TOKEN_TRANSFER) {
            // ERC20 전송의 경우 toAddress는 토큰 컨트랙트 주소
            toAddress = request.getTokenAddress();
            contractAddress = request.getTokenAddress();
            // ERC20 transfer 함수 호출을 위한 실제 데이터 생성
            BigInteger amountWei = request.getAmount().multiply(java.math.BigDecimal.valueOf(1e18)).toBigInteger();
            data = EthereumTransactionData.createERC20TransferData(request.getToAddress(), amountWei);
        }
        
        return new EthereumTransactionData(
            gasPrice,
            gasLimit,
            value,
            toAddress,
            contractAddress,
            data,
            nonce
        );
    }
    
    /**
     * 트랜잭션 타입에 따른 가스 가격 계산 (현재 설정의 5배로 조정)
     */
    private BigInteger getGasPrice(TransactionRequest request) {
        // 실제 구현에서는 Ethereum RPC를 통해 현재 가스 가격을 가져와야 함
        switch (request.getType()) {
            case PROPOSAL_CREATE:
            case PROPOSAL_VOTE:
                return BigInteger.valueOf(125000000000L); // 125 Gwei (스마트 컨트랙트 호출) - 5배 증가
            case TOKEN_MINT:
            case TOKEN_BURN:
                return BigInteger.valueOf(110000000000L); // 110 Gwei (토큰 작업) - 5배 증가
            case TOKEN_TRANSFER:
                return BigInteger.valueOf(100000000000L); // 100 Gwei (기본) - 5배 증가
            default:
                return BigInteger.valueOf(100000000000L); // 100 Gwei - 5배 증가
        }
    }
    
    /**
     * 트랜잭션 타입에 따른 가스 한도 계산 (5배 증가)
     */
    private BigInteger getGasLimit(TransactionRequest request) {
        switch (request.getType()) {
            case PROPOSAL_CREATE:
                return BigInteger.valueOf(1500000L); // 정책 제안 생성 - 5배 증가
            case PROPOSAL_VOTE:
                return BigInteger.valueOf(500000L); // 투표 - 5배 증가
            case TOKEN_MINT:
                return BigInteger.valueOf(1000000L); // 토큰 민팅 - 5배 증가
            case TOKEN_BURN:
                return BigInteger.valueOf(750000L); // 토큰 소각 - 5배 증가
            case TOKEN_TRANSFER:
                return BigInteger.valueOf(325000L); // ERC-20 토큰 전송 - 5배 증가
            default:
                return BigInteger.valueOf(105000L); // 기본 ETH 전송 - 5배 증가
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
            case TOKEN_TRANSFER: {
                // ERC20 transfer 함수 호출을 위한 실제 컨트랙트 데이터 생성
                String toAddress = request.getToAddress();
                String amount = request.getAmount().toString();
                String tokenAddress = request.getTokenAddress();
                
                // ERC20 transfer 함수 시그니처: transfer(address,uint256)
                // 실제 구현에서는 EthereumTransactionData.createERC20TransferData 사용
                return String.format("{\"method\":\"transfer\",\"toAddress\":\"%s\",\"amount\":\"%s\",\"tokenAddress\":\"%s\",\"type\":\"ERC20_TRANSFER\"}", 
                    toAddress, amount, tokenAddress);
            }
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