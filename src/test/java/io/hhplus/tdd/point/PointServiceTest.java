package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PointServiceTest {

    private PointService pointService;
    private UserPointTable userPointTable;

    @BeforeEach
    void setUp() {
        userPointTable = new UserPointTable();
        PointHistoryTable pointHistoryTable = new PointHistoryTable();
        pointService = new PointService(userPointTable, pointHistoryTable);
    }

    // ==================== Use Case: 포인트 조회 ====================

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 0 포인트를 반환한다")
    void findUserPointByUserId_NewUser_ReturnsZeroPoint() {
        // given: 존재하지 않는 사용자 ID
        // - 아직 한 번도 포인트를 사용하지 않은 신규 사용자
        long userId = 1L;

        // when: 포인트 조회
        UserPoint result = pointService.findUserPointByUserId(userId);

        // then: 0 포인트를 가진 UserPoint 반환
        // - UserPointTable.empty() 메서드를 통해 생성된 객체
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(0L);
    }

    @Test
    @DisplayName("기존 사용자의 포인트를 정상 조회한다")
    void findUserPointByUserId_ExistingUser_ReturnsCorrectPoint() {
        // given: 이미 포인트가 있는 사용자
        // - 1000 포인트를 보유한 사용자를 미리 생성
        long userId = 1L;
        long existingPoint = 1000L;
        userPointTable.insertOrUpdate(userId, existingPoint);

        // when: 포인트 조회
        UserPoint result = pointService.findUserPointByUserId(userId);

        // then: 저장된 포인트가 정확히 조회됨
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(existingPoint);
    }

    @Test
    @DisplayName("다른 사용자의 포인트는 조회되지 않는다")
    void findUserPointByUserId_DifferentUsers_ReturnsIndependentPoints() {
        // given: 여러 사용자가 각각 다른 포인트를 보유
        // - 사용자마다 독립적인 포인트 관리
        long userId1 = 1L;
        long userId2 = 2L;
        userPointTable.insertOrUpdate(userId1, 1000L);
        userPointTable.insertOrUpdate(userId2, 2000L);

        // when: 사용자1의 포인트 조회
        UserPoint result = pointService.findUserPointByUserId(userId1);

        // then: 사용자1의 포인트만 조회됨 (사용자2의 포인트가 아님)
        assertThat(result.id()).isEqualTo(userId1);
        assertThat(result.point()).isEqualTo(1000L);
        assertThat(result.point()).isNotEqualTo(2000L); // 명시적으로 다른 사용자 포인트가 아님을 확인
    }

    @Test
    @DisplayName("음수 사용자 ID로 조회 시 정상 동작한다")
    void findUserPointByUserId_NegativeUserId_ReturnsResult() {
        // given: 음수 사용자 ID (비정상적이지만 시스템이 처리 가능한 경우)
        // - 실제로는 validation이 필요하지만, 현재는 Table이 처리
        long userId = -1L;

        // when: 포인트 조회
        UserPoint result = pointService.findUserPointByUserId(userId);

        // then: 0 포인트 반환 (예외가 발생하지 않음)
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(0L);
    }
}