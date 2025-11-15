package com.example.English.teaching.center.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    @Column(name = "id")
    private Long id;

    // 1. Đánh dấu mối quan hệ 1-1 với User
    @OneToOne(fetch = FetchType.LAZY) 
    
    // 2. Báo cho JPA rằng "hãy dùng 'id' của User làm 'id' cho Teacher"
    @MapsId 
    
    // 3. Chỉ định cột vật lý trong DB dùng để nối 2 bảng
    @JoinColumn(name = "id")
    private User user; // Giả định bạn đã có @Entity User

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "expertise")
    private String expertise;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Tự động gán ngày tạo khi lưu mới
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Constructors ---
    public Teacher() {
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}