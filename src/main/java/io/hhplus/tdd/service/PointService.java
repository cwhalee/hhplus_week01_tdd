package io.hhplus.tdd.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public PointService(
            UserPointTable userPointTable,
            PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    /**
     * userId의 유효성을 검증하는 메서드
     * - 0 이하의 값이면 INVALID_USERID 예외 발생
     */
    public void validateUserId(long userId) {
        if (userId <= 0) {
            throw new UserPointException(POINT_STATUS.INVALID_USERID);
        }
    }

    /** findUserPointByUserId
     * userid 로 userPoint 조회
     * @param userId
     * @return UserPoint
     */
    public UserPoint findUserPointByUserId(long userId) {
        return userPointTable.selectById(userId);
    }

    /** findAllPointHistoryByUserId
     * userid 로 모든 userPointHistory 조회
     * @param userId
     * @return List<PointHistory>
     */
    public List<PointHistory> findAllPointHistoryByUserId(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    /** userPointCharge
     * userid 로 amount 생성 or 수정 후 history 생성
     * @param userId, amount
     * @return UserPoint
     */
    public UserPoint userPointCharge(long userId, long amount) {
        UserPoint current = findUserPointByUserId(userId);     
        long newPoint = current.charge(amount);                
        UserPoint updated = userPointTable.insertOrUpdate(userId, newPoint);

        // history 생성
        pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
        return updated;
    }

    /** userPointUse
     * userid 로 amount 만큼 충전금액에서 사용 후 history 생성
     * @param userId, amount
     * @return UserPoint
     */
    public UserPoint userPointUse(long userId, long amount) {
        UserPoint current = findUserPointByUserId(userId);
        long newPoint = current.use(amount);                   
        UserPoint updated = userPointTable.insertOrUpdate(userId, newPoint);

        pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());
        return updated;
    }
}