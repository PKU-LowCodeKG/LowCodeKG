package org.example.lowcodekg.model.result;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {
    SUCCESS(200, "操作成功"),
    FAIL(201, "操作失败"),
    LOGIN_ERROR(201, "用户名或者密码错误"),
    VALIDATE_CODE_ERROR(202, "验证码错误"),
    LOGIN_AUTH(208, "用户未登录"),
    USER_NAME_IS_EXISTS(209, "用户名已经存在"),
    SYSTEM_ERROR(9999, "您的网络有问题请稍后重试"),
    NODE_ERROR(217, "该节点下有子节点，不可以删除"),
    DATA_ERROR(204, "数据异常"),
    ACCOUNT_STOP(216, "账号已停用"),
    STOCK_LESS(219, "库存不足"),
    USER_NOT_EXIST(220, "邮箱对应的用户不存在");

    private Integer code;      // 业务状态码
    private String message;    // 响应消息

    private ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
