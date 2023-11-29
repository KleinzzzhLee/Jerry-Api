package com.example.apiclientsdk.utils;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;

import java.util.Map;

/**
 * 生成sign签名的工具
 */
public class SignUtil {
    public static String getSign(Map<String, String> map, String appSecret) {
        Digester md5 = new Digester(DigestAlgorithm.MD5);

        String str = map.toString() + "." + appSecret;
        return  md5.digestHex(str);
    }
}
