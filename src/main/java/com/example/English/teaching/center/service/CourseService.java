package com.example.English.teaching.center.service;

import com.example.English.teaching.center.model.Course;
import com.example.English.teaching.center.model.Lesson;
import com.example.English.teaching.center.model.StudentCourse;
import com.example.English.teaching.center.model.Teacher;
import com.example.English.teaching.center.model.Test;
import com.example.English.teaching.center.model.TestResult;
import com.example.English.teaching.center.model.User;
import com.example.English.teaching.center.repository.CourseRepository;
import com.example.English.teaching.center.repository.LessonRepository;
import com.example.English.teaching.center.repository.StudentCourseRepository;
import com.example.English.teaching.center.repository.TeacherRepository;
import com.example.English.teaching.center.repository.TestResultRepository;
import com.example.English.teaching.center.repository.UserRepository;
import com.example.English.teaching.center.utils.SlugUtils;

import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
public class CourseService {
    private final CourseRepository courseRepository; 
    private final TeacherRepository teacherRepository;
    private final TestResultRepository testResultRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final StudentCourseRepository studentCourseRepository;

    public CourseService(CourseRepository courseRepository, 
                        TeacherRepository teacherRepository,
                        TestResultRepository testResultRepository,
                        UserRepository userRepository,
                        LessonRepository lessonRepository,
                        StudentCourseRepository studentCourseRepository) {
        this.courseRepository = courseRepository;
        this.teacherRepository = teacherRepository;
        this.testResultRepository = testResultRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.studentCourseRepository = studentCourseRepository;
    }

// Process for student --------------------------------------------------------------------
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Optional<Course> findCourseById(Long id) {
        return courseRepository.findById(id);
    }

    public List<Course> getCoursesByStudentAndStatus(Long studentId, StudentCourse.Status status) {
        return courseRepository.findCoursesByStudentIdAndStatus(studentId, status);
    }

    private Course findCourseByIdOrSlug(String identifier) {
        return courseRepository.findBySlug(identifier)
            .orElseGet(() -> {
                try {
                    return courseRepository.findById(Long.parseLong(identifier)).orElse(null);
                } catch (NumberFormatException e) {
                    return null;
                }
            });
    }

    @Transactional 
    public void incrementViewCount(Long courseId) {
        courseRepository.incrementViewCount(courseId);
    }

    public Map<String, Object> getCourseDetailData(String identifier, 
                                                String email,
                                                HttpSession session ) {
        Course course = Optional.ofNullable(findCourseByIdOrSlug(identifier))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học: " + identifier));
        
        String sessionKey = "VIEWED_COURSES";

        // 1. Get a list of courses viewed from Sessions.
        @SuppressWarnings("unchecked")
        Set<Long> viewedCourses = (Set<Long>) session.getAttribute(sessionKey);

        if(viewedCourses == null) {
            viewedCourses = new HashSet<>();
        }

        // 2. Check if the current course is already in the list.
        if(!viewedCourses.contains(course.getId())){
            incrementViewCount(course.getId());
            course.setViewCount(course.getViewCount() + 1);

            viewedCourses.add(course.getId());
            session.setAttribute(sessionKey, viewedCourses);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("course", course);

        // 3. Check if you have purchased it.
        boolean isOwned = false;
        BigDecimal balance = BigDecimal.ZERO;

        if(email != null && !email.equalsIgnoreCase("anonymousUser")){
            Optional<User> userOpt = userRepository.findByEmail(email);
            if(userOpt.isPresent()){
                User user = userOpt.get();
                balance = user.getBalance();

                isOwned = studentCourseRepository.existsByStudentIdAndCourseId(user.getId(), course.getId());

                if (course.getTeacher().getId().equals(user.getId())){
                    isOwned = true;
                }
            }
        }

        data.put("userBalance", balance);
        data.put("isOwned", isOwned);
        return data;
    }

    public Map<String, Object> getMyCourseDetailData(String identifier, Long lessonId, Long testId, Long userId) {
        Course course = Optional.ofNullable(findCourseByIdOrSlug(identifier))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học: " + identifier));
        
        course = courseRepository.findByIdWithLessons(course.getId())
                .orElseThrow(() -> new RuntimeException("Lỗi nạp danh sách bài học"));

        Map<String, Object> data = new HashMap<>();
        data.put("course", course);

        if (lessonId != null) {
            lessonRepository.findByIdWithMaterials(lessonId).ifPresent(selectedLesson -> {
                Lesson lessonWithTests = lessonRepository.findByIdWithTests(lessonId);
                if (lessonWithTests != null) {
                    selectedLesson.setTests(lessonWithTests.getTests());
                    data.put("selectedLesson", selectedLesson);

                    if (testId != null) {
                        processTestData(testId, userId, selectedLesson, data);
                    }
                }
            });
        }
        return data;
    }

    private void processTestData(Long testId, Long userId, Lesson lesson, Map<String, Object> data) {
        Test selectedTest = lesson.getTests().stream()
                .filter(t -> t.getId().equals(testId))
                .findFirst().orElse(null);

        if (selectedTest != null) {
            List<TestResult> history = testResultRepository.findByTestIdAndStudentIdOrderByTakenAtDesc(testId, userId);
            BigDecimal bestScore = history.stream()
                    .filter(r -> r.getStatus() == TestResult.Status.COMPLETED)
                    .map(TestResult::getScore)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::max);

            data.put("selectedTest", selectedTest);
            data.put("testHistory", history);
            data.put("bestScore", bestScore);
        }
    }

    //Process for teacher ---------------------------------------------------------------------
    public Page<Course> getCoursesByTeacher(Long teacherId, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());
        return courseRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId, pageable);
    }

    public Optional<Course> findBySlug(String slug) {
        return courseRepository.findBySlug(slug);
    }

    public Course createNewDraftCourse() {
        Course course = new Course();
        course.setStatus(Course.Status.DRAFT);
        course.setFee(BigDecimal.ZERO);
        return course;
    }

    public Course saveOrUpdateCourse(Course course, Long teacherId) {
        String baseSlug = SlugUtils.makeSlug(course.getName());
        String finalSlug = baseSlug;
        int count = 1;
        Long currentId = (course.getId() != null) ? course.getId() : -1L;
        while (courseRepository.existsBySlugAndIdNot(finalSlug, currentId)) {
            finalSlug = baseSlug + "-" + count;
            count++;
        }
        course.setSlug(finalSlug);

        if(course.getId() == null){
            Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên"));
            course.setTeacher(teacher);
            course.setStatus(Course.Status.DRAFT);
            return courseRepository.save(course);
        }
        else{
            Course existing = courseRepository.findById(course.getId()).orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));
            existing.setName(course.getName());
            existing.setSlug(finalSlug);
            existing.setDescription(course.getDescription());
            existing.setFee(course.getFee());
            existing.setCategory(course.getCategory());
            return courseRepository.save(existing);
        }
    }

    public void deleteDraftCourse(Long id){
        courseRepository.deleteById(id);
    }
}