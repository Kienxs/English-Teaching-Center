package com.example.English.teaching.center.dto;

import lombok.Data;

@Data
public class SectionDTO {
    private Long id;
    private String sectionTitle;
    private String sectionContent;
    private String imageUrl;
    private Integer sectionOrder; 
}