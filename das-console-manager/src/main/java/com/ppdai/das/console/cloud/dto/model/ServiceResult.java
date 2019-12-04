package com.ppdai.das.console.cloud.dto.model;

import lombok.*;

/**
 * Created by wanglinag
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResult<T> {

    public static final int ERROR = 500;
    public static final int SUCCESS = 0;
    public static final String SUCCESS_MSG = "success";
    public static final String ERROR_MSG = "error";
    private int code = SUCCESS;
    private T data;
    private String msg = SUCCESS_MSG;

    public static ServiceResult<String> fail() {
        ServiceResult<String> serviceResult = new ServiceResult();
        serviceResult.setCode(ServiceResult.ERROR);
        serviceResult.setMsg(ERROR_MSG);
        serviceResult.setData(null);
        return serviceResult;
    }

    public static <T> ServiceResult<T> fail(String msg) {
        ServiceResult serviceResult = new ServiceResult();
        serviceResult.setCode(ServiceResult.ERROR);
        serviceResult.setMsg(msg);
        serviceResult.setData(null);
        return serviceResult;
    }

    public static ServiceResult<String> success() {
        ServiceResult<String> serviceResult = new ServiceResult();
        serviceResult.setCode(ServiceResult.SUCCESS);
        serviceResult.setMsg(SUCCESS_MSG);
        serviceResult.setData(null);
        return serviceResult;
    }

    public static <T> ServiceResult<T> success(Object data) {
        ServiceResult serviceResult = new ServiceResult();
        serviceResult.setMsg(SUCCESS_MSG);
        serviceResult.setData(data);
        return serviceResult;
    }

    public static ServiceResult toServiceResult(com.ppdai.das.console.dto.model.ServiceResult sr) {
        ServiceResult serviceResult = new ServiceResult();
        if (sr.getCode() == com.ppdai.das.console.dto.model.ServiceResult.SUCCESS) {
            serviceResult.setMsg(SUCCESS_MSG);
            serviceResult.setData(sr.getMsg());
        } else {
            serviceResult.setCode(ERROR);
            serviceResult.setMsg(sr.getMsg().toString());
            serviceResult.setData(null);
        }
        return serviceResult;
    }
}
