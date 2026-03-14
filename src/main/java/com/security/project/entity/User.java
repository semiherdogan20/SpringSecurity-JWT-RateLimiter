package com.security.project.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user")
public class User{
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "email")
    private String email;

    @Column(name = "password", unique = true)
    private String pwd;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<EmailVerificationCode> verificationCodes;

    @Column(name = "status")
    private Enums status;

    @Column(name = "role")
    private String role;

    @Column(name = "creatTime")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<LoginAttempt> loginAttempts;

    public User() {
    }

    public User(String email, String pwd, String role) {
        this.email = email;
        this.pwd = pwd;
        this.role = role;
    }

    public User(String email, String pwd, Enums status, String role, LocalDateTime createdAt) {
        this.email = email;
        this.pwd = pwd;
        this.status = status;
        this.role = role;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public Enums getStatus() {
        return status;
    }

    public void setStatus(Enums status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<EmailVerificationCode> getVerificationCodes() {
        return verificationCodes;
    }

    public void setVerificationCodes(List<EmailVerificationCode> verificationCodes) {
        this.verificationCodes = verificationCodes;
    }

    public List<LoginAttempt> getLoginAttempts() {
        return loginAttempts;
    }

    public void setLoginAttempts(List<LoginAttempt> loginAttempts) {
        this.loginAttempts = loginAttempts;
    }
}
