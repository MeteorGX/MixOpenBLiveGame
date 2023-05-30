package com.meteorcat.mix.game.openblive.core.utils;

import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.Date;

/**
 * JSON响应结构对象
 * @author MeteorCat
 */
public class JsonRespond implements Serializable {


    /**
     * 响应状态
     */
    private final Integer status;

    /**
     * 响应消息
     */
    private final String message;

    /**
     * 响应数据
     */
    private final Object response;

    /**
     * 响应事件
     */
    private final Date date = new Date();


    /**
     * 不允许直接实例
     * @param status 响应状态
     * @param message 响应消息
     * @param response 响应数据
     */
    private JsonRespond(Integer status, String message, Object response) {
        this.status = status;
        this.message = message;
        this.response = response;
    }


    /**
     * 构建响应体
     * @param status 响应状态
     * @param message 响应消息
     * @param data 响应数据
     * @return JsonRespond
     */
    public static JsonRespond of(Integer status,String message,Object data){
        return new JsonRespond(status,message,data);
    }

    /**
     * 构建响应体
     * @param status 响应状态
     * @param message 响应消息
     * @return JsonRespond
     */
    public static JsonRespond of(Integer status,String message){
        return of(status,message,null);
    }


    /**
     * 构建响应体
     * @param status Http状态
     * @param message 响应消息
     * @param data 响应数据
     * @return JsonRespond
     */
    public static JsonRespond of(HttpStatus status,String message,Object data){
        return of(status.value(),message,data);
    }


    /**
     * 构建响应体
     * @param status Http状态
     * @param message 响应消息
     * @return JsonRespond
     */
    public static JsonRespond of(HttpStatus status,String message){
        return of(status.value(),message,null);
    }

    /**
     * 构建响应体
     * @param status Http状态
     * @param data 响应数据
     * @return JsonRespond
     */
    public static JsonRespond of(HttpStatus status,Object data){
        return of(status.value(), status.getReasonPhrase(),data);
    }


    /**
     * 构建响应体
     * @param status Http状态
     * @return JsonRespond
     */
    public static JsonRespond of(HttpStatus status){
        return of(status.value(),status.getReasonPhrase(),null);
    }


    public Date getDate() {
        return date;
    }

    public Integer getStatus() {
        return status;
    }

    public Object getResponse() {
        return response;
    }

    public String getMessage() {
        return message;
    }
}
