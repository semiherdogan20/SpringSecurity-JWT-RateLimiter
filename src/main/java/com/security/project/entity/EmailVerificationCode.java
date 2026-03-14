package com.security.project.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "emailverificationcode")
public class EmailVerificationCode{

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id" , nullable = false)
    private User user;

    private String code;

    private Enums type; // REGISTER,LOGIN

    @Column(name = "expiresat")
    private LocalDateTime expiresAt;

    @Column(name = "isused")
    private boolean used;

    @Column(name = "creatTime")
    private LocalDateTime createdAt;

    public EmailVerificationCode() {
    }

    public EmailVerificationCode(User user, String code, Enums type) {
        this.user = user;
        this.code = code;
        this.type = type;
    }

    public EmailVerificationCode(int id, User user, String code, Enums type, LocalDateTime expiresAt, boolean used, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.code = code;
        this.type = type;
        this.expiresAt = expiresAt;
        this.used = used;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Enums getType() {
        return type;
    }

    public void setType(Enums type) {
        this.type = type;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


}
