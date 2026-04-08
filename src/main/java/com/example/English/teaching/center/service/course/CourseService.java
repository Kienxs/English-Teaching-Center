package com.example.English.teaching.center.service.course;

import com.example.English.teaching.center.dto.course.CourseDetailResponse;
import com.example.English.teaching.center.dto.course.CourseSaveRequest;
import com.example.English.teaching.center.entity.Course;
import com.example.English.teaching.center.entity.Lesson;
import com.example.English.teaching.center.entity.StudentCourse;
import com.example.English.teaching.center.entity.Teacher;
import com.example.English.teaching.center.entity.Test;
import com.example.English.teaching.center.entity.TestResult;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.exception.RateLimitException;
import com.example.English.teaching.center.mapper.CourseMapper;
import com.example.English.teaching.center.repository.CourseRepository;
import com.example.English.teaching.center.repository.LessonRepository;
import com.example.English.teaching.center.repository.StudentCourseRepository;
import com.example.English.teaching.center.repository.TeacherRepository;
import com.example.English.teaching.center.repository.TestResultRepository;
import com.example.English.teaching.center.repository.UserRepository;
import com.example.English.teaching.center.service.infra.RateLimitingService;
import com.example.English.teaching.center.utils.NetworkUtils;
import com.example.English.teaching.center.utils.SlugUtils;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
    private final CourseMapper courseMapper;
    private final RateLimitingService rateLimitingService; 

    public CourseService(CourseRepository courseRepository, 
                        TeacherRepository teacherRepository,
                        TestResultRepository testResultRepository,
                        UserRepository userRepository,
                        LessonRepository lessonRepository,
                        StudentCourseRepository studentCourseRepository,
                        CourseMapper courseMapper,
                        RateLimitingService rateLimitingService) {
        this.courseRepository = courseRepository;
        this.teacherRepository = teacherRepository;
        this.testResultRepository = testResultRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.courseMapper = courseMapper;
        this.rateLimitingService = rateLimitingService;
    }

// Process for student --------------------------------------------------------------------
    public List<CourseDetailResponse> getAllCourses() {
        return courseRepository.findAll().stream()
            .map(courseMapper::toDTO)
            .toList();
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
        String clientIP = NetworkUtils.getClientIPFromContext();
        String limitKey = "VIEW_COURSE_" + courseId + "_" + clientIP;

        Bucket bucket = rateLimitingService.resolveBucket(limitKey, 1, 30);
        if(bucket.tryConsume(1)) {
            courseRepository.incrementViewCount(courseId);
        }
    }

    public Map<String, Object> getCourseDetailData(String identifier, 
                                                String email,
                                                HttpSession session ) {
        Course course = Optional.ofNullable(findCourseByIdOrSlug(identifier))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học: " + identifier));

        incrementViewCount(course.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("course", course);

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

    public Map<String, Object> getMyCourseDetailData(String identifier, Long lessonId, String testSlug, Long userId) {
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

                    if (testSlug != null && !testSlug.trim().isEmpty()) {
                        processTestData(testSlug.trim(), userId, selectedLesson, data);
                    }
                }
            });
        }
        return data;
    }

    private void processTestData(String testSlug, Long userId, Lesson lesson, Map<String, Object> data) {
        Test selectedTest = lesson.getTests().stream()
                .filter(t -> t.getSlug().equals(testSlug))
                .findFirst().orElse(null);

        if (selectedTest != null) {
            List<TestResult> history = testResultRepository.findByTestIdAndStudentIdOrderByTakenAtDesc(selectedTest.getId(), userId);
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
    public Page<CourseDetailResponse> getCoursesByTeacher(Long teacherId, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());
        Page<Course> coursePage = courseRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId, pageable);
        return coursePage.map(courseMapper::toDTO);
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

    @Transactional 
    public Course saveOrUpdateCourse(CourseSaveRequest dto, Long teacherId) {
        String clientIP = NetworkUtils.getClientIPFromContext();
        Bucket bucket = rateLimitingService.resolveBucket("SAVE_COURSE_" + teacherId + "_" + clientIP, 20, 1);
        if(!bucket.tryConsume(1)) throw new RateLimitException("Thao tác lưu khóa học quá nhanh, vui lòng chờ ít giây!");

        String baseSlug = SlugUtils.makeSlug(dto.getName());
        String finalSlug = baseSlug;
        int count = 1;
        Long currentId = (dto.getId() != null) ? dto.getId() : -1L;
        
        while (courseRepository.existsBySlugAndIdNot(finalSlug, currentId)) {
            finalSlug = baseSlug + "-" + count;
            count++;
        }

        Course course;
        if (dto.getId() == null) {
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy giáo viên"));
            course = new Course();
            course.setTeacher(teacher);
            course.setStatus(Course.Status.DRAFT);
        } else {
            course = courseRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));
            
            if (!course.getTeacher().getId().equals(teacherId)) 
                throw new SecurityException("Cảnh báo bảo mật: Bạn không có quyền chỉnh sửa khóa học này!");
        }

        course.setName(dto.getName());
        course.setSlug(finalSlug);
        course.setDescription(dto.getDescription());
        course.setFee(dto.getFee());
        course.setCategory(Course.Category.valueOf(dto.getCategory())); 
        
        return courseRepository.save(course);
    }

    public void deleteDraftCourse(Long courseId, Long teacherId){
        String clientIP = NetworkUtils.getClientIPFromContext();
        Bucket bucket = rateLimitingService.resolveBucket("DELETE_COURSE_" + teacherId + "_" + clientIP, 5, 1);
        if(!bucket.tryConsume(1)) throw new RateLimitException("Thao tác quá nhanh!");

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));
        
        if(!course.getTeacher().getId().equals(teacherId))
            throw new SecurityException("Cảnh báo bảo mật: Bạn không có quyền xóa khóa học này!");
        
        if(course.getStatus() != Course.Status.DRAFT)
            throw new IllegalStateException("Lỗi nghiệp vụ: Chỉ có thể xóa khóa học ở trạng thái NHÁP");

        courseRepository.delete(course);
    }

    public Page<CourseDetailResponse> getCoursesWithFilters(String categoryStr,
                                                String modeStr,
                                                String keyword,
                                                String sortStr,
                                                int page,
                                                int size){
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if("price_asc".equals(sortStr))
            sort = Sort.by(Sort.Direction.ASC, "fee");
        else if("price_desc".equals(sortStr))
            sort = Sort.by(Sort.Direction.DESC, "fee");
        
        Pageable pageable = PageRequest.of(page, size, sort);

        Course.Category categoryEnum = null;
        if(categoryStr != null && !categoryStr.trim().isEmpty()){
            try{
                categoryEnum = Course.Category.valueOf(categoryStr);
            }catch(IllegalArgumentException ignored){}
        }

        Course.Mode modeEnum = null;
        if(modeStr != null && !modeStr.trim().isEmpty()){
            try{
                modeEnum = Course.Mode.valueOf(modeStr);
            }catch(IllegalArgumentException ignored){}
        }

        String validKeyword = (keyword != null && !keyword.trim().isEmpty() ? keyword.trim() : null);

        Page<Course> courses = courseRepository.findCoursesWithFilters(categoryEnum, modeEnum, validKeyword, pageable);

        return courses.map(courseMapper::toDTO);
    }
}