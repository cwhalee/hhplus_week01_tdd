package io.hhplus.tdd.unit.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.UserPointException;
import io.hhplus.tdd.service.PointService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * PointService 단위 테스트
 * - 스프링 컨텍스트 없이 순수 자바 객체로 검증
 * - 내부 비즈니스 로직의 동작과 예외 흐름을 빠르게 확인
 */
class PointServiceTest {

    private PointService pointService;
    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;

    @BeforeEach
    void setUp() {
        userPointTable = new UserPointTable();
        pointHistoryTable = new PointHistoryTable();
        pointService = new PointService(userPointTable, pointHistoryTable);
    }

    // ==================== findUserPointByUserId ====================

    /**
     * findUserPointByUserId
     * 신규 사용자 조회 시 point = 0 반환
     */
    @Test
    @DisplayName("findUserPointByUserId - 신규 사용자 조회 시 point = 0")
    void findUserPointByUserId_NewUser_ReturnsZeroPoint() {
        UserPoint result = pointService.findUserPointByUserId(1L);
        Assertions.assertEquals(0L, result.point());
    }

    /**
     * findUserPointByUserId
     * 기존 사용자의 포인트 조회
     */
    @Test
    @DisplayName("findUserPointByUserId - 기존 사용자 조회 시 포인트 정상 반환")
    void findUserPointByUserId_ExistingUser_ReturnsCorrectPoint() {
        long userId = 1L;
        userPointTable.insertOrUpdate(userId, 100_000L);

        UserPoint result = pointService.findUserPointByUserId(userId);

        Assertions.assertEquals(userId, result.id());
        Assertions.assertEquals(100_000L, result.point());
    }

    // ==================== userPointCharge ====================

    /**
     * userPointCharge
     * 정상 충전 시 포인트 및 히스토리 검증
     */
    @Test
    @DisplayName("userPointCharge - 정상 충전 시 포인트 및 히스토리 생성 검증")
    void userPointCharge_Success() {
        long userId = 1L;
        long amount = 10_000L;

        pointService.userPointCharge(userId, amount);

        UserPoint userPoint = pointService.findUserPointByUserId(userId);
        List<PointHistory> histories = pointHistoryTable.selectAllByUserId(userId);

        Assertions.assertEquals(10_000L, userPoint.point());
        Assertions.assertEquals(1, histories.size());
        Assertions.assertEquals(TransactionType.CHARGE, histories.get(0).type());
        Assertions.assertEquals(10_000L, histories.get(0).amount());
    }

    /**
     * userPointCharge
     * 최소 충전 금액 미만 시 예외 발생
     */
    @Test
    @DisplayName("userPointCharge - 최소 충전 금액 미만 시 예외 발생")
    void userPointCharge_BelowMin_ThrowsException() {
        long userId = 1L;
        long amount = 9_999L; // minCharge = 10,000

        Assertions.assertThrows(UserPointException.class,
                () -> pointService.userPointCharge(userId, amount));
    }

    /**
     * userPointCharge
     * 최대 충전 금액 초과 시 예외 발생
     */
    @Test
    @DisplayName("userPointCharge - 최대 충전 금액 초과 시 예외 발생")
    void userPointCharge_AboveMax_ThrowsException() {
        long userId = 1L;
        long amount = 200_001L; // maxCharge = 200,000

        Assertions.assertThrows(UserPointException.class,
                () -> pointService.userPointCharge(userId, amount));
    }

    // ==================== userPointUse ====================

    /**
     * userPointUse
     * 정상 사용 시 포인트 차감 및 히스토리 검증
     */
    @Test
    @DisplayName("userPointUse - 정상 사용 시 포인트 차감 및 히스토리 검증")
    void userPointUse_Success() {
        long userId = 1L;

        pointService.userPointCharge(userId, 100_000L);
        pointService.userPointUse(userId, 10_000L);

        UserPoint userPoint = pointService.findUserPointByUserId(userId);
        List<PointHistory> histories = pointHistoryTable.selectAllByUserId(userId);

        Assertions.assertEquals(90_000L, userPoint.point());
        Assertions.assertEquals(2, histories.size());
        Assertions.assertEquals(TransactionType.USE, histories.get(1).type());
        Assertions.assertEquals(10_000L, histories.get(1).amount());
    }

    /**
     * userPointUse
     * 최소 사용 금액 미만 시 예외 발생
     */
    @Test
    @DisplayName("userPointUse - 최소 사용 금액 미만 시 예외 발생")
    void userPointUse_BelowMin_ThrowsException() {
        long userId = 1L;

        pointService.userPointCharge(userId, 100_000L);

        Assertions.assertThrows(UserPointException.class,
                () -> pointService.userPointUse(userId, 500L)); // minUse = 1,000
    }

    /**
     * userPointUse
     * 사용 금액이 보유 포인트 초과 시 예외 발생
     */
    @Test
    @DisplayName("userPointUse - 보유 포인트 초과 사용 시 예외 발생")
    void userPointUse_OverBalance_ThrowsException() {
        long userId = 1L;

        pointService.userPointCharge(userId, 10_000L);

        Assertions.assertThrows(UserPointException.class,
                () -> pointService.userPointUse(userId, 20_000L));
    }
}
