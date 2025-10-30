package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {
    private static final long maxPoint       = 2_000_000L;
    private static final long maxUsePoint    = 1_000_000L;
    private static final long minUsePoint    = 1_000L;
    private static final long maxChargePoint = 200_000L;
    private static final long minChargePoint = 10_000L;

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    /**
     * 충전할 포인트를 입력받고 검증하는 함수
     * - 최소 충전 금액 보다 충전하려는 금액이 작으면  예외
     * - 최대 충전 금액 보다 충전하려는 금액이 크면    예외
     * - 충전 시 금액이 최대 보유 포인트 보다 큰 경우  예외
     * 모든 조건 통과 시 충전 시 금액을 반환
     * @param pointToCharge
     * @return
     */
    public long charge(long pointToCharge) {
        if(minChargePoint > pointToCharge || maxChargePoint < pointToCharge){
            throw new UserPointException(POINT_STATUS.INVALID_CHARGE_AMOUNT);
        }
        if( (this.point+pointToCharge) > maxPoint ) {
            throw new UserPointException(POINT_STATUS.CHARGE_POINT_OVERFLOW);
        }
        return this.point+pointToCharge;
    }

    /**
     * 사용할 포인트를 입력 받고 검증하려는 함수
     * - 사용하려는 포인트가 최소 사용 포인트보다 작으면 예외
     * - 사용하려는 포인트가 최대 사용 포인트보다 많으면 예외
     * - 사용 후 잔여 포인트가 0 보다 작은 음수일 경우 예외
     * 사용 후 잔액을 반환
     * @param pointToUse
     * @return
     */
    public long use(long pointToUse) {
        if( pointToUse < minUsePoint || pointToUse > maxUsePoint){
            throw new UserPointException(POINT_STATUS.INVALID_USE_AMOUNT);
        }
        if( this.point-pointToUse < 0 ){
            throw new UserPointException(POINT_STATUS.USED_POINT_UNDERFLOW);
        }
        return this.point-pointToUse;
    }
}
