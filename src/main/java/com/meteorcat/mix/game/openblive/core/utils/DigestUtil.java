package com.meteorcat.mix.game.openblive.core.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 加密|哈希处理
 * @author MeteorCat
 */
public class DigestUtil {

    /**
     * MD5处理
     * @param data 数据
     * @return String
     */
    public static String md5(String data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(data.getBytes());
            return new BigInteger(1, messageDigest.digest()).toString(16);
        } catch (NoSuchAlgorithmException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * SHA-256处理
     * @param data 数据
     * @return String
     */
    public static String hmac256(String key,String data){
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"));
            byte[] signData = mac.doFinal(data.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte item : signData) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100), 1, 3);
            }
            return sb.toString().toLowerCase();
        } catch (NoSuchAlgorithmException | InvalidKeyException exception) {
            exception.printStackTrace();
            return null;
        }
    }
}
