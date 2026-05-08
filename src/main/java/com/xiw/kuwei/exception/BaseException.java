package com.xiw.kuwei.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * 异常封装基类
 */
@Getter
@Setter
public abstract class BaseException extends RuntimeException {

    private final Integer errorCode;

    private final transient Object data;

    protected BaseException(String message, Integer errorCode, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.data = data;
    }

}
