package com.security.project.services;

import com.security.project.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSender {

    @Autowired
    private JavaMailSender mailSender;

    // Metot imzasını User ve String code alacak şekilde değiştirdik
    public void sendVerifCode(User user, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Doğrulama Kodu");
        message.setText("Doğrulama kodunuz: " + code);
        message.setFrom("xxxxx@gmail.com");
        mailSender.send(message);
    }

    public void sendVerifiedMail(User user, String subject, String text){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setFrom("xxxxx@gmail.com");
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
