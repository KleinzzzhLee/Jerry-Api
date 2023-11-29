package com.api.project.service;

import com.api.project.common.BaseResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class UserInterfaceInfoTest {
    @Resource
    UserInterfaceInfoService userInterfaceInfoService;
    @Test
    public void testInvokeOfUserInterfaceInfo() {
//        BaseResponse<Boolean> invoke = userInterfaceInfoService.invoke(1L, 1722182010384011265L);
//        System.out.println(invoke);
    }
}
