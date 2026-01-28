package com.example.English.teaching.center.model.dto;

public class UserProfileDTO {
    private String fullName;
    private String phone;
    // Thêm các trường khác từ form nếu bạn đã thêm chúng vào Entity
    // private java.time.LocalDate dob; 
    // private String city;
    // ...

    // Thêm Getters và Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

}