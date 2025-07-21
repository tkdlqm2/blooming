package com.bloominggrace.governance.shared.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * RawTransaction 생성을 위한 인터페이스
 * 각 블록체인 네트워크별로 구현체를 제공합니다.
 */
public interface RawTransactionBuilder {
    
    /**
     * ERC20 토큰 전송을 위한 RawTransaction JSON 생성
     * 
     * @param data 트랜잭션 데이터 (fromAddress, toAddress, tokenAddress, amount, nonce 등)
     * @return RawTransaction JSON 문자열
     */
    String createRawTransaction(Map<String, String> data);
    
    /**
     * 거버넌스 제안 생성을 위한 RawTransaction JSON 생성
     * 
     * @param proposalId 제안 ID
     * @param title 제안 제목
     * @param description 제안 설명
     * @param walletAddress 제안자 지갑 주소
     * @param proposalFee 제안 수수료
     * @param votingStartDate 투표 시작일
     * @param votingEndDate 투표 종료일
     * @param requiredQuorum 필요 정족수
     * @param nonce 트랜잭션 nonce (선택사항)
     * @return RawTransaction JSON 문자열
     */
    String createProposalCreationRawTransaction(
        UUID proposalId,
        String title,
        String description,
        String walletAddress,
        BigDecimal proposalFee,
        LocalDateTime votingStartDate,
        LocalDateTime votingEndDate,
        BigDecimal requiredQuorum,
        String nonce
    );
    
    /**
     * 거버넌스 투표를 위한 RawTransaction JSON 생성
     * 
     * @param proposalId 제안 ID
     * @param walletAddress 투표자 지갑 주소
     * @param voteType 투표 타입 (YES, NO, ABSTAIN)
     * @param reason 투표 이유 (선택사항)
     * @param votingPower 투표 권한 (토큰 수량)
     * @param nonce 트랜잭션 nonce (선택사항)
     * @return RawTransaction JSON 문자열
     */
    String createVoteRawTransaction(
        UUID proposalId,
        String walletAddress,
        String voteType,
        String reason,
        BigDecimal votingPower,
        String nonce
    );
} 