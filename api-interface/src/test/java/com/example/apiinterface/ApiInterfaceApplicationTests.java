package com.example.apiinterface;

import com.example.apiclientsdk.clients.ApiClient;
import com.example.apiclientsdk.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class ApiInterfaceApplicationTests {

    @Resource
    private ApiClient apiClient;

    @Test
    void contextLoads() {
        String appKey = apiClient.getAppKey();
        String appSecret = apiClient.getAppSecret();
        User user = new User();
        user.setName("zzzhlee");
        // 测试
        String string1 = apiClient.getNameByGet("zzzhlee");
        System.out.println(string1);

        String string2 = apiClient.getNameByPost("zxcvasdf");
        System.out.println(string2);

        String string3 = apiClient.getUsernameByPost(user);
        System.out.println(string3);

    }

}
