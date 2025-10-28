package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

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

    /**
     * UseCase : 특정 유저 포인트 조회
     * findUserPointByUserId
     */
    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 0 포인트를 반환한다")
    void findUserPointByUserId_NoneUser_NonePoint() {
        long userId = 1L;

        UserPoint result = pointService.findUserPointByUserId(userId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(0L);
    }

    @Test
    @DisplayName("기존 사용자의 포인트를 정상 조회한다")
    void findUserPointByUserId_ExistingUser_Success() {
        long userId = 1L;
        long existingPoint = 1000L;
        userPointTable.insertOrUpdate(userId, existingPoint);

        UserPoint result = pointService.findUserPointByUserId(userId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(existingPoint);
    }

    @Test
    @DisplayName("다른 사용자의 포인트는 조회되지 않는다")
    void findUserPointByUserId_DifferentUsers_NotFind() {
        long userId1 = 1L;
        long userId2 = 2L;
        userPointTable.insertOrUpdate(userId1, 1000L);
        userPointTable.insertOrUpdate(userId2, 2000L);

        UserPoint result = pointService.findUserPointByUserId(userId1);

        assertThat(result.id()).isEqualTo(userId1);
        assertThat(result.point()).isEqualTo(1000L);
        assertThat(result.point()).isNotEqualTo(2000L);
    }

    @Test
    @DisplayName("음수 사용자 ID로 조회 시 정상 동작한다")
    void findUserPointByUserId_NegativeUserId_Success() {
        long userId = -1L;
        UserPoint result = pointService.findUserPointByUserId(userId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(0L);
    }

    /**
     * UseCase : 유저 포인트 충전
     * userPointCharge
     */
    @Test
    @DisplayName("정상적으로 충전 성공")
    void userPointCharge_Success() {
        long userId = 1L;
        long amount = 10000L;

        UserPoint result = pointService.userPointCharge(userId, amount);

        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(amount);
    }

    // ==================== Charge: 신규 유저 충전 성공 ====================
// @Test
// @DisplayName("신규 유저 - 첫 충전 성공 시 잔액이 amount가 된다")
// void userPointCharge_NewUser_Success() {
//     // given
//     // long userId = ...
//     // long amount = ...; // MIN_CHARGE 이상 유효 금액
//
//     // when
//     // UserPoint result = pointService.userPointCharge(userId, amount);
//
//     // then
//     // assertThat(result.id()).isEqualTo(userId);
//     // assertThat(result.point()).isEqualTo(amount);
//     // (선택) 히스토리 존재 여부는 별도 테스트에서 검증
// }


// ==================== Charge: 기존 유저 누적 충전 ====================
// @Test
// @DisplayName("기존 유저 - 충전 시 기존 잔액에 누적된다")
// void userPointCharge_ExistingUser_Accumulates() {
//     // given
//     // long userId = ...
//     // long initial = ...;   // 예: 5_000
//     // long amount  = ...;   // 예: 7_000
//     // userPointTable.insertOrUpdate(userId, initial); // 초기 잔액 세팅
//
//     // when
//     // UserPoint result = pointService.userPointCharge(userId, amount);
//
//     // then
//     // long expected = initial + amount;
//     // assertThat(result.point()).isEqualTo(expected);
//     // assertThat(pointService.findUserPointByUserId(userId).point()).isEqualTo(expected);
// }


// ==================== Charge: 음수 금액 예외 ====================
// @Test
// @DisplayName("충전 금액이 음수면 예외 발생")
// void userPointCharge_NegativeAmount_Throws() {
//     // given
//     // long userId = ...
//     // long amount = -1L;
//
//     // when & then
//     // assertThatThrownBy(() -> pointService.userPointCharge(userId, amount))
//     //     .isInstanceOf(IllegalArgumentException.class); // 또는 도메인 예외/상태 확인
// }


// ==================== Charge: 0원 충전 처리(정책 확정) ====================
// @Test
// @DisplayName("충전 금액이 0원이면 예외(또는 NO-OP) - 정책을 테스트로 확정")
// void userPointCharge_ZeroAmount_Policy() {
//     // given
//     // long userId = ...
//     // long amount = 0L;
//
//     // when & then (택1)
//     // A) 예외 정책
//     // assertThatThrownBy(() -> pointService.userPointCharge(userId, amount))
//     //     .isInstanceOf(IllegalArgumentException.class);
//
//     // B) NO-OP 정책
//     // long before = pointService.findUserPointByUserId(userId).point();
//     // UserPoint after = pointService.userPointCharge(userId, amount);
//     // assertThat(after.point()).isEqualTo(before);
// }


// ==================== Charge: 최소/최대 금액 경계값 ====================
// @Test
// @DisplayName("충전 금액이 최소 미만이면 예외")
// void userPointCharge_BelowMin_Throws() {
//     // given
//     // long userId = ...
//     // long amount = MIN_CHARGE - 1; // 네 프로젝트 규칙으로 치환
//
//     // when & then
//     // assertThatThrownBy(() -> pointService.userPointCharge(userId, amount))
//     //     .isInstanceOf(IllegalArgumentException.class);
// }
//
// // @Test
// // @DisplayName("충전 금액이 최대 초과이면 예외")
// // void userPointCharge_AboveMax_Throws() {
// //     // given
// //     // long userId = ...
// //     // long amount = MAX_CHARGE + 1; // 규칙에 맞게 치환
// //
// //     // when & then
// //     // assertThatThrownBy(() -> pointService.userPointCharge(userId, amount))
// //     //     .isInstanceOf(IllegalArgumentException.class);
// // }


// ==================== Charge: 히스토리 생성 확인 ====================
// @Test
// @DisplayName("충전 성공 시 히스토리가 1건 추가되고 타입/금액이 일치한다")
// void userPointCharge_HistoryCreated() {
//     // given
//     // long userId = ...
//     // long amount = ...; // 유효 금액
//
//     // when
//     // pointService.userPointCharge(userId, amount);
//
//     // then
//     // List<PointHistory> histories = pointService.findAllPointHistoryByUserId(userId);
//     // assertThat(histories).hasSize(1);
//     // assertThat(histories.get(0).type()).isEqualTo(TransactionType.CHARGE);
//     // assertThat(histories.get(0).amount()).isEqualTo(amount);
// }


// ==================== Charge: 연속 충전 누적 + 히스토리 건수 ====================
// @Test
// @DisplayName("여러 번 연속 충전 시 잔액이 누적되고 히스토리도 누적된다")
// void userPointCharge_MultipleCharges_AccumulatesAndHistory() {
//     // given
//     // long userId = ...
//     // long a1 = ...; // 예: 10_000
//     // long a2 = ...; // 예: 20_000
//     // long a3 = ...; // 예: 30_000
//
//     // when
//     // pointService.userPointCharge(userId, a1);
//     // pointService.userPointCharge(userId, a2);
//     // UserPoint result = pointService.userPointCharge(userId, a3);
//
//     // then
//     // long expected = a1 + a2 + a3;
//     // assertThat(result.point()).isEqualTo(expected);
//     // List<PointHistory> histories = pointService.findAllPointHistoryByUserId(userId);
//     // assertThat(histories).hasSize(3);
//     // assertThat(histories).allMatch(h -> h.type() == TransactionType.CHARGE);
// }


// ==================== Charge: 대용량(경계 근접) 금액 처리 ====================
// @Test
// @DisplayName("충전 금액이 큰 편이어도(허용 범위 내) 정상 누적된다")
// void userPointCharge_LargeAmount_WithinLimit_Success() {
//     // given
//     // long userId = ...
//     // long amount = ...; // MAX_CHARGE 또는 그 근처의 허용 값
//
//     // when
//     // UserPoint result = pointService.userPointCharge(userId, amount);
//
//     // then
//     // assertThat(result.point()).isEqualTo(amount);
// }

}