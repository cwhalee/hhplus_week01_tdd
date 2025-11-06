package io.hhplus.tdd.point;

public class UserPointException extends RuntimeException {
    public final POINT_STATUS status;

    public UserPointException(POINT_STATUS status, String message){
        super(message);
        this.status = status;
    }

    public UserPointException(POINT_STATUS status){
        this.status = status;
    }
}
