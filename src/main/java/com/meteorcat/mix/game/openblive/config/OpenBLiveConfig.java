package com.meteorcat.mix.game.openblive.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 哔哩哔哩开放平台所需的系统配置
 * @author MeteorCat
 */
@Configuration
public class OpenBLiveConfig {


    /**
     * 默认采用哔哩哔哩的正式环境域名
     */
    @Value("${mix.open.blive.api.url:https://live-open.biliapi.com}")
    private String apiUrl;


    /**
     * 哔哩哔哩创建项目分配的项目id, 注意使用 long 处理, 官方文档指出该值会溢出int
     */
    @Value("${mix.open.blive.api.id}")
    private Long appId;


    /**
     * 哔哩哔哩发放的 access_key_id
     */
    @Value("${mix.open.blive.api.key}")
    private String appKey;


    /**
     * 哔哩哔哩发放的 access_key_secret
     */
    @Value("${mix.open.blive.api.secret}")
    private String appSecret;


    /**
     * 服务器地址
     * @return String
     */
    public String getApiUrl() {
        return apiUrl;
    }

    /**
     * 获取哔哩哔哩应用ID
     * @return appid
     */
    public Long getAppId() {
        return appId;
    }


    /**
     * 获取哔哩哔哩应用Key
     * @return appKey
     */
    public String getAppKey() {
        return appKey;
    }


    /**
     * 获取哔哩哔哩应用Secret
     * @return appSecret
     */
    public String getAppSecret() {
        return appSecret;
    }
}
