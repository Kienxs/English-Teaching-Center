package com.example.English.teaching.center.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.English.teaching.center.entity.Post;

import lombok.Data;

@Data
public class PostEditDTO {
    private Long id;
    private String title;
    private String summary;
    private String thumbnailUrl;
    private String slug;

    private Post.PostStatus status;

    private List<SectionDTO> sections = new ArrayList<>();
}
