package com.example.English.teaching.center.controller.teacher;

import com.example.English.teaching.center.dto.course.CourseDetailResponse;
import com.example.English.teaching.center.dto.course.CourseSaveRequest;
import com.example.English.teaching.center.dto.course.LessonSaveRequest;
import com.example.English.teaching.center.dto.course.MaterialSaveRequest;
import com.example.English.teaching.center.entity.Course;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.service.course.CourseService;
import com.example.English.teaching.center.service.course.LessonService;
import com.example.English.teaching.center.service.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/teacher")
public class TeacherCourseController {

    private final CourseService courseService;
    private final LessonService lessonService;
    private final UserService userService;

    public TeacherCourseController(CourseService courseService, LessonService lessonService, UserService userService) {
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.userService = userService;
    }

    @GetMapping("/course-management")
    public String courseManagement(Model model, Principal principal,
                                   @RequestParam(defaultValue = "0") int page, 
                                   @RequestParam(defaultValue = "5") int size) { 
        if(principal == null) return "redirect:/login";

        User teacher = userService.findByEmail(principal.getName());
        Page<CourseDetailResponse> coursePage = courseService.getCoursesByTeacher(teacher.getId(), page, size);
        
        model.addAttribute("coursePage", coursePage); 
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", coursePage.getTotalPages());
        model.addAttribute("teacherName", teacher.getFullName());
        
        return "teacher/course-management";
    }

    @GetMapping("/course/create")
    public String showCreateForm(Model model) {
        model.addAttribute("course", courseService.createNewDraftCourse()); 
        model.addAttribute("pageTitle", "Tạo khóa học mới");
        return "teacher/course-edit"; 
    }

    @GetMapping("/course/edit/{slug}")
    public String showEditForm(@PathVariable("slug") String slug, Model model) {
        Course existingCourse = courseService.findBySlug(slug).orElse(null);
        if (existingCourse == null) return "redirect:/teacher/course-management";

        model.addAttribute("course", existingCourse); 
        model.addAttribute("pageTitle", "Chỉnh sửa: " + existingCourse.getName());
        return "teacher/course-edit"; 
    }

    @GetMapping("/course/delete/{id}")
    public String deleteCourse(@PathVariable("id") Long id, 
                                RedirectAttributes ra, Principal principal) {
        try{
            User currenUser = userService.findByEmail(principal.getName());
            courseService.deleteDraftCourse(id, currenUser.getId());
            ra.addFlashAttribute("successMessage", "Đã xóa khóa học thành công!");
        }catch (Exception e){
            ra.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/teacher/course-management";
    }

    @PostMapping("/course/save")
    public String saveCourse(@ModelAttribute("course") CourseSaveRequest dto, Principal principal) {
        User currentUser = userService.findByEmail(principal.getName());
        Course savedCourse = courseService.saveOrUpdateCourse(dto, currentUser.getId());
        return "redirect:/teacher/course/edit/" + savedCourse.getSlug();
    }

    @PostMapping("/lesson/save")
    public String saveLesson(@ModelAttribute("LessonSaveDTO") LessonSaveRequest dto,
                            Principal principal){
        User currentUser = userService.findByEmail(principal.getName());

        lessonService.saveOrUpdateLesson(dto, currentUser.getId());
        String slug = courseService.findCourseById(dto.getCourseId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"))
                        .getSlug();
        return "redirect:/teacher/course/edit/" + slug;
    }

    @PostMapping("/lesson/delete/{lessonId}/{courseSlug}")
    public String deleteLesson(@PathVariable ("lessonId") Long lessonId,
                               @PathVariable ("courseSlug") String courseSlug,
                                Principal principal){
        User currentUser = userService.findByEmail(principal.getName());

        lessonService.deleteLesson(lessonId, currentUser.getId());
        return "redirect:/teacher/course/edit/" + courseSlug;
    }

    @PostMapping("/material/save")
    public String saveMaterial(@ModelAttribute MaterialSaveRequest dto, 
                                Principal principal){ 
        String email = principal.getName();
        User currentUser = userService.findByEmail(email);
        Long teacherId = currentUser.getId();

        Long courseId = lessonService.saveOrUpdateMaterial(dto, teacherId);
        
        String slug = courseService.findCourseById(courseId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khóa học"))
                    .getSlug();
        return "redirect:/teacher/course/edit/" + slug;
    }

    @PostMapping("/material/delete/{id}/{courseSlug}")
    public String deleteMaterial(@PathVariable Long id, 
                                 @PathVariable String courseSlug, 
                                 Principal principal){ 
        
        String email = principal.getName();
        User currentUser = userService.findByEmail(email);
        Long teacherId = currentUser.getId();

        lessonService.deleteMaterial(id, teacherId);
        
        return "redirect:/teacher/course/edit/" + courseSlug;
    }
}