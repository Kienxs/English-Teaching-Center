package com.example.English.teaching.center.service.course;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.English.teaching.center.dto.course.QuestionSaveRequest;
import com.example.English.teaching.center.dto.course.TestResponse;
import com.example.English.teaching.center.dto.course.TestSaveRequest;
import com.example.English.teaching.center.entity.Lesson;
import com.example.English.teaching.center.entity.Question;
import com.example.English.teaching.center.entity.Test;
import com.example.English.teaching.center.entity.TestResult;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.exception.RateLimitException;
import com.example.English.teaching.center.mapper.TestMapper;
import com.example.English.teaching.center.repository.LessonRepository;
import com.example.English.teaching.center.repository.QuestionRepository;
import com.example.English.teaching.center.repository.TestRepository;
import com.example.English.teaching.center.repository.TestResultRepository;
import com.example.English.teaching.center.repository.UserRepository;
import com.example.English.teaching.center.service.infra.RateLimitingService;
import com.example.English.teaching.center.utils.SlugUtils;

import io.github.bucket4j.Bucket;
import jakarta.transaction.Transactional;

@Service
public class TestService {
    private final TestRepository testRepository;
    private final TestResultRepository testResultRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final TestMapper testMapper;
    private final RateLimitingService rateLimitingService;

    public TestService(TestRepository testRepository,
                       TestResultRepository testResultRepository,
                       UserRepository userRepository,
                       LessonRepository lessonRepository,
                       QuestionRepository questionRepository,
                       TestMapper testMapper,
                       RateLimitingService rateLimitingService) {
        this.testRepository = testRepository;
        this.testResultRepository = testResultRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.questionRepository = questionRepository;
        this.testMapper = testMapper;
        this.rateLimitingService = rateLimitingService;
    }
// For Students ---------------------------------------------------------
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

    public TestResponse getSafeTestDetails(String identifier){
        Test test = Optional.ofNullable((findTestByIdOrSlug(identifier)))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi: " + identifier));
        return testMapper.toTestDTO(test);
    }

    public TestResult startOrResumeTest(String identifier, User user){
        String limitKey = "START_TEST_" + user.getEmail();
        Bucket bucket = rateLimitingService.resolveBucket(limitKey, 5, 1);
        if(!bucket.tryConsume(1)) 
            throw new RateLimitException("Vui lòng không nháy đúp nút bắt đầu!");

        Test test = Optional.ofNullable(findTestByIdOrSlug(identifier))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi: " + identifier));

        List<TestResult> activeTests = testResultRepository.findByStudentIdAndStatus(user.getId(), TestResult.Status.DOING);

        if(!activeTests.isEmpty()){
            TestResult active = activeTests.get(0);
            if(!active.getTest().getId().equals(test.getId()))
                throw new IllegalStateException("Bạn đang có bài thi chưa hoàn thành: " + active.getTest().getTitle());
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
        String limitKey = "SUBMIT_TEST_" + email;

        Bucket bucket = rateLimitingService.resolveBucket(limitKey, 3, 1);
        if(!bucket.tryConsume(1)) 
            throw new RateLimitException("Đang xử lý bài thi, vui lòng không gửi liên tục!");

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        
        Test test = Optional.ofNullable(findTestByIdOrSlug(identifier))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài thi: " + identifier));

        List<TestResult> doingTests = testResultRepository.findByStudentIdAndStatus(user.getId(), TestResult.Status.DOING);

        if(doingTests.isEmpty() || !doingTests.get(0).getTest().getId().equals(test.getId()))
            throw new IllegalStateException("Không tìm thấy bài thi đang làm hoặc đã nộp rồi.");

        TestResult result = doingTests.get(0);

        long expectedDurationSeconds = test.getDurationMinutes() * 60L;
        long actualTakenSeconds = java.time.Duration.between(result.getStartTime(), LocalDateTime.now()).getSeconds();
        
        if (actualTakenSeconds > expectedDurationSeconds + 60) {
            result.setScore(BigDecimal.ZERO);
            result.setSubmitTime(LocalDateTime.now());
            result.setStatus(TestResult.Status.COMPLETED);
            result.setExecutionTimeSeconds((int) actualTakenSeconds);
            testResultRepository.save(result);
            throw new IllegalStateException("Bạn đã nộp bài quá thời gian quy định! Bài thi bị hủy kết quả.");
        }

        double earnedPoints = 0;
        double totalPoints = 0;
        List<TestResult.AnswerDetail> detailsList = new java.util.ArrayList<>();

        for(Question q : test.getQuestions()){
            double questionWeight = (q.getPoints() != null) ? q.getPoints().doubleValue() : 0;
            totalPoints += questionWeight;

            String selectedAnswerText = answers.get("answers[" + q.getId() + "]");

            String correctAnswerText = q.getOptions().stream()
                .filter(Question.Option::isCorrect)
                .map(Question.Option::getText)
                .findFirst()
                .orElse("");

            boolean isCorrect = (selectedAnswerText != null && selectedAnswerText.equals(correctAnswerText));

            if (isCorrect)  earnedPoints += questionWeight;

            detailsList.add(new TestResult.AnswerDetail(q.getId(), selectedAnswerText, isCorrect));
        }

        double finalScore = (totalPoints > 0) ? (earnedPoints / totalPoints) * 10.0 : 0;
        BigDecimal roundedScore = new BigDecimal(finalScore).setScale(2, RoundingMode.HALF_UP);

        result.setScore(roundedScore);
        result.setSubmitTime(LocalDateTime.now());
        result.setStatus(TestResult.Status.COMPLETED);
        result.setDetails(detailsList);
        result.setExecutionTimeSeconds((int) actualTakenSeconds); 

        return testResultRepository.save(result);
    }

// For Teachers ---------------------------------------------------------
    private void verifyTestOwnership(Test test, Long teacherId){
        if(!test.getLesson().getCourse().getTeacher().getId().equals(teacherId)){
            throw new RuntimeException("Cảnh báo bảo mật: Bạn không có quyền thao tác trên bài thi này!");
        }
    }

    @Transactional
    public String saveTest(TestSaveRequest dto, Long teacherId) {
        String limitKey = "SAVE_TEST_" + teacherId;

        Bucket bucket = rateLimitingService.resolveBucket(limitKey, 20, 1);
        if (!bucket.tryConsume(1)) 
            throw new RateLimitException("Hệ thống đang bận, thầy/cô vui lòng thao tác chậm lại một chút nhé!");

        Lesson lesson = lessonRepository.findById(dto.getLessonId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học"));

        if(!lesson.getCourse().getTeacher().getId().equals(teacherId))
            throw new SecurityException("Bạn không có quyền tạo bài thi cho khóa học của người khác!");

        Test test = (dto.getId() != null) ? testRepository.findById(dto.getId()).orElse(new Test()) : new Test();
        
        if(test.getId() != null)
            verifyTestOwnership(test, teacherId);

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
    public void deleteTest(Long id, Long teacherId) { 
        String limitKey = "DELETE_TEST_" + teacherId;

        Bucket bucket = rateLimitingService.resolveBucket(limitKey, 10, 1);
        if (!bucket.tryConsume(1)) 
            throw new RateLimitException("Thao tác quá nhanh!");

        Test test = testRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài test"));
        verifyTestOwnership(test, teacherId);
        testRepository.delete(test);
    }

    @Transactional
    public void saveQuestion(QuestionSaveRequest dto, Long teacherId) { 
        String limitKey = "SAVE_QUESTION_" + teacherId;
        
        Bucket bucket = rateLimitingService.resolveBucket(limitKey, 20, 1);
        if (!bucket.tryConsume(1)) 
            throw new RateLimitException("Thầy/cô đang lưu câu hỏi quá nhanh, vui lòng chờ ít giây!");

        Test test = testRepository.findById(dto.getTestId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài Test!"));
            
        verifyTestOwnership(test, teacherId);

        Question q = (dto.getId() != null) 
            ? questionRepository.findById(dto.getId()).orElse(new Question()) 
            : new Question();
            
        q.setTest(test);
        q.setQuestionText(dto.getQuestionText());
        q.setPoints(dto.getPoints());

        List<Question.Option> optionsList = Arrays.asList(
            new Question.Option(dto.getOptionA(), "A".equals(dto.getCorrectAnswer())),
            new Question.Option(dto.getOptionB(), "B".equals(dto.getCorrectAnswer())),
            new Question.Option(dto.getOptionC(), "C".equals(dto.getCorrectAnswer())),
            new Question.Option(dto.getOptionD(), "D".equals(dto.getCorrectAnswer()))
        );
        q.setOptions(optionsList);

        questionRepository.save(q);
    }

    @Transactional
    public Long deleteQuestionAndGetTestId(Long questionId, Long teacherId) { 
        String limitKey = "DELETE_QUESTION_" + teacherId;

        Bucket bucket = rateLimitingService.resolveBucket(limitKey, 20, 1);
        if (!bucket.tryConsume(1)) 
            throw new RateLimitException("Thao tác xóa quá nhanh!");

        Question q = questionRepository.findById(questionId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));
            
        verifyTestOwnership(q.getTest(), teacherId); 
        
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