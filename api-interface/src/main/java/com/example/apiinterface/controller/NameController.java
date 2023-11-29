package com.example.apiinterface.controller;


import com.example.apiclientsdk.model.User;
import com.example.apiclientsdk.utils.SignUtil;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/name")
public class NameController {

    // 这个appSecret 是从数据库查处来的
    private String appSecret = new String("123456");
    @GetMapping("/")
    public String getNameByGet(String name, HttpServletRequest request) {
        return "Get请求" + name;
    }

    @PostMapping("/")
    public String getNameByPost(@RequestParam String name) {
        return "Post请求" + name;
    }


    @PostMapping("/user")
    public String getUserNameByPost(@RequestBody User user, HttpServletRequest request) {
//        String body = request.getHeader("body");
//        String timestamp = request.getHeader("timestamp");
//        String random = request.getHeader("random");
//        String appKey = request.getHeader("appKey");
//
//        // 获取到签名
//        String sign_value = request.getHeader("sign_value");
////        String appSecret = request.getHeader("appSecret");
//
//        // todo 从数据库中查询对应的 appKey 是否分配给用户， 查到了选其 appSecret
//        if(!appKey.equals("admin")){
//            throw  new RuntimeException("参数有误");
//        }
//        // todo 对时间戳进行查看，保证不超过设置的时间
//
//        // todo 查询随机数是否存在  结合redis， 查询当前的随机数是否存在
////        if(Long.valueOf(random) > 1000) {
////            throw new RuntimeException("随机数太大了");
////        }
//
//        // 将这些参数封装为一个map， 进行加密， 与 传入的sign_value进行对比
//        Map<String, String> map = new HashMap<>();
//        map.put("appKey", appKey);
//        map.put("body", body);
//        map.put("timestamp", timestamp);
//        map.put("random", random);
//        // todo 实际情况是从数据库中查出 appSecret
//        String sign = SignUtil.getSign(map, appSecret);
//        if(!sign.equals(sign_value)) {
//            throw new RuntimeException();
//        }
        System.out.println("进入接口内部");
        return user.getName() + "POST请求";
    }


}
