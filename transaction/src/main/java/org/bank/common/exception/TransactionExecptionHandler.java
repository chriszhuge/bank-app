package org.bank.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.bank.common.enums.CodeEnum;
import org.bank.common.response.ResponseData;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice({"org.bank"})
public class TransactionExecptionHandler {
    @ResponseBody
    @ExceptionHandler({TransactionException.class, Throwable.class})
    public ResponseData<Object> process(Throwable cause, HttpServletRequest request){
        ResponseData<Object> responseData = new ResponseData<>();
        if(cause instanceof TransactionException){
            log.error("TransactionException, Url = {} , stack = {}", request.getRequestURL() ,cause);
            responseData.setCode(((TransactionException) cause).getCode());
            responseData.setMsg(cause.getMessage());
        }else {
            log.error("SystemException ,Url = {}  ,type = {}, stack  = {}", request.getRequestURL(), request.getMethod(), cause);
            responseData.setCode(CodeEnum.SYSTEM_EXCEPTION.getCode());
            responseData.setMsg(CodeEnum.SYSTEM_EXCEPTION.getMessage());
        }
        return responseData;
    }
}
