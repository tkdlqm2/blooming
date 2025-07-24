package com.bloominggrace.governance.governance.application.dto;

import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CreateProposalRequest DTO 테스트")
class CreateProposalRequestTest {

    @Test
    @DisplayName("기본 생성자로 CreateProposalRequest를 생성할 수 있다")
    void createCreateProposalRequestWithDefaultConstructor() {
        // when
        CreateProposalRequest request = new CreateProposalRequest();

        // then
        assertThat(request).isNotNull();
        assertThat(request.getCreatorId()).isNull();
        assertThat(request.getTitle()).isNull();
        assertThat(request.getDescription()).isNull();
        assertThat(request.getVotingStartDate()).isNull();
        assertThat(request.getVotingEndDate()).isNull();
        assertThat(request.getRequiredQuorum()).isNull();
        assertThat(request.getCreatorWalletAddress()).isNull();
        assertThat(request.getProposalFee()).isNull();
        assertThat(request.getNetworkType()).isNull();
    }

    @Test
    @DisplayName("모든 필드를 포함한 생성자로 CreateProposalRequest를 생성할 수 있다")
    void createCreateProposalRequestWithAllArgsConstructor() {
        // given
        UUID creatorId = UUID.randomUUID();
        String title = "새로운 기능 제안";
        String description = "이 제안은 새로운 기능을 추가하는 것입니다";
        LocalDateTime votingStartDate = LocalDateTime.now().plusDays(1);
        LocalDateTime votingEndDate = LocalDateTime.now().plusDays(7);
        BigDecimal requiredQuorum = new BigDecimal("1000.00");
        String creatorWalletAddress = "0x1234567890abcdef";
        BigDecimal proposalFee = new BigDecimal("100.00");
        NetworkType networkType = NetworkType.ETHEREUM;

        // when
        CreateProposalRequest request = new CreateProposalRequest(
            creatorId, title, description, votingStartDate, votingEndDate,
            requiredQuorum, creatorWalletAddress, proposalFee, networkType
        );

        // then
        assertThat(request).isNotNull();
        assertThat(request.getCreatorId()).isEqualTo(creatorId);
        assertThat(request.getTitle()).isEqualTo(title);
        assertThat(request.getDescription()).isEqualTo(description);
        assertThat(request.getVotingStartDate()).isEqualTo(votingStartDate);
        assertThat(request.getVotingEndDate()).isEqualTo(votingEndDate);
        assertThat(request.getRequiredQuorum()).isEqualTo(requiredQuorum);
        assertThat(request.getCreatorWalletAddress()).isEqualTo(creatorWalletAddress);
        assertThat(request.getProposalFee()).isEqualTo(proposalFee);
        assertThat(request.getNetworkType()).isEqualTo(networkType);
    }

    @Test
    @DisplayName("CreateProposalRequest의 setter와 getter가 올바르게 작동한다")
    void createProposalRequestSettersAndGetters() {
        // given
        CreateProposalRequest request = new CreateProposalRequest();
        UUID creatorId = UUID.randomUUID();
        String title = "수정된 제안";
        String description = "수정된 설명";
        LocalDateTime votingStartDate = LocalDateTime.now().plusDays(2);
        LocalDateTime votingEndDate = LocalDateTime.now().plusDays(8);
        BigDecimal requiredQuorum = new BigDecimal("2000.00");
        String creatorWalletAddress = "0xabcdef1234567890";
        BigDecimal proposalFee = new BigDecimal("200.00");
        NetworkType networkType = NetworkType.SOLANA;

        // when
        request.setCreatorId(creatorId);
        request.setTitle(title);
        request.setDescription(description);
        request.setVotingStartDate(votingStartDate);
        request.setVotingEndDate(votingEndDate);
        request.setRequiredQuorum(requiredQuorum);
        request.setCreatorWalletAddress(creatorWalletAddress);
        request.setProposalFee(proposalFee);
        request.setNetworkType(networkType);

        // then
        assertThat(request.getCreatorId()).isEqualTo(creatorId);
        assertThat(request.getTitle()).isEqualTo(title);
        assertThat(request.getDescription()).isEqualTo(description);
        assertThat(request.getVotingStartDate()).isEqualTo(votingStartDate);
        assertThat(request.getVotingEndDate()).isEqualTo(votingEndDate);
        assertThat(request.getRequiredQuorum()).isEqualTo(requiredQuorum);
        assertThat(request.getCreatorWalletAddress()).isEqualTo(creatorWalletAddress);
        assertThat(request.getProposalFee()).isEqualTo(proposalFee);
        assertThat(request.getNetworkType()).isEqualTo(networkType);
    }

    @Test
    @DisplayName("동일한 값을 가진 CreateProposalRequest들이 같다")
    void createProposalRequestEquality() {
        // given
        UUID creatorId = UUID.randomUUID();
        String title = "동일한 제안";
        String description = "동일한 설명";
        LocalDateTime votingStartDate = LocalDateTime.now().plusDays(1);
        LocalDateTime votingEndDate = LocalDateTime.now().plusDays(7);
        BigDecimal requiredQuorum = new BigDecimal("1000.00");
        String creatorWalletAddress = "0x1234567890abcdef";
        BigDecimal proposalFee = new BigDecimal("100.00");
        NetworkType networkType = NetworkType.ETHEREUM;

        CreateProposalRequest request1 = new CreateProposalRequest(
            creatorId, title, description, votingStartDate, votingEndDate,
            requiredQuorum, creatorWalletAddress, proposalFee, networkType
        );
        CreateProposalRequest request2 = new CreateProposalRequest(
            creatorId, title, description, votingStartDate, votingEndDate,
            requiredQuorum, creatorWalletAddress, proposalFee, networkType
        );

        // when & then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    @DisplayName("다른 값을 가진 CreateProposalRequest들은 다르다")
    void createProposalRequestInequality() {
        // given
        UUID creatorId1 = UUID.randomUUID();
        UUID creatorId2 = UUID.randomUUID();
        String title = "제안";
        String description = "설명";
        LocalDateTime votingStartDate = LocalDateTime.now().plusDays(1);
        LocalDateTime votingEndDate = LocalDateTime.now().plusDays(7);
        BigDecimal requiredQuorum = new BigDecimal("1000.00");
        String creatorWalletAddress = "0x1234567890abcdef";
        BigDecimal proposalFee = new BigDecimal("100.00");
        NetworkType networkType = NetworkType.ETHEREUM;

        CreateProposalRequest request1 = new CreateProposalRequest(
            creatorId1, title, description, votingStartDate, votingEndDate,
            requiredQuorum, creatorWalletAddress, proposalFee, networkType
        );
        CreateProposalRequest request2 = new CreateProposalRequest(
            creatorId2, title, description, votingStartDate, votingEndDate,
            requiredQuorum, creatorWalletAddress, proposalFee, networkType
        );

        // when & then
        assertThat(request1).isNotEqualTo(request2);
        assertThat(request1.hashCode()).isNotEqualTo(request2.hashCode());
    }

    @Test
    @DisplayName("CreateProposalRequest의 toString이 올바르게 작동한다")
    void createProposalRequestToString() {
        // given
        UUID creatorId = UUID.randomUUID();
        String title = "테스트 제안";
        String description = "테스트 설명";
        LocalDateTime votingStartDate = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime votingEndDate = LocalDateTime.of(2024, 1, 7, 18, 0);
        BigDecimal requiredQuorum = new BigDecimal("1000.00");
        String creatorWalletAddress = "0x1234567890abcdef";
        BigDecimal proposalFee = new BigDecimal("100.00");
        NetworkType networkType = NetworkType.ETHEREUM;

        CreateProposalRequest request = new CreateProposalRequest(
            creatorId, title, description, votingStartDate, votingEndDate,
            requiredQuorum, creatorWalletAddress, proposalFee, networkType
        );

        // when
        String toString = request.toString();

        // then
        assertThat(toString).isNotNull();
        assertThat(toString).contains(creatorId.toString());
        assertThat(toString).contains(title);
        assertThat(toString).contains(description);
        assertThat(toString).contains(votingStartDate.toString());
        assertThat(toString).contains(votingEndDate.toString());
        assertThat(toString).contains(requiredQuorum.toString());
        assertThat(toString).contains(creatorWalletAddress);
        assertThat(toString).contains(proposalFee.toString());
        assertThat(toString).contains(networkType.toString());
    }

    @Test
    @DisplayName("null 값을 가진 CreateProposalRequest를 생성할 수 있다")
    void createCreateProposalRequestWithNullValues() {
        // when
        CreateProposalRequest request = new CreateProposalRequest(
            null, null, null, null, null, null, null, null, null
        );

        // then
        assertThat(request).isNotNull();
        assertThat(request.getCreatorId()).isNull();
        assertThat(request.getTitle()).isNull();
        assertThat(request.getDescription()).isNull();
        assertThat(request.getVotingStartDate()).isNull();
        assertThat(request.getVotingEndDate()).isNull();
        assertThat(request.getRequiredQuorum()).isNull();
        assertThat(request.getCreatorWalletAddress()).isNull();
        assertThat(request.getProposalFee()).isNull();
        assertThat(request.getNetworkType()).isNull();
    }

    @Test
    @DisplayName("빈 문자열을 가진 CreateProposalRequest를 생성할 수 있다")
    void createCreateProposalRequestWithEmptyStrings() {
        // given
        UUID creatorId = UUID.randomUUID();
        String emptyTitle = "";
        String emptyDescription = "";
        LocalDateTime votingStartDate = LocalDateTime.now().plusDays(1);
        LocalDateTime votingEndDate = LocalDateTime.now().plusDays(7);
        BigDecimal requiredQuorum = new BigDecimal("1000.00");
        String emptyWalletAddress = "";
        BigDecimal proposalFee = new BigDecimal("100.00");
        NetworkType networkType = NetworkType.ETHEREUM;

        // when
        CreateProposalRequest request = new CreateProposalRequest(
            creatorId, emptyTitle, emptyDescription, votingStartDate, votingEndDate,
            requiredQuorum, emptyWalletAddress, proposalFee, networkType
        );

        // then
        assertThat(request).isNotNull();
        assertThat(request.getTitle()).isEqualTo(emptyTitle);
        assertThat(request.getDescription()).isEqualTo(emptyDescription);
        assertThat(request.getCreatorWalletAddress()).isEqualTo(emptyWalletAddress);
    }

    @Test
    @DisplayName("큰 소수점 자릿수의 금액을 가진 CreateProposalRequest를 생성할 수 있다")
    void createCreateProposalRequestWithLargeDecimals() {
        // given
        UUID creatorId = UUID.randomUUID();
        String title = "큰 금액 제안";
        String description = "큰 금액이 필요한 제안";
        LocalDateTime votingStartDate = LocalDateTime.now().plusDays(1);
        LocalDateTime votingEndDate = LocalDateTime.now().plusDays(7);
        BigDecimal largeQuorum = new BigDecimal("123456789.123456789");
        String creatorWalletAddress = "0x1234567890abcdef";
        BigDecimal largeFee = new BigDecimal("987654321.987654321");
        NetworkType networkType = NetworkType.ETHEREUM;

        // when
        CreateProposalRequest request = new CreateProposalRequest(
            creatorId, title, description, votingStartDate, votingEndDate,
            largeQuorum, creatorWalletAddress, largeFee, networkType
        );

        // then
        assertThat(request).isNotNull();
        assertThat(request.getRequiredQuorum()).isEqualTo(largeQuorum);
        assertThat(request.getProposalFee()).isEqualTo(largeFee);
    }

    @Test
    @DisplayName("0 금액을 가진 CreateProposalRequest를 생성할 수 있다")
    void createCreateProposalRequestWithZeroAmounts() {
        // given
        UUID creatorId = UUID.randomUUID();
        String title = "무료 제안";
        String description = "무료로 진행되는 제안";
        LocalDateTime votingStartDate = LocalDateTime.now().plusDays(1);
        LocalDateTime votingEndDate = LocalDateTime.now().plusDays(7);
        BigDecimal zeroQuorum = BigDecimal.ZERO;
        String creatorWalletAddress = "0x1234567890abcdef";
        BigDecimal zeroFee = BigDecimal.ZERO;
        NetworkType networkType = NetworkType.ETHEREUM;

        // when
        CreateProposalRequest request = new CreateProposalRequest(
            creatorId, title, description, votingStartDate, votingEndDate,
            zeroQuorum, creatorWalletAddress, zeroFee, networkType
        );

        // then
        assertThat(request).isNotNull();
        assertThat(request.getRequiredQuorum()).isEqualTo(zeroQuorum);
        assertThat(request.getProposalFee()).isEqualTo(zeroFee);
    }

    @Test
    @DisplayName("긴 제목과 설명을 가진 CreateProposalRequest를 생성할 수 있다")
    void createCreateProposalRequestWithLongTexts() {
        // given
        UUID creatorId = UUID.randomUUID();
        String longTitle = "매우 긴 제안 제목입니다. 이 제목은 50자를 넘어가는 긴 제목입니다.";
        String longDescription = "매우 긴 설명입니다. 이 설명은 여러 문장으로 구성되어 있으며, " +
            "제안의 세부사항을 자세히 설명합니다. 첫째, 이 제안의 배경과 필요성을 설명합니다. " +
            "둘째, 제안의 구체적인 내용과 실행 방안을 제시합니다.";
        LocalDateTime votingStartDate = LocalDateTime.now().plusDays(1);
        LocalDateTime votingEndDate = LocalDateTime.now().plusDays(7);
        BigDecimal requiredQuorum = new BigDecimal("1000.00");
        String creatorWalletAddress = "0x1234567890abcdef";
        BigDecimal proposalFee = new BigDecimal("100.00");
        NetworkType networkType = NetworkType.ETHEREUM;

        // when
        CreateProposalRequest request = new CreateProposalRequest(
            creatorId, longTitle, longDescription, votingStartDate, votingEndDate,
            requiredQuorum, creatorWalletAddress, proposalFee, networkType
        );

        // then
        assertThat(request).isNotNull();
        assertThat(request.getTitle()).isEqualTo(longTitle);
        assertThat(request.getDescription()).isEqualTo(longDescription);
        assertThat(request.getTitle().length()).isGreaterThan(30);
        assertThat(request.getDescription().length()).isGreaterThan(80);
    }

    @Test
    @DisplayName("다양한 NetworkType으로 CreateProposalRequest를 생성할 수 있다")
    void createCreateProposalRequestWithDifferentNetworkTypes() {
        // given
        UUID creatorId = UUID.randomUUID();
        String title = "네트워크 테스트";
        String description = "네트워크 테스트 설명";
        LocalDateTime votingStartDate = LocalDateTime.now().plusDays(1);
        LocalDateTime votingEndDate = LocalDateTime.now().plusDays(7);
        BigDecimal requiredQuorum = new BigDecimal("1000.00");
        String creatorWalletAddress = "0x1234567890abcdef";
        BigDecimal proposalFee = new BigDecimal("100.00");

        // when & then
        CreateProposalRequest ethereumRequest = new CreateProposalRequest(
            creatorId, title, description, votingStartDate, votingEndDate,
            requiredQuorum, creatorWalletAddress, proposalFee, NetworkType.ETHEREUM
        );
        assertThat(ethereumRequest.getNetworkType()).isEqualTo(NetworkType.ETHEREUM);

        CreateProposalRequest solanaRequest = new CreateProposalRequest(
            creatorId, title, description, votingStartDate, votingEndDate,
            requiredQuorum, creatorWalletAddress, proposalFee, NetworkType.SOLANA
        );
        assertThat(solanaRequest.getNetworkType()).isEqualTo(NetworkType.SOLANA);
    }
} 