package com.example.English.teaching.center.dto.content;

import java.util.ArrayList;
import java.util.List;

import com.example.English.teaching.center.entity.Post;
import com.example.English.teaching.center.utils.HtmlSanitizerUtils;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class EditPostRequest {
    private Long id;

    @NotBlank(message = "Tiêu đề bài viết không được để trống")
    private String title;
    private String summary;
    private String thumbnailUrl;
    private String slug;

    private Post.PostStatus status;

    private List<SectionResponse> sections = new ArrayList<>();

    public void setTitle(String title) {
        this.title = HtmlSanitizerUtils.sanitizePlainText(title);
    }

    public void setSummary(String summary) {
        this.summary = HtmlSanitizerUtils.sanitizePlainText(summary);
    }
}
