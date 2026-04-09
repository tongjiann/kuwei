package com.xiw.kuwei.exception;


import com.diboot.core.vo.Status;

/**
 * "逻辑"异常
 * 常见的异常，比如某个变量为null等
 */
public class LogicalException extends BaseException {

    public LogicalException(String message) {
        super(message, Status.FAIL_EXCEPTION.code(), null);
    }

    public LogicalException(Throwable cause) {
        this(cause.getLocalizedMessage());
    }

    public LogicalException() {
        super(Status.FAIL_EXCEPTION.label(), Status.FAIL_EXCEPTION.code(), null);
    }

}
