package com.sichao.common.exceptionhandler;

import com.sichao.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//统一异常处理类
@ControllerAdvice
@Slf4j//可以在出现异常时，将异常信息输出到文件中
public class GlobalExceptionHandler {
    //1、全局异常处理
    @ExceptionHandler(Exception.class)//指定出现什么异常执行这个方法
    @ResponseBody//为了能够返回数据
    public R error(Exception e){
        e.printStackTrace();
        return R.error().message("执行了全局异常处理");
    }

    //2、特定异常处理
    @ExceptionHandler(ArithmeticException.class)//指定出现什么异常执行这个方法
    @ResponseBody//为了能够返回数据
    public R error(ArithmeticException e){
        e.printStackTrace();
        return R.error().message("执行了ArithmeticException异常");
    }

    //3、自定义此处理
    @ExceptionHandler(sichaoException.class)//指定出现什么异常执行这个方法
    @ResponseBody//为了能够返回数据
    public R error(sichaoException e){
        log.error(e.getMsg());//输出简略的异常信息
        e.printStackTrace();//TODO 开发时打开，生产时关闭
        return R.error().code(e.getCode()).message(e.getMsg());
    }

}
