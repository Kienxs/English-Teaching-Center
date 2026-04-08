package com.example.English.teaching.center.service.course;

import org.springframework.stereotype.Service;

import com.example.English.teaching.center.dto.course.LessonSaveRequest;
import com.example.English.teaching.center.dto.course.MaterialResponse;
import com.example.English.teaching.center.dto.course.MaterialSaveRequest;
import com.example.English.teaching.center.entity.Course;
import com.example.English.teaching.center.entity.Lesson;
import com.example.English.teaching.center.entity.Material;
import com.example.English.teaching.center.exception.RateLimitException;
import com.example.English.teaching.center.repository.CourseRepository;
import com.example.English.teaching.center.repository.LessonRepository;
import com.example.English.teaching.center.repository.MaterialRepository;
import com.example.English.teaching.center.service.infra.RateLimitingService;
import com.example.English.teaching.center.utils.NetworkUtils;

import io.github.bucket4j.Bucket;
import jakarta.transaction.Transactional;

@Service
public class LessonService {
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final MaterialRepository materialRepository;
    private final RateLimitingService rateLimitingService; 

    public LessonService(LessonRepository lessonRepository, 
                         CourseRepository courseRepository, 
                         MaterialRepository materialRepository,
                         RateLimitingService rateLimitingService) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.materialRepository = materialRepository;
        this.rateLimitingService = rateLimitingService;
    }

    private void verifyLessonOwnership(Lesson lesson, Long teacherId) {
        if (!lesson.getCourse().getTeacher().getId().equals(teacherId)) 
            throw new SecurityException("Cảnh báo bảo mật: Bạn không có quyền thao tác trên bài học/khóa học này!");
    }

    @Transactional
    public void saveOrUpdateLesson(LessonSaveRequest dto, Long teacherId){
        String clientIP = NetworkUtils.getClientIPFromContext();
        Bucket bucket = rateLimitingService.resolveBucket("SAVE_LESSON_" + teacherId + "_" + clientIP, 20, 1);
        if (!bucket.tryConsume(1)) 
            throw new RateLimitException("Thầy/cô thao tác quá nhanh, vui lòng chờ ít giây!");

        Lesson lesson;
        if (dto.getId() != null) {
            lesson = lessonRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học"));
            verifyLessonOwnership(lesson, teacherId); // Dùng hàm dùng chung
        } else {
            Course course = courseRepository.findById(dto.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"));

            if (!course.getTeacher().getId().equals(teacherId)) 
                throw new SecurityException("Bạn không có quyền thêm bài vào khóa học của người khác!");

            lesson = new Lesson();
            lesson.setCourse(course);
        }
        
        lesson.setTitle(dto.getTitle());
        lesson.setLessonOrder(dto.getLessonOrder());
        lesson.setDescription(dto.getDescription());
        lessonRepository.save(lesson);
    }

    public void deleteLesson(Long lessonId, Long teacherId){
        String clientIP = NetworkUtils.getClientIPFromContext();
        Bucket bucket = rateLimitingService.resolveBucket("DELETE_LESSON_" + teacherId + "_" + clientIP, 10, 1);
        if (!bucket.tryConsume(1)) 
            throw new RateLimitException("Thao tác quá nhanh!");

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học"));

        verifyLessonOwnership(lesson, teacherId); 

        lessonRepository.deleteById(lessonId);
    }

    @Transactional
    public Long saveOrUpdateMaterial(MaterialSaveRequest dto, Long teacherId) { 
        String clientIP = NetworkUtils.getClientIPFromContext();
        Bucket bucket = rateLimitingService.resolveBucket("SAVE_MATERIAL_" + teacherId + "_" + clientIP, 30, 1);
        if (!bucket.tryConsume(1)) 
            throw new RateLimitException("Hệ thống đang lưu tài liệu, vui lòng chờ ít giây!");

        Lesson lesson = lessonRepository.findById(dto.getLessonId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học"));
        
        verifyLessonOwnership(lesson, teacherId);

        Material material;
        if (dto.getId() != null) {
            material = materialRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu"));
            
            if (!material.getLesson().getId().equals(lesson.getId())) {
                throw new SecurityException("Tài liệu này không thuộc về bài học đã chọn!");
            }
        } else {
            material = new Material();
            material.setLesson(lesson);
        }

        material.setTitle(dto.getTitle());
        material.setFileUrl(dto.getFileUrl());
        material.setType(Material.FileType.valueOf(dto.getType()));
        materialRepository.save(material);

        return lesson.getCourse().getId();
    }

    @Transactional
    public Long deleteMaterial(Long materialId, Long teacherId) { 
        String clientIP = NetworkUtils.getClientIPFromContext();
        Bucket bucket = rateLimitingService.resolveBucket("DELETE_MATERIAL_" + teacherId + "_" + clientIP, 10, 1);
        if (!bucket.tryConsume(1)) 
            throw new RateLimitException("Thao tác quá nhanh!");

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu"));

        verifyLessonOwnership(material.getLesson(), teacherId);

        Long courseId = material.getLesson().getCourse().getId();
        materialRepository.delete(material);
        return courseId;
    }
}
