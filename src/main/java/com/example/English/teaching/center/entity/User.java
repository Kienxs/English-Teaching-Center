package com.example.English.teaching.center.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction; 
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data; 
import lombok.NoArgsConstructor; 
import lombok.ToString; 

@Entity
@Table(name = "users")
@Data 
@NoArgsConstructor
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id=? and version=?")
@SQLRestriction("is_deleted = false") 
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) 
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private UUID id;

    @Version // Fix click 2 lần
    private Long version = 0L;

    @NotNull
    @Column(name= "full_name", nullable = false, length = 100)
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

    @Column(name = "avatar_public_id")
    private String avatarPublicId;

    @Column(name="balance", precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "verification_code", length = 64)
    private String verificationCode;

    @Column(name = "reset_password_token", length = 40)
    private String resetPasswordToken;

    @Column(name = "reset_password_expiry")
    private LocalDateTime resetPasswordExpiry;

    @Column(name = "token_version", nullable = false)
    private Integer tokenVersion = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.STUDENT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status = Status.ACTIVE;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp 
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @ToString.Exclude
    @JsonIgnore
    private List<Transaction> transactions; 

    @OneToMany(mappedBy = "student", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @ToString.Exclude
    @JsonIgnore
    private List<StudentCourse> enrolledCourses; 

    @OneToMany(mappedBy = "student", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @ToString.Exclude
    @JsonIgnore
    private List<TestResult> testResults; 

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL) 
    @ToString.Exclude
    @JsonIgnore
    private List<Comment> courseComments; 

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonIgnore
    private List<Comment> blogComments;

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
        this.isDeleted = false;
    }
}