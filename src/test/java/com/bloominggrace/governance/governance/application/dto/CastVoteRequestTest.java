package com.bloominggrace.governance.governance.application.dto;

import com.bloominggrace.governance.governance.domain.model.VoteType;
import com.bloominggrace.governance.wallet.domain.model.NetworkType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CastVoteRequest DTO 테스트")
class CastVoteRequestTest {

    @Test
    @DisplayName("기본 생성자로 CastVoteRequest를 생성할 수 있다")
    void createCastVoteRequestWithDefaultConstructor() {
        // when
        CastVoteRequest request = new CastVoteRequest();

        // then
        assertThat(request).isNotNull();
        assertThat(request.getVoterId()).isNull();
        assertThat(request.getVoteType()).isNull();
        assertThat(request.getReason()).isNull();
        assertThat(request.getVoterWalletAddress()).isNull();
        assertThat(request.getNetworkType()).isNull();
    }

    @Test
    @DisplayName("모든 필드를 포함한 생성자로 CastVoteRequest를 생성할 수 있다")
    void createCastVoteRequestWithAllArgsConstructor() {
        // given
        UUID voterId = UUID.randomUUID();
        VoteType voteType = VoteType.YES;
        String reason = "이 제안에 찬성합니다";
        String walletAddress = "0x1234567890abcdef";
        NetworkType networkType = NetworkType.ETHEREUM;

        // when
        CastVoteRequest request = new CastVoteRequest(voterId, voteType, reason, walletAddress, networkType);

        // then
        assertThat(request).isNotNull();
        assertThat(request.getVoterId()).isEqualTo(voterId);
        assertThat(request.getVoteType()).isEqualTo(voteType);
        assertThat(request.getReason()).isEqualTo(reason);
        assertThat(request.getVoterWalletAddress()).isEqualTo(walletAddress);
        assertThat(request.getNetworkType()).isEqualTo(networkType);
    }

    @Test
    @DisplayName("CastVoteRequest의 setter와 getter가 올바르게 작동한다")
    void castVoteRequestSettersAndGetters() {
        // given
        CastVoteRequest request = new CastVoteRequest();
        UUID voterId = UUID.randomUUID();
        VoteType voteType = VoteType.NO;
        String reason = "이 제안에 반대합니다";
        String walletAddress = "0xabcdef1234567890";
        NetworkType networkType = NetworkType.SOLANA;

        // when
        request.setVoterId(voterId);
        request.setVoteType(voteType);
        request.setReason(reason);
        request.setVoterWalletAddress(walletAddress);
        request.setNetworkType(networkType);

        // then
        assertThat(request.getVoterId()).isEqualTo(voterId);
        assertThat(request.getVoteType()).isEqualTo(voteType);
        assertThat(request.getReason()).isEqualTo(reason);
        assertThat(request.getVoterWalletAddress()).isEqualTo(walletAddress);
        assertThat(request.getNetworkType()).isEqualTo(networkType);
    }

    @Test
    @DisplayName("동일한 값을 가진 CastVoteRequest들이 같다")
    void castVoteRequestEquality() {
        // given
        UUID voterId = UUID.randomUUID();
        VoteType voteType = VoteType.ABSTAIN;
        String reason = "기권합니다";
        String walletAddress = "0x1234567890abcdef";
        NetworkType networkType = NetworkType.ETHEREUM;

        CastVoteRequest request1 = new CastVoteRequest(voterId, voteType, reason, walletAddress, networkType);
        CastVoteRequest request2 = new CastVoteRequest(voterId, voteType, reason, walletAddress, networkType);

        // when & then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    @DisplayName("다른 값을 가진 CastVoteRequest들은 다르다")
    void castVoteRequestInequality() {
        // given
        UUID voterId1 = UUID.randomUUID();
        UUID voterId2 = UUID.randomUUID();
        VoteType voteType = VoteType.YES;
        String reason = "찬성합니다";
        String walletAddress = "0x1234567890abcdef";
        NetworkType networkType = NetworkType.ETHEREUM;

        CastVoteRequest request1 = new CastVoteRequest(voterId1, voteType, reason, walletAddress, networkType);
        CastVoteRequest request2 = new CastVoteRequest(voterId2, voteType, reason, walletAddress, networkType);

        // when & then
        assertThat(request1).isNotEqualTo(request2);
        assertThat(request1.hashCode()).isNotEqualTo(request2.hashCode());
    }

    @Test
    @DisplayName("CastVoteRequest의 toString이 올바르게 작동한다")
    void castVoteRequestToString() {
        // given
        UUID voterId = UUID.randomUUID();
        VoteType voteType = VoteType.YES;
        String reason = "찬성합니다";
        String walletAddress = "0x1234567890abcdef";
        NetworkType networkType = NetworkType.ETHEREUM;

        CastVoteRequest request = new CastVoteRequest(voterId, voteType, reason, walletAddress, networkType);

        // when
        String toString = request.toString();

        // then
        assertThat(toString).isNotNull();
        assertThat(toString).contains(voterId.toString());
        assertThat(toString).contains(voteType.toString());
        assertThat(toString).contains(reason);
        assertThat(toString).contains(walletAddress);
        assertThat(toString).contains(networkType.toString());
    }

    @Test
    @DisplayName("null 값을 가진 CastVoteRequest를 생성할 수 있다")
    void createCastVoteRequestWithNullValues() {
        // when
        CastVoteRequest request = new CastVoteRequest(null, null, null, null, null);

        // then
        assertThat(request).isNotNull();
        assertThat(request.getVoterId()).isNull();
        assertThat(request.getVoteType()).isNull();
        assertThat(request.getReason()).isNull();
        assertThat(request.getVoterWalletAddress()).isNull();
        assertThat(request.getNetworkType()).isNull();
    }

    @Test
    @DisplayName("빈 문자열을 가진 CastVoteRequest를 생성할 수 있다")
    void createCastVoteRequestWithEmptyStrings() {
        // given
        UUID voterId = UUID.randomUUID();
        VoteType voteType = VoteType.YES;
        String emptyReason = "";
        String emptyWalletAddress = "";
        NetworkType networkType = NetworkType.ETHEREUM;

        // when
        CastVoteRequest request = new CastVoteRequest(voterId, voteType, emptyReason, emptyWalletAddress, networkType);

        // then
        assertThat(request).isNotNull();
        assertThat(request.getVoterId()).isEqualTo(voterId);
        assertThat(request.getVoteType()).isEqualTo(voteType);
        assertThat(request.getReason()).isEqualTo(emptyReason);
        assertThat(request.getVoterWalletAddress()).isEqualTo(emptyWalletAddress);
        assertThat(request.getNetworkType()).isEqualTo(networkType);
    }

    @Test
    @DisplayName("긴 이유 텍스트를 가진 CastVoteRequest를 생성할 수 있다")
    void createCastVoteRequestWithLongReason() {
        // given
        UUID voterId = UUID.randomUUID();
        VoteType voteType = VoteType.YES;
        String longReason = "이 제안에 찬성하는 이유는 다음과 같습니다. " +
            "첫째, 이 제안은 커뮤니티의 장기적인 이익을 위해 필요합니다. " +
            "둘째, 기술적으로 실현 가능하며 안전합니다. " +
            "셋째, 투명하고 공정한 절차를 통해 진행되었습니다.";
        String walletAddress = "0x1234567890abcdef";
        NetworkType networkType = NetworkType.ETHEREUM;

        // when
        CastVoteRequest request = new CastVoteRequest(voterId, voteType, longReason, walletAddress, networkType);

        // then
        assertThat(request).isNotNull();
        assertThat(request.getReason()).isEqualTo(longReason);
        assertThat(request.getReason().length()).isGreaterThan(100);
    }

    @Test
    @DisplayName("다양한 VoteType으로 CastVoteRequest를 생성할 수 있다")
    void createCastVoteRequestWithDifferentVoteTypes() {
        // given
        UUID voterId = UUID.randomUUID();
        String reason = "테스트";
        String walletAddress = "0x1234567890abcdef";
        NetworkType networkType = NetworkType.ETHEREUM;

        // when & then
        CastVoteRequest yesRequest = new CastVoteRequest(voterId, VoteType.YES, reason, walletAddress, networkType);
        assertThat(yesRequest.getVoteType()).isEqualTo(VoteType.YES);

        CastVoteRequest noRequest = new CastVoteRequest(voterId, VoteType.NO, reason, walletAddress, networkType);
        assertThat(noRequest.getVoteType()).isEqualTo(VoteType.NO);

        CastVoteRequest abstainRequest = new CastVoteRequest(voterId, VoteType.ABSTAIN, reason, walletAddress, networkType);
        assertThat(abstainRequest.getVoteType()).isEqualTo(VoteType.ABSTAIN);
    }

    @Test
    @DisplayName("다양한 NetworkType으로 CastVoteRequest를 생성할 수 있다")
    void createCastVoteRequestWithDifferentNetworkTypes() {
        // given
        UUID voterId = UUID.randomUUID();
        VoteType voteType = VoteType.YES;
        String reason = "테스트";
        String walletAddress = "0x1234567890abcdef";

        // when & then
        CastVoteRequest ethereumRequest = new CastVoteRequest(voterId, voteType, reason, walletAddress, NetworkType.ETHEREUM);
        assertThat(ethereumRequest.getNetworkType()).isEqualTo(NetworkType.ETHEREUM);

        CastVoteRequest solanaRequest = new CastVoteRequest(voterId, voteType, reason, walletAddress, NetworkType.SOLANA);
        assertThat(solanaRequest.getNetworkType()).isEqualTo(NetworkType.SOLANA);
    }
} 