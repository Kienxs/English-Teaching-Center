package com.example.English.teaching.center.dto.course;

import java.util.UUID;

import com.example.English.teaching.center.utils.HtmlSanitizerUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MaterialSaveRequest {
    private UUID id;

    @NotNull(message = "ID Bài học không được để trống")
    private UUID lessonId;

    @NotBlank(message = "Tiêu đề tài liệu không được để trống")
    private String title;

    @NotBlank(message = "Đường dẫn file không được để trống")
    private String fileUrl;

    @NotBlank(message = "Loại tài liệu không được để trống")
    private String type;

    public void setTitle(String title){
        this.title = HtmlSanitizerUtils.sanitizePlainText(title);
    }

    public void setType(String type){
        this.type = HtmlSanitizerUtils.sanitizePlainText(type);
    }
}