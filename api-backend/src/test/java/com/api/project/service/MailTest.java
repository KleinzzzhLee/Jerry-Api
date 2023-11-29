package com.api.project.service;

import com.api.project.service.commonservice.MailService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class MailTest {
    @Resource
    MailService mailService;

    @Test
    public void testMailSend() {
        System.out.println(mailService);
        mailService.sendMail("1924774423@qq.com");
    }
}
