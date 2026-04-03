package com.example.English.teaching.center.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class PostEditDTO {
    private Long id;
    private String title;
    private String summary;
    private String thumbnailUrl;
    private String slug;

    private List<SectionDTO> sections = new ArrayList<>();
}
