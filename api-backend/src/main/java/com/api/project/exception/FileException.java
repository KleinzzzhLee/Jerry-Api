package com.api.project.exception;

import com.api.project.common.ErrorCode;

import java.io.IOException;

public class FileException extends IOException {
    private final int code;

    public FileException(int code, String message) {
        super(message);
        this.code = code;
    }

    public FileException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public FileException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
