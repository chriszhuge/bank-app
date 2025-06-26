package org.bank.common.enums;

public enum CodeEnum {
    SUCCESS(0,"成功"),
    TRANSACTION_NOT_EXIST(10001, "此交易不存在"),
    ILLEGAL_PARA(10002, "参数校验不通过"),
    SERVICE_DEGRADED(99999, "系统繁忙"),
    SYSTEM_EXCEPTION(500, "内部错误");

    private final Integer code;
    private final String message;

    CodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode(){return code;}

    public String getMessage(){return message;}
}
