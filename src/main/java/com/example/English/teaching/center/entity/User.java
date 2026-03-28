package com.example.English.teaching.center.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data; 
import lombok.NoArgsConstructor; 
import lombok.ToString; 

@Entity
@Table(name = "users")
@Data 
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version // Fix click 2 lần
    private Long version = 0L;

    @NotNull
    @Column(name= "full_name", nullable = false, length =100)
    private String fullName;

    @NotNull
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotNull
    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 12)
    private String phone;

    @Column(name="avatar_url", length = 255)
    private String avatarUrl = "/images/home/avatar_clone.png";

    @Column(name="balance")
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "verification_code", length = 64)
    private String verificationCode;

    @Column(name = "reset_password_token", length = 40)
    private String resetPasswordToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 7)
    private Role role = Role.STUDENT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Status status = Status.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp 
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private List<Transaction> transactions; 

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private List<StudentCourse> enrolledCourses; 

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private List<TestResult> testResults; 

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private List<Comment> courseComments; 

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private List<Comment> blogComments;
    
    @OneToMany(mappedBy = "handledBy", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonIgnore
    private List<ConsultationRequest> consultationRequestsHandled;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private List<RefreshToken> refreshTokens;

    public enum Role {
        ADMIN, TECHNICAL, TEACHER, STUDENT;

        public Collection<? extends GrantedAuthority> getAuthorities() {
            return java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + this.name()));
        }
    }

    public enum Status {
        ACTIVE, PENDING, REJECTED
    }
    
 
    public User(String name, String email, String password) {
        this(name, email, password, Role.STUDENT, Status.PENDING);
    }

    public User(String fullName, String email, String password, Role role, Status status) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.balance = BigDecimal.ZERO; 
    }
}