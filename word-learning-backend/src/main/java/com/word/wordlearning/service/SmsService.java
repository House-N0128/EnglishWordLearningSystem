package com.word.wordlearning.service;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    @Value("${aliyun.sms.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name}")
    private String signName;

    @Value("${aliyun.sms.template-code}")
    private String templateCode;

    private Client client;

    @PostConstruct
    public void init() {
        try {
            Config config = new Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret);
            config.endpoint = "dysmsapi.aliyuncs.com";
            client = new Client(config);
            log.info("阿里云短信服务初始化成功");
        } catch (Exception e) {
            log.error("阿里云短信服务初始化失败: {}", e.getMessage());
        }
    }

    public boolean sendSms(String phoneNumber, String code) {
        if (client == null) {
            log.warn("短信服务未初始化，跳过发送。验证码: {} -> {}", phoneNumber, code);
            return false;
        }
        try {
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phoneNumber)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam("{\"code\":\"" + code + "\"}");
            SendSmsResponse response = client.sendSms(request);
            log.info("短信发送结果: phone={}, code={}, bizId={}",
                    phoneNumber, response.getBody().getCode(), response.getBody().getBizId());
            return "OK".equals(response.getBody().getCode());
        } catch (Exception e) {
            log.error("短信发送失败: phone={}, error={}", phoneNumber, e.getMessage());
            return false;
        }
    }
}
