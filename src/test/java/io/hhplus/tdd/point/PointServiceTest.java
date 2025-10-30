package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

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

    /**
     * UseCase : 유저 포인트 조회
     * PointService.findUserPointByUserId
     */

    @Test
    @DisplayName("기존 사용자의 포인트를 정상 조회")
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
    @DisplayName("존재하지 않는 사용자 조회 시 Point가 0으로 반환")
    void findUserPointByUserId_NoneUser_NonePoint() {
        long userId = 1L;

        UserPoint result = pointService.findUserPointByUserId(userId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(0L);
    }

    @Test
    @DisplayName("음수 ID로 조회 시 예외 발생")
    void findUserPointByUserId_NegativeUserId_Success() {
        long userId = -1L;
        UserPoint result = pointService.findUserPointByUserId(userId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.point()).isEqualTo(0L);
    }

    /**
     * UseCase : 유저 포인트 충전
     * PointService.userPointCharge
     */
     @Test
     @DisplayName("기존 유저 충전 성공")
     void userPointCharge_ExistingUser_Success() {
          long userId = 1L;
          long amount = 10000L;

          UserPoint result = pointService.userPointCharge(userId, amount);

          assertThat(result.id()).isEqualTo(userId);
          assertThat(result.point()).isEqualTo(amount);
     }

     @Test
     @DisplayName("충전 금액이 최소 미만이면 예외")
     void userPointCharge_BelowMin_Throws() {
          long userId = 1L;
          long amount = 1001L;

          assertThatThrownBy(() -> pointService.userPointCharge(userId, amount))
              .isInstanceOf(IllegalArgumentException.class);
     }

      @Test
      @DisplayName("충전 금액이 최대 초과이면 예외")
      void userPointCharge_AboveMax_Throws() {
           long userId = 1L;
           long amount = 200_001L;

           assertThatThrownBy(() -> pointService.userPointCharge(userId, amount))
               .isInstanceOf(IllegalArgumentException.class);
      }

     @Test
     @DisplayName("충전 성공 시 히스토리가 1건 추가되고 타입/금액이 일치")
     void userPointCharge_HistoryCreated() {
          long userId = 1L;
          long amount = 10000L;

          pointService.userPointCharge(userId, amount);

          List<PointHistory> histories = pointHistoryTable.selectAllByUserId(userId);
          assertThat(histories.get(0).type()).isEqualTo(TransactionType.CHARGE);
          assertThat(histories.get(0).amount()).isEqualTo(amount);
     }

}