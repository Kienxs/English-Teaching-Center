package com.example.English.teaching.center.mapper;

import org.springframework.stereotype.Component;

import com.example.English.teaching.center.dto.course.QuestionResponse;
import com.example.English.teaching.center.dto.course.QuestionSaveRequest;
import com.example.English.teaching.center.dto.course.TestResponse;
import com.example.English.teaching.center.dto.course.TestSaveRequest;
import com.example.English.teaching.center.entity.Question;
import com.example.English.teaching.center.entity.Test;

import java.util.stream.Collectors;

@Component
public class TestMapper {
    
    public QuestionResponse toQuestionDTO(Question entity){
        if(entity == null) return null;

        QuestionResponse dto = new QuestionResponse();
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

    public QuestionSaveRequest toQuestionSaveDTO(Question entity){
        if(entity == null) return null;

        QuestionSaveRequest dto = new QuestionSaveRequest();
        dto.setId(entity.getId());
        dto.setTestId(entity.getTest() != null ? entity.getTest().getId() : null);
        dto.setQuestionText(entity.getQuestionText());
        dto.setPoints(entity.getPoints());

        dto.setOptions(entity.getOptions());

        return dto;
    }

    public TestResponse toTestDTO(Test entity){
        if(entity == null) return null;

        TestResponse dto = new TestResponse();
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

    public TestSaveRequest toTestSaveDTO(Test entity){
        if(entity == null) return null;

        TestSaveRequest dto = new TestSaveRequest();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDurationMinutes(entity.getDurationMinutes());
        if(entity.getLesson() != null){
            dto.setLessonId(entity.getLesson().getId());
        }

        return dto;
    }
}