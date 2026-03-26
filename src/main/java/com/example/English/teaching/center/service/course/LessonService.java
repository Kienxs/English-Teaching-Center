package com.example.English.teaching.center.service.course;

import org.springframework.stereotype.Service;

import com.example.English.teaching.center.dto.LessonSaveDTO;
import com.example.English.teaching.center.dto.MaterialDTO;
import com.example.English.teaching.center.entity.Lesson;
import com.example.English.teaching.center.entity.Material;
import com.example.English.teaching.center.repository.CourseRepository;
import com.example.English.teaching.center.repository.LessonRepository;
import com.example.English.teaching.center.repository.MaterialRepository;

import jakarta.transaction.Transactional;

@Service
public class LessonService {
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final MaterialRepository materialRepository;

    public LessonService(LessonRepository lessonRepository, 
                        CourseRepository courseRepository, 
                        MaterialRepository materialRepository) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.materialRepository = materialRepository;
    }

    @Transactional
    public void saveOrUpdateLesson(LessonSaveDTO dto){
        Lesson lesson;
        if (dto.getId() != null) {
            lesson = lessonRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học"));
        } else {
            lesson = new Lesson();
            lesson.setCourse(courseRepository.findById(dto.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học")));
        }
        
        lesson.setTitle(dto.getTitle());
        lesson.setLessonOrder(dto.getLessonOrder());
        lesson.setDescription(dto.getDescription());
        lessonRepository.save(lesson);
    }

    @Transactional
    public void deleteLesson(Long lessonId){
        lessonRepository.deleteById(lessonId);
    }

    @Transactional
    public Long saveOrUpdateMaterial(MaterialDTO dto) {
        Lesson lesson = lessonRepository.findById(dto.getLessonId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học"));
        Material material;
        
        if (dto.getId() != null) {
            material = materialRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu"));
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

    public Long deleteMaterial(Long materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu"));
        Long courseId = material.getLesson().getCourse().getId();
        materialRepository.delete(material);
        return courseId;
    }
}
