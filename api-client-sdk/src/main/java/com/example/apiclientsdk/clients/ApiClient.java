package com.example.apiclientsdk.clients;

import cn.hutool.core.util.RandomUtil;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;

import cn.hutool.json.JSONUtil;
import com.example.apiclientsdk.model.User;
import com.example.apiclientsdk.utils.SignUtil;
import lombok.Data;


import java.util.HashMap;
import java.util.Map;


/**
 * API接口客户端，
 *  用户通过此客户端调用接口， 向服务器发出请求
 */
@Data
public class ApiClient {
    private final String GATEWAY_HOST = "http://localhost:8099";

    private String appKey;
    private String appSecret;

    public ApiClient(String appKey, String appSecret) {
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    public String getNameByGet(String name) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);

        String result = HttpUtil.get(GATEWAY_HOST + "/api/name/", paramMap);
        return result;
    }

    public String getNameByPost( String name) {
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);

        String result = HttpUtil.post(GATEWAY_HOST + "/api/name/", paramMap);
        return result;
    }

    /**
     * 利用API签名认证
     * @return
     */
    private Map<String, String> getHeaderMap(String body) {
        Map<String, String> map = new HashMap<>();
        map.put("appKey", appKey);
        // 在API签名认证的过程中，请求参数 不包括appSecret
//        map.put("appSecret", appSecret);
        map.put("body", body);
        // 设置当前时间的时间戳
        map.put("timestamp", String.valueOf(System.currentTimeMillis()));
        // 设置随机数 保证只存在一次
        map.put("random", RandomUtil.randomNumbers(5));
        // 签名
        map.put("sign", SignUtil.getSign(map, appSecret));
        return map;
    }



    /**
     * 将本地保存的secretkey 和 appKey 添加到请求头中，在服务器进行验证
     * @param user
     * @return
     */
    public String getUsernameByPost( User user) {
        String json = JSONUtil.toJsonStr(user);
        HttpResponse response = HttpRequest.post(GATEWAY_HOST + "/api/name/user")
                .body(json)
                .addHeaders(getHeaderMap(json))
                .execute();
        String result = response.body();

        return result;
    }


}
