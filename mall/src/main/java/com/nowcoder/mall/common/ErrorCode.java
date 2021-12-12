package com.nowcoder.mall.common;

public interface ErrorCode {

    // 通用异常

    int UNDEFINED_ERROR = 0;
    int PARAMETER_ERROR = 1;

    // 用户异常
    int USER_NOT_LOGIN = 101;
    int USER_LOGIN_FAULURE = 102;

    // 业务异常
    int STOCK_NOT_ENOUGH = 201;
    int CREATE_ORDER_FAILURE = 202;
    int OUT_OF_LIMIT = 203;
}
