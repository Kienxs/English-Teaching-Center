package com.example.English.teaching.center.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.English.teaching.center.entity.Material;

public interface MaterialRepository extends JpaRepository<Material, UUID> {
    
}