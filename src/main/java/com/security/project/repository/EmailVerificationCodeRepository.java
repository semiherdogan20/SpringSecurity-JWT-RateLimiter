package com.security.project.repository;

import com.security.project.entity.EmailVerificationCode;
import com.security.project.entity.Enums;
import com.security.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode,Integer> {

    Optional<EmailVerificationCode> findTopByUserAndTypeOrderByIdDesc(User user, Enums type);

    List<EmailVerificationCode> findByUserAndType(User user, Enums type);
}