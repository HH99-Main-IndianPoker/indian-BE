package com.service.indianfrog.domain.user.service;

import com.service.indianfrog.global.exception.ErrorCode;
import com.service.indianfrog.global.exception.RestApiException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailSendService {

    private final JavaMailSender javaMailSender;

    public MailSendService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, false,"UTF-8");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
//            ClassPathResource imageResource = new ClassPathResource("images/test.png");
//            message.addAttachment("test.png",imageResource);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("이메일 전송 실패");
            throw new RestApiException(ErrorCode.EMAIL_SEND_FAILURE.getMessage());
        }
    }

}
