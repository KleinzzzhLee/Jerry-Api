package com.api.project.service.commonservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Random;

@Component
public class MailService {
    @Resource
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String USER;

    /**
     * 发送邮件
     * @param to 发送到用户
     * @return code 返回的验证码信息，用于存入到redis中
     */
    public String sendMail(String to) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(USER);
        message.setTo(to);
        message.setSubject("请查收，Mine API的验证码");
        String code = buildCode();
        message.setText("您的注册验证码是 = " + code  + "五分钟内有效");

        javaMailSender.send(message);
        return code;
    }

    private String buildCode() {
        Random random = new Random();

        String code = String.valueOf((int) (random.nextDouble() * 1000000));
        StringBuilder stringBuilder = new StringBuilder(code);
        while (stringBuilder.length() < 6) {
            stringBuilder.append(0);
        }
        return stringBuilder.toString();
    }


}
