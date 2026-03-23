package com.example.English.teaching.center.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CourseSaveDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal fee;
    private String category;
}
