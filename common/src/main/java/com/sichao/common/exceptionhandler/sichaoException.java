package com.sichao.common.exceptionhandler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//自定义异常类
@Data
@AllArgsConstructor//生成有参构造器
@NoArgsConstructor//生成无参构造器
public class sichaoException extends RuntimeException {
    private Integer code;//状态码
    private String msg;//异常信息

}
