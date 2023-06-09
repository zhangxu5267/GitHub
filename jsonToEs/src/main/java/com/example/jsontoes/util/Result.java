package com.example.jsontoes.util;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description: Result 统一返回结果<br>
 * @date: 2022/12/27 0027 下午 1:09 <br>
 * @author: William <br>
 * @version: 1.0 <br>
 */
@Data
@ApiModel(value="统一返回结果对象", description="")
public class Result {

    /**
     *操作成功
     */
    public static final int SUCCESS = 200;

    /**
     * 响应业务状态
     */
    @ApiModelProperty(value = "响应业务状态")
    private Integer status;

    /**
     * 响应消息
     */
    @ApiModelProperty(value = "响应消息")
    private String msg;

    /**
     * 响应中的数据
     */
    @ApiModelProperty(value = "响应中的数据")
    private Object data;



    /**
     * description: Result  默认无参构造<br>
     * @version: 1.0
     * @date: 2022/12/27 0027 下午 1:21
     * @author: William
     */
    public Result() {

    }

    /**
     * description: Result  只包含返回数据构造方法<br>
     * @version: 1.0
     * @date: 2022/12/27 0027 下午 1:22
     * @author: William
     * @param data   需要返回数据
     */
    public Result(Object data) {
        this.status = SUCCESS;
        this.msg = "OK";
        this.data = data;
    }

    /**
     * description: Result 包含所有参数的构造方法<br>
     * @version: 1.0
     * @date: 2022/12/27 0027 下午 1:23
     * @author: William
     * @param status    状态
     * @param msg       消息
     * @param data      数据
     */
    public Result(Integer status, String msg, Object data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public static Result ok() {
        return new Result(null);
    }

    public static Result ok(Object data) {
        return new Result(data);
    }


    public static Result build(Integer status, String msg) {
        return new Result(status, msg, null);
    }

    public static Result build(Integer status, String msg, Object data) {
        return new Result(status, msg, data);
    }

}
