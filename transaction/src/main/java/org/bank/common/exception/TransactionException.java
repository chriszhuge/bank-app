package org.bank.common.exception;

import org.bank.common.enums.CodeEnum;

public class TransactionException extends RuntimeException {
    private int code;
    private String message;
    private Exception exception;

    public TransactionException(CodeEnum codeEnum) {
        super(codeEnum.getMessage());
        this.code = codeEnum.getCode();
        this.message = codeEnum.getMessage();
    }

    public TransactionException(CodeEnum codeEnum, String message) {
        super(codeEnum.getMessage());
        this.code = codeEnum.getCode();
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}