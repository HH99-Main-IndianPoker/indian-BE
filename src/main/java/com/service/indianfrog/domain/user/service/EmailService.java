package com.service.indianfrog.domain.user.service;

import com.service.indianfrog.domain.user.repository.EmailRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final EmailRepository emailRepository;

    public EmailService(JavaMailSender javaMailSender, EmailRepository emailRepository) {
        this.javaMailSender = javaMailSender;
        this.emailRepository = emailRepository;
    }

    @Transactional
    public String emailSend(String email) {
        String certificationNumber = createCertificationNumber();
        try {
            if (emailRepository.hashKey(email)) {
                emailRepository.removeCertificationNumber(email);
            }
            emailRepository.saveCertificationNumber(email, certificationNumber);

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false,
                "UTF-8");
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("인디안 개구리 인증번호입니다.");
            mimeMessageHelper.setText(certificationNumber);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("이메일 전송 실패");
            throw new RuntimeException();
        }

        return certificationNumber;
    }

    @Transactional
    public boolean emailAuthCheck(String email, String emailCode) {
        String certificationNumber = emailRepository.getCertificationNumber(email);

        if (certificationNumber.equals(emailCode)) {
            emailRepository.removeCertificationNumber(email);
            return true;
        }
        return false;
    }

    private String createCertificationNumber()  {
        Random random = new Random();
        int num = random.nextInt(888888) + 111111;
        return String.valueOf(num);
    }
}
