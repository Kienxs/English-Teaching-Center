package com.example.English.teaching.center.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.English.teaching.center.dto.QuestionSaveDTO;
import com.example.English.teaching.center.dto.TestDTO;
import com.example.English.teaching.center.dto.TestSaveDTO;
import com.example.English.teaching.center.entity.Lesson;
import com.example.English.teaching.center.entity.Question;
import com.example.English.teaching.center.entity.Test;
import com.example.English.teaching.center.entity.TestResult;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.mapper.TestMapper;
import com.example.English.teaching.center.repository.LessonRepository;
import com.example.English.teaching.center.repository.QuestionRepository;
import com.example.English.teaching.center.repository.TestRepository;
import com.example.English.teaching.center.repository.TestResultRepository;
import com.example.English.teaching.center.repository.UserRepository;
import com.example.English.teaching.center.utils.SlugUtils;

import jakarta.transaction.Transactional;

@Service
public class TestService {
    private final TestRepository testRepository;
    private final TestResultRepository testResultRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final TestMapper testMapper;

    public TestService(TestRepository testRepository,
                       TestResultRepository testResultRepository,
                       UserRepository userRepository,
                       LessonRepository lessonRepository,
                       QuestionRepository questionRepository,
                       TestMapper testMapper) {
        this.testRepository = testRepository;
        this.testResultRepository = testResultRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.questionRepository = questionRepository;
        this.testMapper = testMapper;
    }

    private Test findTestByIdOrSlug(String identifier) {
        return testRepository.findBySlug(identifier)
            .orElseGet(() -> {
                try {
                    return testRepository.findById(Long.parseLong(identifier)).orElse(null);
                } catch (NumberFormatException e) {
                    return null;
                }
            });
    }

    public TestDTO getSafeTestDetails(String identifier){
        Test test = Optional.ofNullable((findTestByIdOrSlug(identifier)))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi: " + identifier));
        return testMapper.toTestDTO(test);
    }

    @Transactional
    public TestResult startOrResumeTest(String identifier, User user){
        Test test = Optional.ofNullable(findTestByIdOrSlug(identifier))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi: " + identifier));

        List<TestResult> activeTests = testResultRepository.findByStudentIdAndStatus(user.getId(), TestResult.Status.DOING);

        if(!activeTests.isEmpty()){
            TestResult active = activeTests.get(0);
            if(!active.getTest().getId().equals(test.getId())){
                throw new IllegalStateException("Bạn đang có bài thi chưa hoàn thành: " + active.getTest().getTitle());
            }
            return active;
        }

        TestResult newResult = new TestResult();
        newResult.setTest(test);
        newResult.setStudent(user);
        newResult.setScore(BigDecimal.ZERO);
        newResult.setStatus(TestResult.Status.DOING);
        newResult.setStartTime(LocalDateTime.now());
        return testResultRepository.save(newResult);
    }

    @Transactional
    public TestResult submitTest(String identifier, Map<String, String> answers, String email){
        User user = userRepository.findByEmail(email).orElseThrow();
        
        Test test = Optional.ofNullable(findTestByIdOrSlug(identifier))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi: " + identifier));

        List<TestResult> doingTests = testResultRepository.findByStudentIdAndStatus(user.getId(), TestResult.Status.DOING);

        if(doingTests.isEmpty() || !doingTests.get(0).getTest().getId().equals(test.getId())){
            throw new IllegalStateException("Không tìm thấy bài thi đang làm hoặc đã nộp rồi.");
        }

        TestResult result = doingTests.get(0);

        double totalScore = 0;
        double totalPoints = 0;

        for(Question q : test.getQuestions()){
            double point = (q.getPoints() != null) ? q.getPoints().doubleValue() : 0;
            totalPoints += point;

            String selected = answers.get("answers[" + q.getId() + "]");

            if (selected != null && selected.equals(q.getCorrectAnswer())) {
                totalScore += point;
            }
        }

        double finalScore = (totalPoints > 0) ?  (totalScore / totalPoints) * 10.0 : 0;
        BigDecimal roundedScore = new BigDecimal(finalScore).setScale(2, RoundingMode.HALF_UP);

        result.setScore(roundedScore);
        result.setSubmitTime(LocalDateTime.now());
        result.setStatus(TestResult.Status.COMPLETED);

        if(result.getStartTime() != null){
            long seconds = java.time.Duration.between(result.getStartTime(), result.getSubmitTime()).getSeconds();
            result.setExecutionTimeSeconds((int) seconds);
        }

        return testResultRepository.save(result);
    }

    // For Teachers
    @Transactional
    public String saveTest(TestSaveDTO dto) {
        Lesson lesson = lessonRepository.findById(dto.getLessonId()).orElseThrow(() -> new RuntimeException("Không tìm thấy bài học"));
        Test test = (dto.getId() != null) ? testRepository.findById(dto.getId()).orElse(new Test()) : new Test();
        test.setId(dto.getId());
        test.setLesson(lesson);
        test.setTitle(dto.getTitle());

        String baseSlug = SlugUtils.makeSlug(test.getTitle());
        String finalSlug = baseSlug;
        int count = 1;
        Long currentId = (test.getId() != null ) ? test.getId() : -1L;

        while(testRepository.existsBySlugAndIdNot(finalSlug, currentId)) {
            finalSlug = baseSlug + "-" + count;
            count++;
        }

        test.setSlug(finalSlug);
        test.setDurationMinutes(dto.getDurationMinutes());
        testRepository.save(test);

        return lesson.getCourse().getSlug();
    }

    @Transactional
    public void deleteTest(Long id) {
        testRepository.deleteById(id);
    }

    @Transactional
    public void saveQuestion(QuestionSaveDTO dto) {
        Test test = testRepository.findById(dto.getTestId()).orElseThrow();
        Question q = (dto.getId() != null) ? questionRepository.findById(dto.getId()).orElse(new Question()) : new Question();
        q.setTest(test);
        q.setQuestionText(dto.getQuestionText());
        q.setOptionA(dto.getOptionA());
        q.setOptionB(dto.getOptionB());
        q.setOptionC(dto.getOptionC());
        q.setOptionD(dto.getOptionD());
        q.setCorrectAnswer(dto.getCorrectAnswer());
        q.setPoints(dto.getPoints());
        questionRepository.save(q);
    }

    @Transactional
    public Long deleteQuestionAndGetTestId(Long questionId) {
        Question q = questionRepository.findById(questionId).orElseThrow();
        Long testId = q.getTest().getId();
        questionRepository.delete(q);
        return testId;
    }

    public Test findTestById(Long id) {
        return testRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi với ID: " + id));
    }

    public Test findTestByIdentifier(String identifier) {
        return Optional.ofNullable(findTestByIdOrSlug(identifier))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi với: " + identifier));
    }
}
