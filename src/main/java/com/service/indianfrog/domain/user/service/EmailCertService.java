package com.service.indianfrog.domain.user.service;

import com.service.indianfrog.domain.user.repository.EmailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@Slf4j
public class EmailCertService {

    private final EmailRepository emailRepository;
    private final MailSendService mailSendService;

    public EmailCertService(EmailRepository emailRepository, MailSendService mailSendService) {
        this.emailRepository = emailRepository;
        this.mailSendService = mailSendService;
    }

    @Transactional
    public void emailSend(String email) {
        String certificationNumber = createCertificationNumber();
        if (emailRepository.hashKey(email)) {
            emailRepository.removeCertificationNumber(email);
        }
        emailRepository.saveCertificationNumber(email, certificationNumber);

        String subject = "인디안 개구리 : 이메일 인증번호 확인";

        mailSendService.sendEmail(email, subject, certificationNumber);
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
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(888888) + 111111;
        return String.valueOf(num);
    }
}
