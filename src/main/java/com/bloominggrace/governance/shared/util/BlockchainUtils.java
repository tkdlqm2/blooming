package com.bloominggrace.governance.shared.util;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class BlockchainUtils {

    // Sepolia 네트워크 설정 (12초 블록 시간)
    private static final long BLOCK_TIME_SECONDS = 12;
    private static final long BLOCKS_PER_DAY = 24 * 60 * 60 / BLOCK_TIME_SECONDS; // 7200 블록/일

    /**
     * 날짜/시간을 블록 높이로 변환
     *
     * @param targetDateTime        목표 날짜/시간
     * @param currentBlockNumber    현재 블록 번호
     * @param currentBlockTimestamp 현재 블록의 타임스탬프
     * @return 목표 블록 번호
     */
    public static BigInteger convertDateTimeToBlockNumber(
            LocalDateTime targetDateTime,
            BigInteger currentBlockNumber,
            long currentBlockTimestamp) {

        // 목표 시간을 타임스탬프로 변환
        long targetTimestamp = targetDateTime.toEpochSecond(ZoneOffset.UTC);

        // 시간 차이 계산 (초 단위)
        long timeDifferenceSeconds = targetTimestamp - currentBlockTimestamp;

        // 블록 차이 계산
        long blockDifference = timeDifferenceSeconds / BLOCK_TIME_SECONDS;

        // 목표 블록 번호 계산
        return currentBlockNumber.add(BigInteger.valueOf(blockDifference));
    }

    /**
     * 블록 번호를 날짜/시간으로 변환
     *
     * @param blockNumber           블록 번호
     * @param currentBlockNumber    현재 블록 번호
     * @param currentBlockTimestamp 현재 블록의 타임스탬프
     * @return 예상 날짜/시간
     */
    public static LocalDateTime convertBlockNumberToDateTime(
            BigInteger blockNumber,
            BigInteger currentBlockNumber,
            long currentBlockTimestamp) {

        // 블록 차이 계산
        BigInteger blockDifference = blockNumber.subtract(currentBlockNumber);

        // 시간 차이 계산 (초 단위)
        long timeDifferenceSeconds = blockDifference.longValue() * BLOCK_TIME_SECONDS;

        // 목표 타임스탬프 계산
        long targetTimestamp = currentBlockTimestamp + timeDifferenceSeconds;

        return LocalDateTime.ofEpochSecond(targetTimestamp, 0, ZoneOffset.UTC);
    }

    /**
     * 일수를 블록 수로 변환
     *
     * @param days 일수
     * @return 블록 수
     */
    public static BigInteger convertDaysToBlocks(long days) {
        return BigInteger.valueOf(days * BLOCKS_PER_DAY);
    }

    /**
     * 블록 수를 일수로 변환
     *
     * @param blocks 블록 수
     * @return 일수
     */
    public static long convertBlocksToDays(BigInteger blocks) {
        return blocks.longValue() / BLOCKS_PER_DAY;
    }

    /**
     * 시간을 블록 수로 변환
     *
     * @param hours 시간
     * @return 블록 수
     */
    public static BigInteger convertHoursToBlocks(long hours) {
        return BigInteger.valueOf(hours * 60 * 60 / BLOCK_TIME_SECONDS);
    }

    /**
     * 분을 블록 수로 변환
     *
     * @param minutes 분
     * @return 블록 수
     */
    public static BigInteger convertMinutesToBlocks(long minutes) {
        return BigInteger.valueOf(minutes * 60 / BLOCK_TIME_SECONDS);
    }
}