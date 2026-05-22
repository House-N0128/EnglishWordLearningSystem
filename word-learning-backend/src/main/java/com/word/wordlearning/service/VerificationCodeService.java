package com.word.wordlearning.service;

import com.word.wordlearning.entity.OrdinaryUser;
import com.word.wordlearning.mapper.OrdinaryUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class VerificationCodeService {

    private static final Logger log = LoggerFactory.getLogger(VerificationCodeService.class);

    private final OrdinaryUserMapper userMapper;
    private final SmsService smsService;
    private final EmailService emailService;

    private final ConcurrentHashMap<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

    public VerificationCodeService(OrdinaryUserMapper userMapper,
                                   SmsService smsService,
                                   EmailService emailService) {
        this.userMapper = userMapper;
        this.smsService = smsService;
        this.emailService = emailService;
    }

    public String sendCode(String contact) {
        OrdinaryUser user = userMapper.findByPhoneOrEmail(contact);
        if (user == null) {
            return "该手机号或邮箱未注册";
        }

        String code = generateCode();
        codeStore.put(contact, new CodeEntry(code, System.currentTimeMillis() + 5 * 60 * 1000));

        boolean success;
        if (contact.contains("@")) {
            success = emailService.sendVerificationCode(contact, code);
        } else {
            success = smsService.sendSms(contact, code);
        }

        if (!success) {
            log.warn("验证码发送失败，但已生成。contact={}, code={}", contact, code);
        }

        log.info("验证码已生成: contact={}, code={}", contact, code);
        return null; // null means success
    }

    public String verifyCode(String contact, String inputCode) {
        CodeEntry entry = codeStore.get(contact);
        if (entry == null) {
            return "请先发送验证码";
        }
        if (System.currentTimeMillis() > entry.expireTime) {
            codeStore.remove(contact);
            return "验证码已过期，请重新发送";
        }
        if (!entry.code.equals(inputCode)) {
            return "验证码错误";
        }
        codeStore.remove(contact);
        return null;
    }

    public void clearCode(String contact) {
        codeStore.remove(contact);
    }

    private String generateCode() {
        int code = ThreadLocalRandom.current().nextInt(100000, 999999);
        return String.valueOf(code);
    }

    private static class CodeEntry {
        final String code;
        final long expireTime;

        CodeEntry(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
    }
}
