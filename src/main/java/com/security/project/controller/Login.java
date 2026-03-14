package com.security.project.controller;

import com.security.project.constants.ApplicationConstants;
import com.security.project.constants.LoginDto;
import com.security.project.entity.EmailVerificationCode;
import com.security.project.entity.Enums;
import com.security.project.entity.LoginAttempt;
import com.security.project.entity.User;
import com.security.project.repository.EmailVerificationCodeRepository;
import com.security.project.repository.LoginAttemptRepository;
import com.security.project.repository.UserRepository;
import com.security.project.services.EmailSender;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class Login {

    private final UserRepository userRepository;
    private final EmailVerificationCodeRepository emailRepo;
    private final LoginAttemptRepository loginAttemptRepository;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public Login(UserRepository userRepository, EmailVerificationCodeRepository emailRepo, LoginAttemptRepository loginAttemptRepository, EmailSender emailSender, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailRepo = emailRepo;
        this.loginAttemptRepository = loginAttemptRepository;
        this.emailSender = emailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto dto, HttpServletRequest request) {
        User user = userRepository.findByEmail(dto.getUserName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() == Enums.PENDING) {
            return ResponseEntity.status(403).body("Email doğrulaması henüz yapılmamış.");
        }
        if (user.getStatus() == Enums.BLOCKED) {
            return ResponseEntity.status(403).body("Hesabınız engellenmiş.");
        }

        if (!passwordEncoder.matches(dto.getPwd(), user.getPwd())) {
            logAttempt(user.getId(), request, false, Enums.WRONG_PASSWORD);
            return ResponseEntity.status(401).body("Email veya şifre hatalı");
        }

        String otp = String.format("%06d", new Random().nextInt(999999));

        EmailVerificationCode code = new EmailVerificationCode();
        code.setCode(otp);
        code.setUser(user);
        code.setType(Enums.LOGIN);
        code.setUsed(false);
        code.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        emailRepo.save(code);

        emailSender.sendVerifCode(user, otp);

        return ResponseEntity.ok().body("Doğrulama kodu email adresinize gönderildi. Lütfen /verify-login sayfasına ilerleyin.");
    }

    @PostMapping("/verify-login")
    public ResponseEntity<?> verifyLogin(@RequestParam String email, @RequestParam String otpCode, HttpServletRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        EmailVerificationCode activeCode = emailRepo.findTopByUserAndTypeOrderByIdDesc(user, Enums.LOGIN)
                .orElseThrow(() -> new RuntimeException("Aktif kod bulunamadı"));

        if (activeCode.isUsed() || activeCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(400).body("Kod geçersiz veya süresi dolmuş.");
        }

        if (!activeCode.getCode().equals(otpCode)) {
            logAttempt(user.getId(), request, false, Enums.OTP_REQUIRED);
            return ResponseEntity.status(401).body("Hatalı kod.");
        }

        activeCode.setUsed(true);
        emailRepo.save(activeCode);
        //JWT token controller'da üretiliyor ancak ayrı olarak üretilmek istenirse JWTGenerator adlı class'ta da yazılı o sınıfın filter chain'e eklenmesi yeterli olur
        String secret = ApplicationConstants.JWT_SECRET_DEFAULT_VALUE;
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String jwt = Jwts.builder()
                .issuer("SecurityProject")
                .subject("JWT Token")
                .claim("username", user.getEmail())
                .claim("authorities", user.getRole())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 30_000_000L))
                .signWith(secretKey)
                .compact();

        logAttempt(user.getId(), request, true, Enums.SUCCESS);

        return ResponseEntity.ok().header(ApplicationConstants.JWT_HEADER, "Bearer " + jwt).body("Login başarılı");
    }

    private void logAttempt(Integer userId, HttpServletRequest request, boolean success, Enums reason) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUserID(userId);
        attempt.setSuccess(success);
        attempt.setReason(reason);
        if (request != null) {
            attempt.setIpAdress(Register.getClientIp(request));
            attempt.setDevice(request.getHeader("User-Agent"));
        }
        attempt.setTimeStamp(LocalDateTime.now());
        loginAttemptRepository.save(attempt);
    }
}
