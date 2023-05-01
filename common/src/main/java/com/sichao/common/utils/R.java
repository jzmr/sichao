package com.sichao.common.utils;

import com.sichao.common.constant.Constant;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 统一返回给前端的结果类
 * @author: sjc
 * @createTime: 2023年04月26日 22:40
 */
@Data
public class R {
    private Boolean success;    //是否成功
    public Integer code;        //返回码
    private String message;     //返回消息
    private Map<String, Object> data=new HashMap<>();   //返回数据

    //把构造方法私有化，其他类不能new这个对象
    private R() {}

    //链式编程R.ok().code().message().....
    //成功静态方法
    public static R ok(){
        R r=new R();
        r.setSuccess(true);
        r.setCode(Constant.SUCCESS_CODE);
        r.setMessage(Constant.SUCCESS_MESSAGE);
        return r;
    }
    //失败静态方法
    public static R error(){
        R r=new R();
        r.setSuccess(false);
        r.setCode(Constant.FAILURE_CODE);
        r.setMessage(Constant.FAILURE_MESSAGE);
        return r;
    }

    public R success(Boolean success){//设置是否成功
        this.setSuccess(success);
        return this;
    }
    public R message(String message){//设置返回消息
        this.setMessage(message);
        return this;
    }
    public R code(Integer code){//设置code值(20000成功，20001失败)
        this.setCode(code);
        return this;
    }

    public R data(String key,Object value){//设置返回数据，传入字符串和对象，以map的形式返回
        this.data.put(key,value);
        return this;
    }
    public R data(Map<String, Object> map){//设置返回数据，传入map集合，以map的形式返回
        this.setData(map);
        return this;
    }
}
