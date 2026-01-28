package com.example.English.teaching.center.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.English.teaching.center.model.Lesson;
import com.example.English.teaching.center.model.Question;
import com.example.English.teaching.center.model.Test;
import com.example.English.teaching.center.model.TestResult;
import com.example.English.teaching.center.model.User;
import com.example.English.teaching.center.repository.LessonRepository;
import com.example.English.teaching.center.repository.QuestionRepository;
import com.example.English.teaching.center.repository.TestRepository;
import com.example.English.teaching.center.repository.TestResultRepository;
import com.example.English.teaching.center.repository.UserRepository;
import com.example.English.teaching.center.utils.SlugUtils;

@Service
public class TestService {
    private final TestRepository testRepository;
    private final TestResultRepository testResultRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;

    public TestService(TestRepository testRepository,
                       TestResultRepository testResultRepository,
                       UserRepository userRepository,
                       LessonRepository lessonRepository,
                       QuestionRepository questionRepository) {
        this.testRepository = testRepository;
        this.testResultRepository = testResultRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.questionRepository = questionRepository;
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

        return testResultRepository.findByTestIdAndStudentIdAndStatus(test.getId(), user.getId(), TestResult.Status.DOING)
            .stream().findFirst().orElseGet(() -> {
                TestResult newResult = new TestResult();
                newResult.setTest(test);
                newResult.setStudent(user);
                newResult.setScore(BigDecimal.ZERO);
                newResult.setStatus(TestResult.Status.DOING);
                newResult.setStartTime(LocalDateTime.now());
                return testResultRepository.save(newResult);
            });
    }

    public TestResult submitTest(String identifier, Map<String, String> answers, String email){
        User user = userRepository.findByEmail(email).orElseThrow();
        
        Test test = Optional.ofNullable(findTestByIdOrSlug(identifier))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi: " + identifier));

        TestResult result = testResultRepository.findByTestIdAndStudentIdAndStatus(test.getId(), user.getId(), TestResult.Status.DOING)
            .stream().findFirst()
            .orElseThrow(() -> new IllegalStateException("Không tìm thấy bài thi đang làm"));

        double totalScore = 0;
        double totalPoints = test.getQuestions().stream()
                .map(q -> q.getPoints() != null ? q.getPoints().doubleValue() : 0.0)
                .mapToDouble(Double::doubleValue).sum();

        for(Question q : test.getQuestions()){
            String selected = answers.get("answers[" + q.getId() + "]");
            if (selected != null && selected.equals(q.getCorrectAnswer())) {
                totalScore += (q.getPoints() != null) ? q.getPoints().doubleValue() : 0;
            }
        }

        double finalScore = (totalPoints > 0) ?  (totalScore / totalPoints) * 10.0 : 0;
        finalScore = Math.round(finalScore * 100.0) / 100.0;

        result.setScore(BigDecimal.valueOf(finalScore));
        result.setSubmitTime(LocalDateTime.now());
        result.setStatus(TestResult.Status.COMPLETED);

        if(result.getStartTime() != null){
            long seconds = java.time.Duration.between(result.getStartTime(), result.getSubmitTime()).getSeconds();
            result.setExecutionTimeSeconds((int) seconds);
        }

        return testResultRepository.save(result);
    }

    public Test saveTest(Long id, Long lessonId, String title, Integer duration) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new RuntimeException("Không tìm thấy bài học"));
        Test test = (id != null) ? testRepository.findById(id).orElse(new Test()) : new Test();
        test.setId(id);
        test.setLesson(lesson);
        test.setTitle(title);
        String baseSlug = SlugUtils.makeSlug(test.getTitle());
        String finalSlug = baseSlug;
        int count = 1;
        Long currentId = (test.getId() != null ) ? test.getId() : -1L;
        while(testRepository.existsBySlugAndIdNot(finalSlug, currentId)) {
            finalSlug = baseSlug + "-" + count;
            count++;
        }

        test.setSlug(finalSlug);
        test.setDurationMinutes(duration);
        return testRepository.save(test);
    }

    public void deleteTest(Long id) {
        testRepository.deleteById(id);
    }

    public void saveQuestion(Long id, Long testId, String text, String a, String b, String c, String d, String correct, Double points) {
        Test test = testRepository.findById(testId).orElseThrow();
        Question q = (id != null) ? questionRepository.findById(id).orElse(new Question()) : new Question();
        q.setTest(test);
        q.setQuestionText(text);
        q.setOptionA(a); 
        q.setOptionB(b); 
        q.setOptionC(c); 
        q.setOptionD(d);
        q.setCorrectAnswer(correct);
        q.setPoints(BigDecimal.valueOf(points));
        questionRepository.save(q);
    }

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
