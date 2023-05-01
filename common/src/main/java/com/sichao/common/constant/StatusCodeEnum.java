package com.sichao.common.constant;

/**
 * @Description: 状态码枚举
 * @author: sjc
 * @createTime: 2023年04月29日 20:59
 *
 * 错误码和错误信息定义类
 * 1. 错误码定义规则为5为数字
 * 2. 前两位表示业务场景，最后三位表示错误码。例如：20000。20:通用 000：操作成功
 * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
 * 错误码列表：
 * 20: 通用
 *  001：参数格式校验
 *  002：短信验证码频率太高
 * 21: 数据库
 * 22: 用户
 * 23: 博客
 * 24: 话题
 * 25：举报
 */
public enum StatusCodeEnum {
    SUCCESS(20000, "操作成功"),

    // 系统级、通用级异常，编号从20001开始,
    SYS_EXCEPTION(20001, "系统异常"),
    SYS_UNKNOW_EXCEPTION(20002, "系统未知异常"),
    TO_MANY_REQUEST(20003, "请求流量过大，请稍后再试"),
    SMS_CODE_EXCEPTION(20004, "验证码获取频率太高，请稍后再试"),
    PARAMS_FORMAT_EXCEPTION(20005, "参数格式校验失败"),
    PARAMS_EMPTY_EXCEPTION(20006, "参数不能为空"),
    UNAUTHORIZED_REQUEST(20007,"请求未经授权"),
    FORBIDDEN_REQUEST(20008,"禁止访问"),

    //数据库异常，编号从21000开始
    DB_ERROR(21000, "数据库错误"),

    // 业务级异常
    //用户异常，编号从22000开始
    USER_EXCEPTION(22000,"用户异常"),
    REGISTER_PARAMS_EMPTY(22001, "注册数据为空，注册失败"),
    PHONE_FORMAT_EXCEPTION(22002,"手机号码格式不正确"),
    NICKNAME_FORMAT_EXCEPTION(22003,"昵称格式不正确，昵称最少2位、最多8位"),
    CODE_FORMAT_EXCEPTION(22004,"验证码格式不正确，验证码为6位数字"),
    PASSWORD_FORMAT_EXCEPTION(22005,"密码格式不正确，密码至少为8位、最多20位"),

    ;

    private final int code;
    private final String message;

    StatusCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }


}
