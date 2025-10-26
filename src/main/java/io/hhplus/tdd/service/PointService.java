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
//        serviceValidation.isValidUserId(userId);
        return userPointTable.selectById(userId);
    }
}
