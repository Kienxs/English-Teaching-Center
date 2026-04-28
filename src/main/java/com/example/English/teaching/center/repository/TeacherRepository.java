package com.example.English.teaching.center.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.English.teaching.center.entity.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, UUID> {
    
}