package com.example.English.teaching.center.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.English.teaching.center.model.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    
}
