package com.example.English.teaching.center.dto.course;

import java.math.BigDecimal;

import com.example.English.teaching.center.utils.HtmlSanitizerUtils;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseSaveRequest {
    private Long id;

    @NotBlank(message = "Tên khóa học không được để trống")
    private String name;

    private String description;

    @Min(value = 0, message = "Học phí không được nhỏ hơn 0")
    private BigDecimal fee;

    @NotBlank(message = "Vui lòng chọn danh mục")
    private String category;

    public void setName(String name){
        this.name = HtmlSanitizerUtils.sanitizePlainText(name);
    }

    public void setCategory(String category){
        this.category = HtmlSanitizerUtils.sanitizePlainText(category);
    }

    public void setDescription(String description){
        this.description = HtmlSanitizerUtils.sanitizeRichText(description);
    }
}
