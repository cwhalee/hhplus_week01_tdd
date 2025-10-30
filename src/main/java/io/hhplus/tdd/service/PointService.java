package io.hhplus.tdd.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import org.springframework.stereotype.Service;

@Service
public class PointService {

    private final UserPointTable userPointTable;

    public PointService(
            UserPointTable userPointTable,
            PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
    }

    /** findUserPointByUserId
     * userid 로 userPoint 조회
     * @param userId
     * @return UserPoint
     */
    public UserPoint findUserPointByUserId(long userId) {
        return userPointTable.selectById(userId);
    }

    /** userPointCharge
     * userid 로 amount 생성 or 수정
     * @param userId, amount
     * @return UserPoint
     */
    public UserPoint userPointCharge(long userId, long amount) {
        UserPoint userPoint = new UserPoint();

        return userPointTable.insertOrUpdate(userId, chargeAmount);
    }
}
