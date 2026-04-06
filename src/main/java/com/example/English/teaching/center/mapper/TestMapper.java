package com.example.English.teaching.center.mapper;

import org.springframework.stereotype.Component;

import com.example.English.teaching.center.dto.course.QuestionDTO;
import com.example.English.teaching.center.dto.course.QuestionSaveDTO;
import com.example.English.teaching.center.dto.course.TestDTO;
import com.example.English.teaching.center.dto.course.TestSaveDTO;
import com.example.English.teaching.center.entity.Question;
import com.example.English.teaching.center.entity.Test;

import java.util.stream.Collectors;

@Component
public class TestMapper {
    
    public QuestionDTO toQuestionDTO(Question entity){
        if(entity == null) return null;

        QuestionDTO dto = new QuestionDTO();
        dto.setId(entity.getId());
        dto.setQuestionText(entity.getQuestionText());
        dto.setPoints(entity.getPoints());

        if (entity.getOptions() != null) {
            dto.setOptions(entity.getOptions().stream()
                    .map(Question.Option::getText)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public QuestionSaveDTO toQuestionSaveDTO(Question entity){
        if(entity == null) return null;

        QuestionSaveDTO dto = new QuestionSaveDTO();
        dto.setId(entity.getId());
        dto.setTestId(entity.getTest() != null ? entity.getTest().getId() : null);
        dto.setQuestionText(entity.getQuestionText());
        dto.setPoints(entity.getPoints());

        dto.setOptions(entity.getOptions());

        return dto;
    }

    public TestDTO toTestDTO(Test entity){
        if(entity == null) return null;

        TestDTO dto = new TestDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setSlug(entity.getSlug());
        dto.setDurationMinutes(entity.getDurationMinutes());

        if(entity.getQuestions() != null){
            dto.setQuestions(entity.getQuestions().stream()
                    .map(this::toQuestionDTO) 
                    .toList());
        }

        return dto;
    }

    public TestSaveDTO toTestSaveDTO(Test entity){
        if(entity == null) return null;

        TestSaveDTO dto = new TestSaveDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDurationMinutes(entity.getDurationMinutes());
        if(entity.getLesson() != null){
            dto.setLessonId(entity.getLesson().getId());
        }

        return dto;
    }
}