package com.meteorcat.mix.game.openblive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteorcat.mix.game.openblive.api.BaseApi;
import com.meteorcat.mix.game.openblive.config.OpenBLiveConfig;
import com.meteorcat.mix.game.openblive.core.utils.CurlRedirect;
import com.meteorcat.mix.game.openblive.core.utils.DigestUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@SpringBootTest
class MixOpenBLiveGameApplicationTests {


    /**
     * 日志句柄
     */
    Logger logger = LoggerFactory.getLogger(MixOpenBLiveGameApplicationTests.class);


    /**
     * 开放平台配置
     */
    @Autowired
    OpenBLiveConfig config;


    /**
     * JSON解析器
     */
    ObjectMapper mapper = new ObjectMapper();


    /**
     * 官方样例
     */
    @Test
    void bili(){

        // 官方提供的样例
        final String secret = "JzOzZfSHeYYnAMZ";
        final String message = "x-bili-accesskeyid:xxxx\n" +
                "x-bili-content-md5:fa6837e35b2f591865b288dfd859ce9d\n" +
                "x-bili-signature-method:HMAC-SHA256\n" +
                "x-bili-signature-nonce:ad184c09-095f-91c3-0849-230dd3744045\n" +
                "x-bili-signature-version:1.0\n" +
                "x-bili-timestamp:1624594467";


        // 测试转化签名对比官方:
        // a81c50234b6bbf15bc56e387ee4f19c6f871af2f70b837dc56db16517d4a341f
        String sign = DigestUtil.hmac256(secret,message);
        logger.info("Sign = {}",sign);

    }

    /**
     * 官方签名方法
     */
    @Test
    void sign() {
        // 按照官方demo加密比较
        final String key = "xxxx";
        final String secret = "JzOzZfSHeYYnAMZ";
        final String bodyMd5 = "fa6837e35b2f591865b288dfd859ce9d";

        // 创建哈希参数
        BaseApi api = new BaseApi();
        Map<String,Object> data = api.signByMd5(key,secret,bodyMd5);
        logger.info("Param = {}",data);
    }


    /**
     * 测试创建请求
     */
    @Test
    void start() throws JsonProcessingException {
        // 构建参数
        final String url = config.getApiUrl();
        final String path = "/v2/app/start";
        final Long id = config.getAppId();
        final String key = config.getAppKey();
        final String secret = config.getAppSecret();
        final String code = "CMYW927A6Q680";

        // 构建消息主体
        Map<String,Object> body = new TreeMap<>(){{
            put("code",code);
            put("app_id",id);
        }};
        final String bodyStr = mapper.writeValueAsString(body);
        logger.info("Body = {}",bodyStr);


        // 生成签名
        String address = String.format("%s%s",url,path);
        BaseApi api = new BaseApi();
        CurlRedirect response = api.redirect(key,secret,address,bodyStr);
        logger.info("Response({}) = {}",response.getStatus(),response.getBody());
    }

}
