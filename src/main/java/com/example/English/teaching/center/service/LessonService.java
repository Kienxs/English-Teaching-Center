package com.example.English.teaching.center.service;

import org.springframework.stereotype.Service;

import com.example.English.teaching.center.model.Lesson;
import com.example.English.teaching.center.model.Material;
import com.example.English.teaching.center.repository.CourseRepository;
import com.example.English.teaching.center.repository.LessonRepository;
import com.example.English.teaching.center.repository.MaterialRepository;

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

    public void saveOrUpdateLesson(Long id, Long courseId, String title, Integer order, String description){
        Lesson lesson;
        if (id != null) {
            lesson = lessonRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học"));
        } else {
            lesson = new Lesson();
            lesson.setCourse(courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học")));
        }
        
        lesson.setTitle(title);
        lesson.setLessonOrder(order);
        lesson.setDescription(description);
        lessonRepository.save(lesson);
    }

    public void deleteLesson(Long lessonId){
        lessonRepository.deleteById(lessonId);
    }

    public Long saveOrUpdateMaterial(Long id, Long lessonId, String title, String fileUrl, String type) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow();
        Material material;
        
        if (id != null) {
            material = materialRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu"));
        } else {
            material = new Material();
            material.setLesson(lesson);
        }

        material.setTitle(title);
        material.setFileUrl(fileUrl);
        material.setType(Material.FileType.valueOf(type));
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
