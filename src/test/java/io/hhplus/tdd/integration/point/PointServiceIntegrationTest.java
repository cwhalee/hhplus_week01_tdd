package io.hhplus.tdd.integration.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.*;
import io.hhplus.tdd.service.PointService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PointServiceIntegrationTest {

    @TestConfiguration
    static class TestBeans {
        @Bean UserPointTable userPointTable() { return new UserPointTable(); }
        @Bean PointHistoryTable pointHistoryTable() { return new PointHistoryTable(); }
    }

    @Autowired PointService pointService;

    // ============ findUserPointByUserId ============
    @Test
    @DisplayName("신규 유저 조회 시 point=0")
    void findUserPointByUserId_newUser_zero() {
        UserPoint up = pointService.findUserPointByUserId(100L);
        Assertions.assertEquals(0L, up.point());
    }

    @Test
    @DisplayName("유효하지 않은 userId 조회 시 예외")
    void findUserPointByUserId_invalid_throws() {
        UserPointException ex = Assertions.assertThrows(UserPointException.class,
                () -> pointService.findUserPointByUserId(-1L));
        Assertions.assertEquals(POINT_STATUS.INVALID_USERID, ex.status);
    }

    // ============ userPointCharge ============
    @Test
    @DisplayName("충전 성공 시 포인트 반영 + 히스토리 기록")
    void userPointCharge_success_history() {
        long userId = 1L;
        pointService.userPointCharge(userId, 10_000L);

        UserPoint up = pointService.findUserPointByUserId(userId);
        List<PointHistory> histories = pointService.findAllPointHistoryByUserId(userId);

        Assertions.assertEquals(10_000L, up.point());
        Assertions.assertEquals(1, histories.size());
        Assertions.assertEquals(TransactionType.CHARGE, histories.get(0).type());
        Assertions.assertEquals(10_000L, histories.get(0).amount());
    }

    @Test
    @DisplayName("충전 최소/최대 범위 위반 시 예외")
    void userPointCharge_range_throws() {
        Assertions.assertThrows(UserPointException.class,
                () -> pointService.userPointCharge(1L, 9_999L));    // min=10_000
        Assertions.assertThrows(UserPointException.class,
                () -> pointService.userPointCharge(1L, 200_001L));  // max=200_000
    }

    // ============ userPointUse ============
    @Test
    @DisplayName("사용 성공 시 포인트 감소 + 히스토리 기록")
    void userPointUse_success_history() {
        long userId = 2L;
        pointService.userPointCharge(userId, 20_000L);
        pointService.userPointUse(userId, 1_000L);

        UserPoint up = pointService.findUserPointByUserId(userId);
        List<PointHistory> histories = pointService.findAllPointHistoryByUserId(userId);

        Assertions.assertEquals(19_000L, up.point());
        Assertions.assertEquals(2, histories.size()); // CHARGE + USE
        Assertions.assertEquals(TransactionType.USE, histories.get(1).type());
        Assertions.assertEquals(1_000L, histories.get(1).amount());
    }

    @Test
    @DisplayName("사용 금액 범위/잔액 부족 예외")
    void userPointUse_range_underflow_throws() {
        long userId = 3L;
        pointService.userPointCharge(userId, 10_000L);

        Assertions.assertThrows(UserPointException.class,
                () -> pointService.userPointUse(userId, 900L));       // minUse=1_000
        Assertions.assertThrows(UserPointException.class,
                () -> pointService.userPointUse(userId, 1_000_001L)); // maxUse=1_000_000
        Assertions.assertThrows(UserPointException.class,
                () -> pointService.userPointUse(userId, 20_000L));    // 잔액부족
    }
}
