package com.api.project.service;

import com.api.project.controller.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserController userController;

    @Test
    public void testUpdatePasswordUser() {

    }
}
