package com.meteorcat.mix.game.openblive.api;

import com.meteorcat.mix.game.openblive.core.utils.CurlRedirect;
import com.meteorcat.mix.game.openblive.core.utils.DigestUtil;
import org.springframework.http.HttpMethod;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * 基础的集成对象类
 * @author MeteorCat
 */
public class BaseApi {


    /**
     * 获取参数签名Header
     * @param key 哔哩哔哩获取的Key
     * @param secret 哔哩哔哩获取的Secret
     * @param body 请求主体
     * @return Map<String,Object>
     */
    public Map<String,Object> sign(String key,String secret,String body){
        // 计算消息主体的 MD5
        String bodyMd5 = DigestUtil.md5(body);
        return signByMd5(key,secret,bodyMd5);
    }


    /**
     * 获取参数签名Header
     * @param key 哔哩哔哩获取的Key
     * @param secret 哔哩哔哩获取的Secret
     * @param bodyMd5 哔哩哔哩获取的数据Md5
     * @return Map<String,Object>
     */
    public Map<String,Object> signByMd5(String key,String secret,String bodyMd5){
        // 生成随机值和时间值
        Random random = new Random();
        int timestamp = Math.toIntExact(System.currentTimeMillis() / 1000L);
        String nonce = Integer.toString(random.nextInt(100000)+timestamp);

        // 参数排序填充
        Map<String,Object> params = new TreeMap<>(){{
            put("x-bili-accesskeyid",key);
            put("x-bili-content-md5",bodyMd5);
            put("x-bili-signature-method","HMAC-SHA256");
            put("x-bili-signature-nonce",nonce);
            put("x-bili-signature-version","1.0");
            put("x-bili-timestamp",timestamp);
        }};

        // 构建待签名主体
        StringBuilder buffer = new StringBuilder();
        int pos = 0;
        int size = params.size();
        for (Map.Entry<String,Object> value: params.entrySet()) {
            pos++;
            buffer.append(String.format(pos < size ? "%s:%s\n" : "%s:%s",value.getKey(),value.getValue().toString()));
        }

        // 采用 SHA-256 生成签名
        String sign = DigestUtil.hmac256(secret,buffer.toString());
        params.put("Authorization",sign);
        return params;
    }


    /**
     * 转发请求
     * @param key 哔哩哔哩获取的Key
     * @param secret 哔哩哔哩获取的Secret
     * @param address 请求地址
     * @param body 请求数据
     * @return CurlRedirect
     */
    public CurlRedirect redirect(String key,String secret,String address,String body){
        Map<String,Object> header = sign(key,secret,body);
        return new CurlRedirect.Builder(address)
                .pushHeaders(header)
                .json(HttpMethod.POST,body);
    }




}
