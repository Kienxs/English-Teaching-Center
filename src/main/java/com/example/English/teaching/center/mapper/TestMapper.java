package com.example.English.teaching.center.mapper;

import org.springframework.stereotype.Component;

import com.example.English.teaching.center.dto.QuestionDTO;
import com.example.English.teaching.center.dto.TestDTO;
import com.example.English.teaching.center.entity.Question;
import com.example.English.teaching.center.entity.Test;

@Component
public class TestMapper {
    public QuestionDTO toQuestonDTO(Question entity){
        if(entity == null) return null;

        QuestionDTO dto = new QuestionDTO();
        dto.setId(entity.getId());
        dto.setQuestionText(entity.getQuestionText());
        dto.setOptionA(entity.getOptionA());
        dto.setOptionB(entity.getOptionB());
        dto.setOptionC(entity.getOptionC());
        dto.setOptionD(entity.getOptionD());
        dto.setPoints(entity.getPoints());

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
                    .map(this::toQuestonDTO)
                    .toList());
        }

        return dto;
    }
}
