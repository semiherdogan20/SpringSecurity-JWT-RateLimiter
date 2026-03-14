package com.security.project.controller;

import com.security.project.entity.EmailVerificationCode;
import com.security.project.entity.Enums;
import com.security.project.entity.LoginAttempt;
import com.security.project.entity.User;
import com.security.project.repository.EmailVerificationCodeRepository;
import com.security.project.repository.LoginAttemptRepository;
import com.security.project.repository.UserRepository;
import com.security.project.services.EmailSender;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class Register {

    private final UserRepository userRepository;
    private final EmailVerificationCodeRepository emailRepo;
    private final LoginAttemptRepository loginAttemptRepository;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public Register(UserRepository userRepository, EmailVerificationCodeRepository emailRepo, LoginAttemptRepository loginAttemptRepository, EmailSender emailSender, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailRepo = emailRepo;
        this.loginAttemptRepository = loginAttemptRepository;
        this.emailSender = emailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user){
        User savedUser = new User();
        savedUser.setPwd(passwordEncoder.encode(user.getPwd()));
        savedUser.setEmail(user.getEmail());
        savedUser.setStatus(Enums.PENDING);
        // Satır 47 Hatası: setAuthProvider ve LOCAL enum'u entity'de olmadığı için kaldırıldı.
        savedUser.setRole("ROLE_USER");
        userRepository.save(savedUser);

        String otp = String.format("%06d", new Random().nextInt(999999));

        EmailVerificationCode emailVerifCode = new EmailVerificationCode();
        emailVerifCode.setCode(otp);
        emailVerifCode.setUser(savedUser);
        emailVerifCode.setType(Enums.REGISTER);
        emailVerifCode.setUsed(false);
        emailVerifCode.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        emailRepo.save(emailVerifCode);

        // Satır 61 Hatası: Metot parametreleri uyumlu hale getirildi
        emailSender.sendVerifCode(savedUser, otp);

        return ResponseEntity.ok().body("Bilgileriniz kaydedildi. Email doğrulama ekranına yönlendiriliyorsunuz.");
    }

    @PostMapping("/register-verify/{userID}")
    public ResponseEntity<?> verify(@PathVariable int userID, @RequestParam String verifyCode, HttpServletRequest request){
        Optional<User> optUser = userRepository.findById(userID);

        if(optUser.isPresent()){
            User user = optUser.get();
            // Satır 72 Hatası: Repository'ye metot eklendiği için hata kalktı
            List<EmailVerificationCode> codes = emailRepo.findByUserAndType(user, Enums.REGISTER);

            boolean isValid = false;
            for(EmailVerificationCode code : codes){
                if(code.getCode().equals(verifyCode) && !code.isUsed()){
                    code.setUsed(true);
                    emailRepo.save(code);

                    user.setStatus(Enums.ACTIVE);
                    userRepository.save(user);
                    isValid = true;

                    LoginAttempt loginAttempt = new LoginAttempt();
                    loginAttempt.setReason(Enums.SUCCESS);
                    loginAttempt.setSuccess(true);
                    // Entity getter/setter isimlerine uyarlandı
                    loginAttempt.setUserID(user.getId());
                    loginAttempt.setIpAdress(getClientIp(request));
                    loginAttempt.setTimeStamp(LocalDateTime.now());
                    loginAttemptRepository.save(loginAttempt);
                    break;
                }
            }

            if(isValid) {
                emailSender.sendVerifiedMail(user, "Hesabın Onaylandı", "Hesabın başarıyla onaylandı.");
                return ResponseEntity.ok().body("Kod doğrulandı ve hesabınız aktif edildi.");
            } else {
                return ResponseEntity.badRequest().body("Geçersiz veya kullanılmış kod.");
            }
        }
        return ResponseEntity.badRequest().body("Verilen ID ile user bulunamadı.");
    }

    public static String getClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isEmpty() && !"unknown".equalsIgnoreCase(header)) {
            return header.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}