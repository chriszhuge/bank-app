package org.bank.common.response;

import lombok.Data;
import org.bank.common.enums.CodeEnum;

@Data
public class ResponseData<T> {
    private Integer code;

    private String msg;

    private T data;

    public ResponseData(T data) {
        this.code = CodeEnum.SUCCESS.getCode();
        this.msg = CodeEnum.SUCCESS.getMessage();
        this.data = data;
    }

    public ResponseData() {
        this.code = CodeEnum.SUCCESS.getCode();
        this.msg = CodeEnum.SUCCESS.getMessage();
    }

    public static ResponseData data(Object data){
        return new ResponseData(data);
    }
}
